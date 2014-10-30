package assign4;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class JCount extends JPanel{
	
	
	private JPanel panel;
	private JLabel display;
	private JTextField tf;
	private JButton start;
	private JButton stop;
	private WorkerThread worker;	
	
	
	public JCount() {
		initializeJPanel();
		addListeners();
		
	}
	
	public void initializeJPanel() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		tf = new JTextField();
		tf.setPreferredSize(new Dimension(150,30));
		tf.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(tf);

		display = new JLabel();
		display.setText("0");
		display.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(display);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		
		start = new JButton("Start");
		start.setPreferredSize(new Dimension(150,30));
		start.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(start);		
		
		stop = new JButton("Stop");
		stop.setPreferredSize(new Dimension(150,30));
		stop.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(stop);
		panel.add(Box.createRigidArea(new Dimension(0,40)));	
				
	}
	
	public void addListeners() {
		
		// Start Button Pressed
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String input = tf.getText();
//				System.out.println(input);
				
				try{
					int newGoal = Integer.parseInt(input);
					if (worker == null) {	
						worker = new WorkerThread(0, newGoal);
						worker.start();
					}else {
						worker.interrupt();
						worker = null;
						worker = new WorkerThread(0, newGoal);
						worker.start();
					}					
				}catch (NumberFormatException ex) {
					System.out.println("Please enter a value.");
				}
				
				
				
				
			}
		});
		
		// Stop Button Pressed
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.interrupt();
					worker = null;
				}
				
				display.setText("0");
			}
		});
	}
	
	/**
	 * WorkerThread Class:
	 * 
	 * Executes  
	 *
	 */
	public class WorkerThread extends Thread{
		
		private int count;
		private int goal;		
		
		public WorkerThread(int count, int goal) {
			this.count = count;
			this.goal = goal;
		}
		
		public void run() {
			while(count < goal) {
				// interruption handline
				if (interrupted()) {
					count = 0;
					break;
				}
				
				count++;
				
				if (count % COUNT_CONST == 0){
					try {
						sleep(100);
						String cntProg = Integer.toString(count);
						display.setText(cntProg);
						
					} catch (InterruptedException e) {
						System.out.println("Needed to use this catch");
						count= 0; 
						break;						
					}					
				}				
			}
		}
	}
		
	
	public static final int NUM_PANELS = 4;
	public static final int COUNT_CONST = 10000;
	
	// ------------------------------------ Main ---------------------------------- //
	
	public static void main(String[] args){
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});				
	}
	
	private static void createAndShowGUI() {
		// GUI Look And Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JFrame frame = new JFrame("JCount");
		frame.setLayout(new GridLayout(NUM_PANELS, 1));	
		
		for (int i = 0; i < NUM_PANELS; i++) {
			JCount counter = new JCount();
			counter.worker = counter.new WorkerThread(0,0);
			frame.add(counter.panel);
		}
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}
	
}
