package assign4;

public class Account {

	private int idNum;
	private double balance;
	private int transactions;
	
	public Account(int id){
		idNum = id;
		balance = 0;
		transactions = 0;
	}
	
	public void deposit(double dep) {
		balance += dep;
		transactions++;
	}
	
	public void withdraw(double withdrawal) {
		balance -= withdrawal;
		transactions++;
	}
	
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
	
	public double getBalance() {
		return balance; 
	}
	
	public int getTransactions() {
		return transactions;
	}
	

}
