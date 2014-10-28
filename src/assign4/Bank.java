package assign4;

public class Bank {
	
	//private final Transaction nullTrans = new Transaction(-1,0,0);
	
	public static void main(String[] args) {
		Account a = new Account(1);
		System.out.println(a.toString());
		
		a.deposit(50);
		System.out.println(a.toString());
		
		a.withdraw(25);
		System.out.println(a.toString());
		
		
		
	}
	
	class BankWorker extends Thread{
		
		public void run() {
			
		}
		
	}
}
