package assign4;

/**
 * Account class:
 * 
 * Class designed to hold account information for a bank simulation.
 * 
 * Note that all setter methods are synchronized, which allows the
 * client to use multi-threading methods to access these objects 
 * without fear of data corruption.
 * 
 *  Getter methods do not use synchronized protection; this is 
 *  because we wish to allow the client to access the data 
 *  simultaneously, just not corrupt the data by changing it.
 *
 */
public class Account {

	private int idNum;
	private int balance;
	private int transactions;
	
	public Account(int id){
		idNum = id;
		balance = 1000;
		transactions = 0;
	}
	
	public synchronized void deposit(int dep) {
		balance += dep;
		transactions++;
	}
	
	public synchronized void withdraw(int withdrawal) {
		balance -= withdrawal;
		transactions++;
	}
	
	/**
	 * Overrides the to String method to allow us to print out all of the 
	 * account information and hide the complexity here.
	 */
	@ Override
	public String toString() {
		String str = "Acct: " + idNum + "; ";
		str += "Bal: " + balance + "; ";
		str += "Trans: " + transactions;
		return str;
	}
	
	
	// ---------------- Getter Methods ---------------- //
	
	public int getID() {
		return idNum;
	}
	
	public int getBalance() {
		return balance; 
	}
	
	public int getTransactions() {
		return transactions;
	}
	

}
