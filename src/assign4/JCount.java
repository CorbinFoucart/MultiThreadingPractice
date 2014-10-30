package assign4;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class JCount extends JPanel{
	
	// GUI ivars
	private JPanel panel;
	private JLabel display;
	private JTextField tf;
	private JButton start;
	private JButton stop;
	private WorkerThread worker;	
	
	// CONSTANTS
	public static final int NUM_PANELS = 4;
	public static final int COUNT_CONST = 10000;
	
	// JCount ctor
	public JCount() {
		initializeJPanel();
		addListeners();
	}
	
	/*
	 * General method to initialie the GUI, involves building each 
	 * Count gui component, namely a texfield, counting display, 
	 * and the start and stop buttons.
	 */
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
	
	/*
	 * Methods that handle the swing actionListeners for each
	 * JCount GUI. The start and stop button use listeners.
	 */
	public void addListeners() {
		
		// Start Button Pressed
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String input = tf.getText();
				
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
					tf.setText("Enter an int value.");
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
	 * Executes the counting step by forking off a worker that
	 * performs the couting independently without stopping the user's
	 * interaction with the GUI. 
	 * 
	 * When a multiple of COUNT_CONST is reached, the thread waits 
	 * and then communicates the change to the GUI so that the user 
	 * may continue to use it.
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
				// interruption handling
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
	
	// ------------------------------------ Main ---------------------------------- //
	
	// main method, protects GUI from threads with invokeLater
	public static void main(String[] args){
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});				
	}
	
	/*
	 * More general GUI creation method. Note that it creates
	 * a certain number of JCount objects and glues them together
	 * in a modular way to make the whole GUI.
	 */
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
