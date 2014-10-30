package assign4;

/**
 * Transaction class
 * 
 * This class is immutable so that it may support multi-threading
 * client activities. Essentially contains 3 protected pieces of
 * data:
 * 
 * TO: the account number to which an amount will be transfered
 * FROM: the account number from where the amount is being transfered
 * AMT: the amount that is being transfered.
 * 
 * All of this is pretty self-explanatory.
 *
 */
public class Transaction {

	private final int to;
	private final int from;
	private final int amt;
	
	public Transaction(int from, int to, int amt) {
		this.from = from;
		this.to = to;
		this.amt = amt;
	}
	
	/**
	 * Override the .equals() method; allows clients to
	 * determine whether transactions are equal.
	 * 
	 * Transactions are equal if they have the same to, from
	 * accounts and transfer the same amount. 
	 */
	public boolean equals(Transaction other) {
		return (     to == other.getTo() 
				&& from == other.getFrom()
				&& amt  == other.getAmt());
	}
	
	// ------------------------ getters ----------------------- // 
	public int getTo() {
		return to;
	}
	
	public int getFrom() {
		return from;
	}
	
	public int getAmt() {
		return amt;
	}
	
}
