package bank.business.domain;

/**
 * @author Ingrid Nunes
 * 
 */
public class Deposit extends Transaction {

	private long envelope;
	private STATUS status;
	
	public Deposit(OperationLocation location, CurrentAccount account, long envelope, double amount, STATUS status) {
		super(location, account, amount);
		this.envelope = envelope;
		this.status = status;
	}
	

	/**
	 * @return the envelope
	 */
	public long getEnvelope() {
		return envelope;
	}
	
	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}
}
