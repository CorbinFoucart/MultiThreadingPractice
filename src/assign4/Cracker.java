package assign4;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Cracker {

	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();
	public static ArrayList<crackWorker> workers = new ArrayList<crackWorker>();
	private static int passMaxLength;
	private static String hashCode;
	private static byte[] hashBytes;
	private static int numThreads;
	private static ArrayList<String> Solutions = new ArrayList<String>();
	private static CountDownLatch latch;
	
	public static void main(String[] args) {
		
		System.out.println(args[0]);
		System.out.println(args.length);
		
		// Generation mode
		if (args.length ==1) {
			 String printHash = generateHash(args[0]);
			 System.out.println(printHash);
			 System.out.println("\n");
		}else {
			try {
				hashCode = args[0];
				hashBytes = hexToArray(hashCode);
				passMaxLength = Integer.parseInt(args[1]);
				numThreads = Integer.parseInt(args[2]);
				latch = new CountDownLatch(numThreads);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Incorrect input");
			}
			
			// divide the char string up between threads and initialize them
			divideLabor();
			
			// start the threads
			startThreads();
			
			if (latch.getCount() == 0) {
				printResults();
			}
			System.out.println("\n");
		}
	}
	
	/*
	 * Helper method that reports the the results 
	 * of the recursive search. Prints an extra \n
	 * char because I thought the terminal looked 
	 * cluttered otherwise.
	 */
	public static void printResults() {
		if (Solutions.isEmpty()) {
			System.out.println("No solution found. \n");
		}else  {
			for (int i = 0; i < Solutions.size(); i++) {
				String soln = Solutions.get(i);
				System.out.println("Solutions found: ");
				System.out.println(soln);
			}
			System.out.println("all done.");
			System.out.println("\n");
		}
		
	}
	
	// simple method to launch a certain number of threads.
	public static void startThreads() {
		for (int i = 0; i < numThreads; i++){
			workers.get(i).run();
		}
	}
	 
	/*
	 * Uses a simple iterative method to evenly distribute the character set
	 * amongst our n worker threads. the remainder is split among the first
	 * few threads, if the number of threads does not divide the character 
	 * set length evenly. 
	 */
	public static void divideLabor() {
		// allocate indexes for each worker
		int spacer = CHARS.length / numThreads;		
		int remainder = CHARS.length % numThreads;		
		
		int count = 0;
		int end = 0;
		int i = 0;
		while(i < numThreads) {
			end = count + spacer - 1;
			if (remainder > 0) {
				end++;
				remainder--;
			}
			crackWorker worker = new crackWorker(count, end);
			workers.add(worker);
			
//			// debugging
//			System.out.println(worker.startIndex);
//			System.out.println(worker.endIndex);
//			System.out.println("\n");
			
			count = end + 1;
			i++;
		}
	}
	
	/*
	 * Our worker class that executes a recursive alphabetical
	 * search, creating all possible strings in the given alphabetical
	 * range and checking them against the hashCode ivar.
	 * 
	 * See generateCombos for more details.
	 */
	public static class crackWorker extends Thread{
		private int startIndex;
		private int endIndex;
		
		public crackWorker(int start, int end) {
			startIndex = start;
			endIndex = end;
		}
		
		// run method
		public void run() {
			for (int len = 1; len <= passMaxLength; len++){
				generateCombos(len, startIndex, endIndex);
			}
			latch.countDown();
		}
	}
	
	/** 
	 *  wrapper for recursive function that adds all combinations
	 *  from the character set of length n to an arraylist. 
	 *  
	 *  I had previously seen this article, and used a similar implementation
	 *  http://www.geeksforgeeks.org/print-all-combinations-of-given-length/
	 *  
	 *  n denotes finding thrings of length n. Since we provide the first letter, 
	 *  we pass in n-1.
	 *  
	 */
	public static void generateCombos(int n, int start, int end) {
		for (int i = start; i <= end; i++) {
			String str = "" + CHARS[i];
			addAllCombos(str, n-1);
		}
		
		
	}
	
	/*
	 * The brains behind the recursive method wrapped by generateCombos().
	 * If there are no more characters, it knows it has built a complete
	 * string, which it passes to checkHash in order to generate a hash value
	 * and check it against our saved HashCode.
	 */
	public static void addAllCombos(String previous, int len) {
		if (len == 0) {
			if (checkHash(previous)) Solutions.add(previous);
//			System.out.println(previous);
		}else {
			for (int i = 0; i < CHARS.length; i++) {
				String next = previous + CHARS[i];
				addAllCombos(next, len - 1);
			}
		}
	}
	
	/*
	 * Uses the java.security libraries to create a 
	 * string hash value from an input string using
	 * SHA algorithm.
	 */
	public static String generateHash(String str) {
		try {
			 MessageDigest md = MessageDigest.getInstance("SHA");
			 hashBytes = md.digest(str.getBytes());
			 String hash = hexToString(hashBytes);
//			 System.out.println(hash);
			 return hash;
		 } catch (NoSuchAlgorithmException cnse) {
		     cnse.printStackTrace();
		 }
		return "";
	}
	
	/*
	 * Returns a boolean corresponding to whether or not 
	 * the input string corresponds to our hashCode saved
	 * as an ivar. Compares the two as byte[]s for speed
	 * rather than converting and checking the strings
	 * for equality.
	 */
	public static boolean checkHash(String str) {
		try {
			 MessageDigest md = MessageDigest.getInstance("SHA");
			 byte[] checkHashBytes = md.digest(str.getBytes());
			 if (Arrays.equals(checkHashBytes, hashBytes)) return true;
		 } catch (NoSuchAlgorithmException cnse) {
		     cnse.printStackTrace();
		 }
		return false;
	}
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3

}
