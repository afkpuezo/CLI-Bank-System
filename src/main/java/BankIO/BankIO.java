/**
 * This interface defines what is needed for input/output for the banking program.
 * 
 * @author Andrew Curry
 */
package BankIO;

import java.util.List;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.UserProfile;

import bankSystem.Request;
import bankSystem.Request.RequestType;

public interface BankIO {

	/**
	 * Displays the given text to the user.
	 * @param text
	 */
	public void displayText(String text);
	
	/**
	 * Displays the given text to the user.
	 * @param text
	 * @param frame : if true, frame the text with a box
	 */
	public void displayText(String text, boolean frame);
	
	/**
	 * Displays information about the given user profile(s) to the user.
	 * @param up
	 */
	public void displayUserProfiles(List<UserProfile> users);
	
	/**
	 * Displays information about the given bank accounts to the user.
	 * @param accounts
	 */
	public void displayBankAccounts(List<BankAccount> accounts);
	
	/**
	 * Displays information about the given transaction records to the user.
	 * @param accounts
	 */
	public void displayTransactionRecords(List<TransactionRecord> transactions);
	
	/**
	 * Returns a Request object based on the user's responding input.
	 * @param permittedRequestTypes : the user chooses one of these
	 * @return Request
	 */
	public Request prompt(RequestType[] permittedRequestTypes);
	
	/**
	 * Called by the BankSystem when execution is ending. Allows for cleanup.
	 */
	public void close();
}
