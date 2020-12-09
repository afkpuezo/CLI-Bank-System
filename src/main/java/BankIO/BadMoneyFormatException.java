package BankIO;

public class BadMoneyFormatException extends Exception {

	static final long serialVersionUID = 0; // dunno, this fixed a warning
	
	public BadMoneyFormatException(String message) {
		super(message);
	}
}
