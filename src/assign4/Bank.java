package assign4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.*;

public class Bank {
	
	// how many pieces of data are specified per line in data file
	private static final int NUM_DATA = 3;
	private static final int NUM_ACCTS = 20;
	private final static Transaction nullTrans = new Transaction(-1,0,0);
	private static ArrayBlockingQueue<Transaction> transQueue;
	private static ArrayList<Worker> workers = new ArrayList<Worker>();
	private static Account[] accounts;
	private static int numThreads;
	private static String filename;
	private static CountDownLatch latch;
	
	public static void main(String[] args) {
		
		// initialize ivars
		filename = args[0].toString();
		numThreads = Integer.parseInt(args[1]);
		transQueue = new ArrayBlockingQueue<Transaction>(numThreads);
		latch = new CountDownLatch(numThreads);
		
		// initialize accounts
		accounts = new Account[NUM_ACCTS];
		for (int i = 0; i < NUM_ACCTS; i++) {
			Account current = new Account(i);
			accounts[i] = current;
		}
			
		// create worker threads, start them 
		for (int i = 0; i < numThreads; i++) {
			Worker w  = new Worker();
			w.start();
			workers.add(w);
		}	
		
		// add transactions to the queue whenever we can
		// with the main thread
		readFile();
		
		//print out the results
		printAccounts();
		
	}
	
	
	// Helper method to main separated for organization
	// main thread chugs along, trying to add transactions to the queue
	public static void readFile() {	 
		// use a bufferedReader to read the transactions into the queue
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("assign4/" + filename));
			String line;
			while ((line = br.readLine()) != null) {
				int[] transData = new int[NUM_DATA];
				StringTokenizer st = new StringTokenizer(line);
				int count = 0;
			     while (st.hasMoreTokens()) {
			    	transData[count] = Integer.parseInt(st.nextToken());
			    	count++;			         
			    }
			    Transaction tr = new Transaction(transData[0], transData[1], transData[2]);
			    try {
					transQueue.put(tr);
				} catch (InterruptedException e) {
					System.out.println("Interrupted");
				}
//					    System.out.println(line);
			}
			br.close(); 
			
			// put as many null transactions in the queue as there are threads
			for (int j = 0; j < numThreads; j++){
				try {
					transQueue.put(nullTrans);	
				} catch (InterruptedException e) {
					System.out.println("Couldn't add nullTrans");
				}
//						System.out.println("Sucessfully Exited Main");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File not found.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// print out the sordid details of this entire mess
	public static void printAccounts() {
		System.out.println("\n");
		for (int i = 0; i < accounts.length; i++) {
			System.out.println(accounts[i].toString());
		}
		System.out.println("\n");
	}
	
	
	/**
	 * Worker class, run method allows the thread to complete
	 * transactions after removing them from the blocking
	 * queue. 
	 * 
	 * Worker completes when it receives the nullTrans transaction
	 * specified in the handout. At completion, it uses the CountDownLatch
	 * to count down as a commuication mechanism with the main method.
	 */
	public static class Worker extends Thread{
		
		public void run() {
			try {
				
				Transaction workerTrans = transQueue.take();
				while (!workerTrans.equals(nullTrans)) {
					
					// get relevant information from each account
					int fromAcct = workerTrans.getFrom();
					int toAcct = workerTrans.getTo();
					int transAmt = workerTrans.getAmt();
					
//					System.out.println("Transaction Complete");
					
					// perform the 'transaction'
					accounts[fromAcct].withdraw(transAmt);
					accounts[toAcct].deposit(transAmt);
					workerTrans = transQueue.take();
				}
				latch.countDown();
//				System.out.println("Successfully Counted Down");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	 
	}

}
