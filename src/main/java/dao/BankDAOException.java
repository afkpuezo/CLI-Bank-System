/**
 * Classes implementing the BankDAO interface will throw this exception
 * when they encounter issues communicating with the data storage.
 * This is for when they can't find the database, NOT when the user attempts
 * to access information they don't have permission to see, and NOT when the
 * system queries an account that doesn't exist, etc.
 */
package dao;

public class BankDAOException extends Exception {
	
	static final long serialVersionUID = 0; // dunno, this fixed a warning
	
	public BankDAOException (String message) {
		super(message);
	}
}
