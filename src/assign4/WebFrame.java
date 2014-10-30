package assign4;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/*
 * Class that represents the WebLoader GUI; this is 
 * the glue that ties everything together. Webworker threads are created
 * as necessary from this class to carry out the url downloads
 */
public class WebFrame extends JFrame{
	
	public static final String FILENAME = "links.txt";
	public static final int NUM_COLS = 2;
	
	// GUI ivars
	protected WebTableModel model;
	private JTable table;
	private JPanel panel;
	protected JButton single;
	protected JButton concurrent;
	protected JButton stop;
	private JTextField numThrField;
	protected JLabel running;
	protected JLabel completed;
	protected JLabel elapsed;
	protected JProgressBar progBar;
	private JPanel bottomPanel1;
	private JPanel bottomPanel2;
	
	// Data ivars
	protected long startTime;
	protected long endTime;
	protected ArrayList<String[]> tblData = new ArrayList<String[]>();
	private ArrayList<String> colNames = new ArrayList<String>(
			Arrays.asList("url", "status"));
	protected int threadsRunning;
	
	// frame
	private static WebFrame frame;
	protected CountDownLatch latch;
	protected ExecutorService service;
	
	
	// WebFrame ctor
	public WebFrame() {
		super("WebLoader");
		threadsRunning = 0;
		readFile();	
		initializeGUI();
		addActionListeners();
	}
	
