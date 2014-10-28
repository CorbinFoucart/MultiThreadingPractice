package assign4;

public class Transaction {

	private final int to;
	private final int from;
	private final double amt;
	
	public Transaction(int to, int from, int amt) {
		this.to = to;
		this.from = from;
		this.amt = amt;
	}
	
	public int getTo() {
		return to;
	}
	
	public int getFrom() {
		return from;
	}
	
	public double getAmt() {
		return amt;
	}
	
}
