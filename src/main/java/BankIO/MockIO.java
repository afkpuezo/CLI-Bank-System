/**
 * A mock implementation of the IO interface used for testing the BankSystem.
 * It can be preloaded with Requests to give back when prompted, 
 * and it caches any output from the BS.
 */
package BankIO;

import java.util.ArrayList;
import java.util.List;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.UserProfile;

import bankSystem.Request;
import bankSystem.Request.RequestType;

public class MockIO implements BankIO {
	
	// instance variables
	private Request nextRequest;
	private List<Object> cachedOutput;

	public MockIO() {
		resetCachedOutput();
	}
	
	public void setNextRequest(Request nextRequest) {
		this.nextRequest = nextRequest;
	}

	public void resetCachedOutput() {		
		cachedOutput = new ArrayList<>();
	}
	
	public List<Object> getCachedOutput() {
		return cachedOutput;
	}

	@Override
	public void displayText(String text) {
		cachedOutput.add(text);

	}

	@Override
	public void displayText(String text, boolean frame) {
		
		//System.out.println("DEBUG: displayText called");
		cachedOutput.add(text);
	}

	@Override
	public void displayUserProfiles(List<UserProfile> users) {

		for (UserProfile up : users) {
			cachedOutput.add(up);			
		}
	}

	@Override
	public void displayBankAccounts(List<BankAccount> accounts) {
		
		for (BankAccount ba : accounts) {
			cachedOutput.add(ba);			
		}
	}

	@Override
	public void displayTransactionRecords(List<TransactionRecord> transactions) {

		for (TransactionRecord tr : transactions) {
			cachedOutput.add(tr);			
		}
	}

	@Override
	public Request prompt(RequestType[] permittedRequestTypes) {
		
		return nextRequest;
	}
	
	/**
	 * Called by the BankSystem when execution is ending. Allows for cleanup.
	 */
	public void close() {
		
	}
}