	/*
	 * General method to initialize the GUI. It does
	 * so by creating a heirarchy of JPanels that are collected
	 * to form the main three panels of the GUI, the table, the 
	 * buttons and labels, and the progress bar along with the 
	 * stop button. 
	 * 
	 * Assumes that no threads are yet working during our
	 * setup. 
	 */
	public void initializeGUI() {
		panel = new JPanel();
		model = new WebTableModel();
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,300));
		panel.add(scrollpane);
		
		bottomPanel1 = new JPanel(new GridLayout(2,1));
		
			JPanel subPanel = new JPanel(new GridLayout(1,3));
			
			JPanel subsubPanel1 = new JPanel(new GridLayout(3,1));
			single = new JButton("Single Thread Fetch");
			subsubPanel1.add(single);
			concurrent = new JButton("Concurrent Thread Fetch");
			subsubPanel1.add(concurrent);
			numThrField = new JTextField();
			numThrField.setMaximumSize(new Dimension(30, 20));
			subsubPanel1.add(numThrField);
			subPanel.add(subsubPanel1);
			
			subPanel.add(Box.createRigidArea(new Dimension(0,40)));
			
			JPanel subsubPanel2 = new JPanel(new GridLayout(3,1));
			running = new JLabel("Running: ");
			completed = new JLabel("Completed: ");
			elapsed = new JLabel("Elapsed: ");
			subsubPanel2.add(running);
			subsubPanel2.add(completed);
			subsubPanel2.add(elapsed);
			subPanel.add(subsubPanel2);
			
		bottomPanel1.add(subPanel);
		
		bottomPanel2 = new JPanel(new GridLayout(1,2));
		progBar = new JProgressBar();
		
		JPanel spacingPanel = new JPanel(new GridLayout(1,3));
		stop = new JButton("Stop");
		stop.setEnabled(false);
		spacingPanel.add(Box.createRigidArea(new Dimension(0,20)));
		spacingPanel.add(stop);
		spacingPanel.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		bottomPanel2.add(progBar);
		bottomPanel2.add(spacingPanel);
		
	
		
			
			
		
	}
	
	/*
	 * Action Listener Methods for each of the buttons with
	 * which the user can interact. 
	 * 
	 * single and concurrent call launch(), which uses an executor
	 * to coordinate the correct number of threads for the downloads.
	 * 
	 * stop interrupts the entire process and returns the GUI to
	 * the ready state where it may execute another request.
	 */
	public void addActionListeners() {

		// Single Thread Button Pressed
		single.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				elapsed.setText("Elapsed: ");
				startTime = System.currentTimeMillis();
				clearData();
				latch = new CountDownLatch(tblData.size());
				single.setEnabled(false);
				concurrent.setEnabled(false);
				stop.setEnabled(true);
				progBar.setMaximum(tblData.size());
				
				int numThreads = 1;
				launch(numThreads);
			}	
		});

		// Concurrent Thread Button Pressed
		concurrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					try{
						int numThreads = Integer.parseInt(numThrField.getText());
						elapsed.setText("Elapsed: ");
						startTime = System.currentTimeMillis();
						clearData();
						latch = new CountDownLatch(tblData.size());
						single.setEnabled(false);
						concurrent.setEnabled(false);
						stop.setEnabled(true);
						progBar.setMaximum(tblData.size());
						
						launch(numThreads);
						
					}catch (NumberFormatException ex) {
						System.out.println("Please enter an integer number of threads.");
						numThrField.setText("Please enter # threads here");
					}
			}	
		});

		
		// Stop Button Pressed
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					single.setEnabled(true);
					concurrent.setEnabled(true);
					service.shutdownNow();
					stop.setEnabled(false);
			}	
		});
	}
	
	/*
	 * Method to reset our WebTableModel; the urls need 
	 * not be changed, but the statuses should be cleared 
	 * preceeding a new run. 
	 */
	public void clearData() {
		for (int i = 0; i < tblData.size(); i++){
			tblData.get(i)[1] = "";
		}
		model.fireTableDataChanged();
	}
	
	
	/**
	 * 
	 * Note to the grader: Dr. Young said in lecture that, despite
	 * the handout, we are allowed to use Executors.
	 * 
	 *  Method that uses an executor to handle launching 
	 *  the correct number of threads to complete the downloads.
	 *  Each new worker is a new instance of the WebWorker class,
	 *  which completes its run method to both attempt the URL
	 *  download and to communicate the process to the GUI, which
	 *  is thread safe in our invokeLater catch method.
	 *  
	 *  The executor shuts down upon completion of all the downloads.
	 *  
	 */
	public void launch(int threads){
		service = Executors.newFixedThreadPool(threads);
		
		for (int i = 0; i < tblData.size(); i++) {
			String url = tblData.get(i)[0];
			Runnable worker = new WebWorker(url, i, frame);
			if (!Thread.interrupted()) {
				service.submit(worker);
			} else {
				break;
			}
		}
		service.shutdown();
	}
	
	/*
	 * Our custom extension of AbstractTableModel that provides the
	 * model part of MVC for the WebLoader program. Here we simply overwrite 
	 * the table getter methods, referring to the data ivars created
	 * when we read in the url text file.
	 */
	public class WebTableModel extends AbstractTableModel{

		/**
		 * Overridden getColumnCount() method of AbstractTableModel.
		 * Returns integer of the number of columns in the ResultSet object
		 * after a database query.
		 */
		@Override
		public int getColumnCount() {
			return NUM_COLS;
		}

		/**
		 * Overridden getRowCount() method of AbstractTableModel.
		 * Returns the number of rows stored in the ResultSet object
		 */
		@Override
		public int getRowCount() {
			return tblData.size();
		}
		
		/**
		 * Overridden getColumnName() method of AbstractTableModel.
		 * Returns the string of the name from the instance variable array
		 * of column names, which we know to be the same throughout the 
		 * runtime of the class.
		 */
		@Override
		public String getColumnName(int i) {
			return colNames.get(i);
		}
		
		/**
		 * Overridden getValueAt() method of AbstractTableModel.
		 * Manipulates the cursor of the ResultSet object to return 
		 * the correct table value. Note that we add one to indexes
		 * because mySQL indexes from 1 rather than 0.
		 */
		@Override
		public Object getValueAt(int arg0, int arg1) {
			return tblData.get(arg0)[arg1];
		}
		
	}
	
	/*
	 * Method that uses a BufferedREader to read the url 
	 * data in the text file into the data instance variables.
	 * 
	 *  Throws exceptions if the file is not found, or if there
	 *  is a problem while reading the file.
	 */
	public void readFile() {
		 FileReader fileReader;
		try {
			fileReader = new FileReader(new File(FILENAME));
			BufferedReader br = new BufferedReader(fileReader);

			 String line = null;
			 // if no more lines the readLine() returns null
			 try {
				while ((line = br.readLine()) != null) {
					String[] row = new String[NUM_COLS];
					row[0] = line;
					row[1] = "";
					
					tblData.add(row);
				 }
			} catch (IOException e) {
				System.out.println("File Reading Problem");
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e1) {
			System.out.println("File Not Found.");
			e1.printStackTrace();
		}
		
		latch = new CountDownLatch(tblData.size());

		 
	}
	
	// ----------------------------------- Main Methods ------------------------------ //
	
	/*
	 * General method that initializes and displays the GUI throughout the 
	 * life of the frame. See IntializeGUI() for details.
	 */
	public static void createAndShowGUI() {
		// GUI Look And Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		frame = new WebFrame();
		frame.add(frame.panel, BorderLayout.NORTH);
		frame.add(frame.bottomPanel1, BorderLayout.CENTER);
		frame.add(frame.bottomPanel2, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	/*
	 * Main method, protects the GUI from threading with invokeLater. 
	 */
	public static void main(String[] args){
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});				
	}

}
