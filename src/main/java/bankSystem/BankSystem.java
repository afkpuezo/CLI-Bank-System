/**
 * This class controls the operation of the banking system. It gets user Requests from the IO, and resolves
 * them by interacting with the DAO.
 * 
 * There is no corresponding interface because the other parts of the system do not need to know anything
 * about the BankSystem (currently, anyway).
 * 
 * @author Andrew Curry
 */
package bankSystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.TransactionRecord.TransactionType;
import com.revature.bankDataObjects.UserProfile;
import com.revature.bankDataObjects.BankAccount.BankAccountStatus;
import com.revature.bankDataObjects.BankAccount.BankAccountType;
import com.revature.bankDataObjects.BankData;
import com.revature.bankDataObjects.UserProfile.UserProfileType;

import BankIO.BankIO;
import bankSystem.Request.RequestType;
import dao.BankDAO;
import dao.BankDAOException;


public class BankSystem {

	// class/static variables
	private static Logger log = Logger.getLogger(BankSystem.class);
	
	/**
	 * ----------------------------------------------------------------------
	 * These strings are tags used in certain requests to describe what kind
	 * of ID is being passed in the params list. They are public so that the
	 * IO can access them when creating requests.
	 */
	public static final String USER_PROFILE_TAG = "PRF";
	public static final String ACCOUNT_TAG = "ACC";
	public static final String TRANSACTION_TAG = "TRR";
	
	/**
	 * ----------------------------------------------------------------------
	 * These Strings are public for the purpose of testing.
	 * Since they are final, I figure there is minimal risk of damage.
	 */
	
	public static final String START_MESSAGE 
			= "Welcome to the bank!";
	public static final String NO_USER_LOGGED_IN_MESSAGE 
			= "-No user logged in-";
	public static final String USER_LOGGED_IN_PREFIX
			= "Currently logged in as: "; // should append username
	public static final String USERNAME_IN_USE_MESSAGE 
			= "Unable to proceed: That username is already in use.";
	public static final String USER_DOES_NOT_EXIST_MESSAGE 
			= "Unable to proceed: No user profile with that name exists.";
	public static final String GENERIC_DAO_ERROR_MESSAGE 
			= "ALERT: There were issues communicating with the database. Contact your system administrator.";
	public static final String LOG_IN_SUCCESSFUL_PREFIX
			= "Successfully logged in as: ";
	public static final String LOGIN_USER_NOT_FOUND_PREFIX 
			= "Unable to proceed: No profile found matching username: ";
	public static final String LOGIN_INVALID_PASSWORD_MESSAGE 
			= "Unable to proceed: Incorrect password.";
	public static final String LOGOUT_MESSAGE = "Logged out.";
	public static final String QUIT_MESSAGE = "Quit.";
	public static final String LOST_CONNECTION_UNRECOVERABLE_MESSAGE 
			= "Connection to database lost; quitting application.";
	public static final String ACCOUNT_DOES_NOT_EXIST_PREFIX 
			= "Unable to proceed: No account exists with ID: ";
	public static final String USER_ID_NOT_FOUND_PREFIX // there's like 3 of these 
			= "Unable to proceed: No user exists with ID: ";
	public static final String USER_ID_MULTIPLE_NOT_FOUND_PREFIX // there's like 4 of these 
			= "Could not find user profiles for the following IDs: ";
	public static final String TRANSACTION_RECORD_NOT_SAVED_MESSAGE
			= "ALERT: The transaction was carried out, but there was a problem adding it to the log";
	public static final String GENERIC_NO_PERMISSION_MESSAGE
			= "Unable to proceed: You do not have permission to take that action.";
	public static final String USER_REGISTERED_MESSAGED
			= "New user profile registered.";
	public static final String ACCOUNT_NOT_OPEN_PREFIX
			= "Unable to proceed: Bank account is not open. Account ID: ";
	
	public static final String APPLY_OPEN_ACCOUNT_NOT_CUSTOMER_MESSAGE 
			= "Unable to proceed: Only customers can apply to open accounts";
	public static final String APPLY_OPEN_ACCOUNT_MESSAGE
			= "Account created, pending approval.";
	public static final String APPROVE_OPEN_ACCOUNT_NO_PERMISSION_MESSAGE
			= "Unable to proceed: Only employees and administrators can approve accounts.";
	public static final String APPROVE_OPEN_ACCOUNT_MESSAGE
			= "Account created, pending approval.";
	public static final String BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX
			= "Unable to proceed: No account exists with ID: ";
	public static final String BANK_ACCOUNT_NOT_PENDING_MESSAGE
			= "Unable to proceed: That account is not pending approval.";
	public static final String ACCOUNT_APPROVED_MESSAGE
			= "Account approved.";
	public static final String ACCOUNT_DENIED_MESSAGE
			= "Account denied.";
	
	public static final String CLOSE_ACCOUNT_NO_PERMISSION_MESSAGE
			= "Unable to proceed: Only an administrator can close an account.";
	public static final String CLOSE_ACCOUNT_NOT_OPEN_MESSAGE
			= "Unable to proceed: That account cannot be closed because it is not open.";
	public static final String CLOSE_ACCOUNT_MESSAGE
			= "Account closed. All funds withdrawn and returned to account owner(s).";
	
	public static final String ADD_OWNER_TO_ACCOUNT_MESSAGE
			= "New owner successfully added to account";
	public static final String ADD_OWNER_CUSTOMER_NOT_OWN_ACCOUNT_MESSAGE
			= "Unable to proceed: You do not have permission to add users to an account you do not own.";
	public static final String ADD_OWNER_NEW_USER_NOT_CUSTOMER_MESSAGE
			= "Unable to proceed: That user cannot be added to this account because they are not a customer."; 
	public static final String ADD_OWNER_ALREADY_OWNED_MESSAGE
			= "Unable to proceed: That user cannot be added to this account because they are already an owner of the account.";
	public static final String ADD_OWNER_ACCOUNT_NOT_OPEN_MESSAGE
			= "Unable to proceed: You cannot add an owner to that account because that account is not open.";
	
	
	public static final String REMOVE_OWNER_SUCCESSFUL_MESSAGE
			= "User has successfuly been removed from that account.";
	public static final String REMOVE_OWNER_CUSTOMER_NOT_OWN_ACCOUNT_MESSAGE
			= "Unable to proceed: You do not have permission to remove users from an account you do not own.";
	public static final String REMOVE_OWNER_TARGET_NOT_OWNER
			= "Unable to proceed: The user you are trying to remove is not an owner of that account.";
	public static final String REMOVE_OWNER_OPEN_ONLY_ONE_OWNER
			= "Unable to proceed: You cannot remove the last owner of an open account." 
			+ " Contact support and have the account closed first.";
	public static final String REMOVE_OWNER_CUSTOMER_CAN_ONLY_REMOVE_THEMSELF_MESSAGE
			= "Unable to proceed: A customer cannot remove another customer. Have the other customer remove themselves,"
			+ "or contact support for assistance.";
	public static final String REMOVE_OWNER_ACCOUNT_NOT_OPEN_MESSAGE
			= "Unable to proceed: You cannot remove an owner from that account because that account is not open.";
	
	public static final String DEPOSIT_SUCCESSFUL_MESSAGE 
			= "Deposit successful.";
	public static final String DEPOSIT_ACCOUNT_NOT_OWNED_MESSAGE
			= "Unable to proceed: You cannot deposit to an account you do not own. Use a transfer instead.";
	public static final String DEPOSIT_ACCOUNT_NOT_OPEN_MESSAGE
			= "Unable to proceed: You cannot deposit to an account that is not open.";
	
	public static final String WITHDRAW_SUCCESSFUL_MESSAGE 
			= "Withdraw successful.";
	public static final String WITHDRAW_ACCOUNT_NOT_OWNED_MESSAGE
			= "Unable to proceed: You cannot withdraw from an account you do not own. Use a transfer instead.";
	public static final String WITHDRAW_ACCOUNT_NOT_OPEN_MESSAGE
			= "Unable to proceed: You cannot withdraw from an account that is not open.";
	public static final String WITHDRAW_OVERDRAFT_BLOCK_MESSAGE
			= "Unable to proceed: There are insufficient funds in that account.";
	
	public static final String TRANSFER_SUCCESSFUL_MESSAGE 
			= "Transfer successful.";
	public static final String TRANSFER_SOURCE_ACCOUNT_NOT_OWNED_MESSAGE
			= "Unable to proceed: You cannot transfer from an account you do not own.";
	public static final String TRANSFER_OVERDRAFT_BLOCK_MESSAGE
			= "Unable to proceed: There are insufficient funds in the source account.";
	
	public static final String VIEW_ACCOUNTS_NO_PERMISSION_PREFIX
			= "You do not have permission to look at the following accounts: ";
	public static final String VIEW_ACCOUNTS_NOT_FOUND_PREFIX
			= "The following accounts were not found: ";
	
	public static final String VIEW_TRANSACTIONS_CUSTOMER_CAN_ONLY_VIEW_SELF_MESSAGE
			= "Unable to proceed: A customer can only see transactions caused by themself, " 
			+ "or affecting accounts they own.";
	public static final String VIEW_TRANSACTIONS_INVALID_IDS_PREFIX
			= "The following transactions were not found: ";
	public static final String VIEW_TRANSACTIONS_NONPERMITTED_IDS_PREFIX
			= "You do not have permission to view the following transactions: ";
	
	public static final String CREATE_EMPLOYEE_SUCCESSFUL_PREFIX
			= "Employee account created, with ID: ";
	
	// arrays of permitted request types -----------------------------------------
	
	private static final RequestType[] NO_USER_CHOICES = 
			{RequestType.LOG_IN, RequestType.REGISTER_USER, RequestType.QUIT};
	
	private static final RequestType[] CUSTOMER_CHOICES_NO_ACCOUNTS = 
			{RequestType.APPLY_OPEN_ACCOUNT, RequestType.LOG_OUT, RequestType.QUIT};
	private static final RequestType[] CUSTOMER_CHOICES_HAS_ACCOUNT =
			{RequestType.VIEW_SELF_PROFILE, RequestType.VIEW_ACCOUNTS, RequestType.DEPOSIT, 
			RequestType.WITHDRAW, RequestType.TRANSFER, RequestType.VIEW_TRANSACTIONS, 
			RequestType.ADD_ACCOUNT_OWNER, RequestType.REMOVE_ACCOUNT_OWNER,RequestType.APPLY_OPEN_ACCOUNT, 
			RequestType.LOG_OUT, RequestType.QUIT};
	
	private static final RequestType[] EMPLOYEE_CHOICES = 
			{RequestType.VIEW_SELF_PROFILE,RequestType.VIEW_ACCOUNTS, RequestType.VIEW_USERS, 
			RequestType.APPROVE_OPEN_ACCOUNT, RequestType.DENY_OPEN_ACCOUNT, RequestType.WITHDRAW, 
			RequestType.DEPOSIT, RequestType.TRANSFER, RequestType.VIEW_TRANSACTIONS, 
			RequestType.LOG_OUT, RequestType.QUIT};
	
	private static final RequestType[] ADMIN_CHOICES =
			{RequestType.VIEW_SELF_PROFILE, RequestType.VIEW_ACCOUNTS, RequestType.VIEW_USERS, 
			RequestType.APPROVE_OPEN_ACCOUNT, RequestType.DENY_OPEN_ACCOUNT, RequestType.WITHDRAW, 
			RequestType.DEPOSIT, RequestType.TRANSFER, RequestType.VIEW_TRANSACTIONS, 
			RequestType.CLOSE_ACCOUNT, RequestType.CREATE_EMPLOYEE, RequestType.CREATE_ADMIN, 
			RequestType.LOG_OUT, RequestType.QUIT};
	
	// instance variables (fields)
	private BankIO io;
	private BankDAO dao;
	
	private UserProfile currentUser; // who is logged in?
	private boolean running; // controls interaction loop
	
	// constructor(s)
	
	/**
	 * Note: the DAO should already be hooked up to the target data store.
	 * @param io
	 * @param dao
	 */
	public BankSystem(BankIO io, BankDAO dao) {
		
		this.io = io;
		this.dao = dao;
		
		currentUser = getEmptyUser();
		running = false;
	}
	
	// 'operation' methods
	
	/**
	 * Called by the driver to start operation of the system.
	 */
	public void start() {
		
		io.displayText(START_MESSAGE, true);
		running = true;
		interactionLoop();
	}
	
	/**
	 * Should only be called during unit testing.
	 * Calls the interactionLoop method, but does not set running to true.
	 * Thus, the loop only happens once.
	 */
	public void testLoop() {
		
		interactionLoop();
	}
	
	/**
	 * Prompts the user for input, and handles the resulting request.
	 */
	private void interactionLoop() {
		
		//boolean running = true;
		//String outputText = "";
		RequestType[] permittedRequestTypes; // = new RequestType[0]; // should get replaced in loop
		Request currentRequest;
		
		do {
			
			// display a header with the current user
			if (currentUser.getType() == UserProfileType.NONE) {
				io.displayText(NO_USER_LOGGED_IN_MESSAGE);
			}
			else {
				io.displayText(
						USER_LOGGED_IN_PREFIX + currentUser.getUsername() + " ID: " + currentUser.getId() + "",
						true);
			}
			
			//determine what to prompt the user with
			if (currentUser.getType() == UserProfileType.NONE) { // if no one is logged in
				permittedRequestTypes = NO_USER_CHOICES;
			}
			else if (currentUser.getType() == UserProfileType.CUSTOMER) {
				if( currentUser.getOwnedAccounts().isEmpty()) {
					permittedRequestTypes = CUSTOMER_CHOICES_NO_ACCOUNTS;
				}
				else {
					permittedRequestTypes = CUSTOMER_CHOICES_HAS_ACCOUNT;
				}
			}
			else if (currentUser.getType() == UserProfileType.EMPLOYEE) {
				permittedRequestTypes = EMPLOYEE_CHOICES;
			}
			else { //if (currentUser.getType() == UserProfileType.ADMIN) // assume admin
				permittedRequestTypes = ADMIN_CHOICES;
			}
			
			currentRequest = io.prompt(permittedRequestTypes);
			
			// now handle the request
			try {
				boolean permitted = false;
				for (RequestType rt : permittedRequestTypes) {
					if (rt == currentRequest.getType()) {
						permitted = true;
						break;
					}
				}
				
				if (!permitted) {
					// should be no way to reach this?
					log.log(Level.WARN, "User " + currentUser.getId() + " attempted forbidden action " + currentRequest.getType());
					throw new ImpossibleActionException(GENERIC_NO_PERMISSION_MESSAGE);
				}
				
				switch(currentRequest.getType()) {
				
					case REGISTER_USER:
						handleRegisterUser(currentRequest);
						break;
					case LOG_IN:
						handleLogIn(currentRequest);
						break;
					case LOG_OUT:
						handleLogOut(currentRequest);
						break;
					case QUIT:
						handleQuit(currentRequest);
						break;
					case APPLY_OPEN_ACCOUNT:
						handleApplyToOpenAccount(currentRequest);
						break;
					case APPROVE_OPEN_ACCOUNT:
						handleApproveOpenAccount(currentRequest);
						break;
					case DENY_OPEN_ACCOUNT:
						handleDenyOpenAccount(currentRequest);
						break;
					case CLOSE_ACCOUNT:
						handleCloseAccount(currentRequest);
						break;
					case ADD_ACCOUNT_OWNER:
						handleAddAccountOwner(currentRequest);
						break;
					case REMOVE_ACCOUNT_OWNER:
						handleRemoveAccountOwner(currentRequest);
						break;
					case DEPOSIT:
						handleDeposit(currentRequest);
						break;
					case WITHDRAW:
						handleWithdraw(currentRequest);
						break;
					case TRANSFER:
						handleTransfer(currentRequest);
						break;
					case VIEW_ACCOUNTS:
						handleViewAccounts(currentRequest);
						break;
					case VIEW_SELF_PROFILE:
						handleViewSelfProfile(currentRequest);
						break;
					case VIEW_USERS:
						handleViewUsers(currentRequest);
						break;
					case VIEW_TRANSACTIONS:
						handleViewTransactions(currentRequest);
						break;
					case CREATE_EMPLOYEE:
						handleCreateEmployee(currentRequest);
						break;
					case CREATE_ADMIN:
						handleCreateAdmin(currentRequest);
						break;
				}
				
				// in case something about the current user has been updated, refresh it
				currentUser = dao.readUserProfile(currentUser.getId());
			}
			catch (ImpossibleActionException e) {
				io.displayText(e.getMessage());
			}
			catch (BankDAOException e) {
				io.displayText(LOST_CONNECTION_UNRECOVERABLE_MESSAGE);
				stopRunning();
			}
			
		} while(running); // end of do-while loop
	} // end interactionLoop() method

	
	// methods for handling specific request types --------------
	

	/**
	 * Creates a new user profile, if the username is not a duplicate.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleRegisterUser(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		String username = params.get(0);
		String password = params.get(1);
		
		try {
			if (dao.isUsernameFree(username)) {
				UserProfile user = new UserProfile(dao.getHighestUserProfileID() + 1);
				user.setUsername(username);
				user.setPassword(password);
				user.setType(UserProfileType.CUSTOMER);
				dao.write(user);
				//System.out.println("DEBUG: new user was just written");
				io.displayText(USER_REGISTERED_MESSAGED);
				changeLoggedInUser(user);
				
				TransactionRecord tr = new TransactionRecord();
				tr.setType(TransactionType.USER_REGISTERED);
				//tr.setActingUser(user.getId());
				saveTransactionRecord(tr);
			}
			else { // username is taken
				throw new ImpossibleActionException(USERNAME_IN_USE_MESSAGE);
			}		
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}

	/**
	 * Logs in to the user account with the matching username, provided it exists
	 * and the password is valid.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleLogIn(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		String username = params.get(0);
		String password = params.get(1);
		
		try {
			UserProfile up = dao.readUserProfile(params.get(0));
			
			if (up.getType() == UserProfileType.NONE) { // if no matching account
				throw new ImpossibleActionException(LOGIN_USER_NOT_FOUND_PREFIX + username);
			}
			else { // account found
				if (up.getPassword().equals(password)) {
					io.displayText(LOG_IN_SUCCESSFUL_PREFIX + username);
					changeLoggedInUser(up);
				}
				else { // invalid pass
					throw new ImpossibleActionException(LOGIN_INVALID_PASSWORD_MESSAGE);
				}
			}
			
			// no transaction
		}
		catch(BankDAOException e){
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
		
	}
	
	/**
	 * Logs out of the current user.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleLogOut(Request currentRequest) throws ImpossibleActionException {
		
		io.displayText(LOGOUT_MESSAGE);;
		changeLoggedInUser(getEmptyUser());
		// no transaction
	}
	
	/**
	 * Triggers the end of execution.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleQuit(Request currentRequest) throws ImpossibleActionException {
		
		io.displayText(QUIT_MESSAGE);
		stopRunning();
		// no transaction
	}
	
	/**
	 * Allows a customer to apply to open a new account.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleApplyToOpenAccount(Request currentRequest) throws ImpossibleActionException {
		
		// unnecessary due to generic catchall
		/*
		if (currentUser.getType() != UserProfileType.CUSTOMER) {
			throw new ImpossibleActionException(APPLY_OPEN_ACCOUNT_NOT_CUSTOMER_MESSAGE);
		}
		*/
		
		try {
			BankAccount ba = new BankAccount(dao.getHighestBankAccountID() + 1);
			ba.setStatus(BankAccountStatus.PENDING);
			ba.setType(BankAccountType.SINGLE);
			ba.setFunds(0);
			ba.addOwner(currentUser.getId());
			dao.write(ba);
			currentUser.addAccount(ba.getId());
			dao.write(currentUser);
			io.displayText(APPLY_OPEN_ACCOUNT_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_REGISTERED);
			//tr.setActingUser(currentUser.getId());
			tr.setDestinationAccount(ba.getId());
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Allows an employee or admin to approve an account that is pending approval.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleApproveOpenAccount(Request currentRequest) throws ImpossibleActionException {
		
		// check permissions - only employees and admins can approve an account
		/*
		if (currentUser.getType() != UserProfileType.EMPLOYEE 
				&& currentUser.getType() != UserProfileType.ADMIN) {
			io.displayText(APPROVE_OPEN_ACCOUNT_NO_PERMISSION_MESSAGE);
		}
		*/
		
		try {
			List<String> params = currentRequest.getParams();
			int id = Integer.parseInt(params.get(0));
			
			BankAccount ba = dao.readBankAccount(id);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + id);
			}
			if (ba.getStatus() != BankAccountStatus.PENDING) {
				throw new ImpossibleActionException(BANK_ACCOUNT_NOT_PENDING_MESSAGE);
			}
			
			ba.setStatus(BankAccountStatus.OPEN);
			dao.write(ba);
			io.displayText(ACCOUNT_APPROVED_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_APPROVED);
			//tr.setActingUser(currentUser.getId());
			tr.setDestinationAccount(ba.getId());
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Allows an employee or admin to deny an account that is pending approval.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleDenyOpenAccount(Request currentRequest) throws ImpossibleActionException {
		
		// check permissions - only employees and admins can approve an account
		if (currentUser.getType() != UserProfileType.EMPLOYEE 
				&& currentUser.getType() != UserProfileType.ADMIN) {
			io.displayText(APPROVE_OPEN_ACCOUNT_NO_PERMISSION_MESSAGE);
		}
		
		try {
			List<String> params = currentRequest.getParams();
			int id = Integer.parseInt(params.get(0));
			
			BankAccount ba = dao.readBankAccount(id);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + id);
			}
			if (ba.getStatus() != BankAccountStatus.PENDING) {
				throw new ImpossibleActionException(BANK_ACCOUNT_NOT_PENDING_MESSAGE);
			}
			
			ba.setStatus(BankAccountStatus.CLOSED);
			dao.write(ba);
			io.displayText(ACCOUNT_DENIED_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_CLOSED);
			//tr.setActingUser(currentUser.getId());
			tr.setDestinationAccount(ba.getId());
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Allows an admin to close an account.
	 * NOTE: only works on open accounts.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleCloseAccount(Request currentRequest) throws ImpossibleActionException {
		
		if (currentUser.getType() != UserProfileType.ADMIN) {
			io.displayText(CLOSE_ACCOUNT_NO_PERMISSION_MESSAGE);
		}
		
		try {
			List<String> params = currentRequest.getParams();
			int id = Integer.parseInt(params.get(0));
			
			BankAccount ba = dao.readBankAccount(id);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + id);
			}
			if (ba.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(CLOSE_ACCOUNT_NOT_OPEN_MESSAGE);
			}
			
			int funds = ba.getFunds();
			ba.setFunds(0);
			ba.setStatus(BankAccountStatus.CLOSED);
			dao.write(ba);
			io.displayText(CLOSE_ACCOUNT_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_CLOSED);
			//tr.setActingUser(currentUser.getId());
			tr.setDestinationAccount(ba.getId());
			tr.setMoneyAmount(funds);
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Allows the user to add a new user owner to a bank account.
	 * The account must be open, and the current user must either be a customer who owns
	 * the account, or an employee/admin. The user being added must be a customer.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleAddAccountOwner(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		int accID = Integer.parseInt(params.get(0));
		int userToAddID = Integer.parseInt(params.get(1));
		
		if (currentUser.getType() == UserProfileType.CUSTOMER && !currentUser.getOwnedAccounts().contains(accID)) {
			throw new ImpossibleActionException(ADD_OWNER_CUSTOMER_NOT_OWN_ACCOUNT_MESSAGE);
		}
		// assume its not a NONE account
		
		try {
			BankAccount ba = dao.readBankAccount(accID);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(ACCOUNT_DOES_NOT_EXIST_PREFIX + accID);
			}
			if (ba.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(ADD_OWNER_ACCOUNT_NOT_OPEN_MESSAGE);
			}
			
			UserProfile up = dao.readUserProfile(userToAddID);
			
			if (up.getType() == UserProfileType.NONE) {
				throw new ImpossibleActionException(USER_ID_NOT_FOUND_PREFIX + accID);
			}
			
			if (up.getType() != UserProfileType.CUSTOMER) {
				throw new ImpossibleActionException(ADD_OWNER_NEW_USER_NOT_CUSTOMER_MESSAGE);
			}
			
			if (up.getOwnedAccounts().contains(accID)) {
				throw new ImpossibleActionException(ADD_OWNER_ALREADY_OWNED_MESSAGE);
			}
			
			// should finally be valid
			ba.setType(BankAccountType.JOINT);
			ba.addOwner(userToAddID);
			up.addAccount(accID);
			
			List<BankData> toWrite = new ArrayList<>();
			toWrite.add(up);
			toWrite.add(ba);
			dao.write(toWrite);
			
			io.displayText(ADD_OWNER_TO_ACCOUNT_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_OWNER_ADDED);
			//tr.setActingUser(currentUser.getId());
			tr.setSourceAccount(userToAddID);
			tr.setDestinationAccount(accID);
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Allows the user to remove a user owner a bank account.
	 * The account must be open, and the current user must either be the customer who 
	 * is being removed from the account, or an employee/admin. The user being 
	 * removed must be a customer.
	 * Can't remove the last owner of an open account.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleRemoveAccountOwner(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		int accID = Integer.parseInt(params.get(0));
		int userToRemoveID = Integer.parseInt(params.get(1));
		
		if (currentUser.getType() == UserProfileType.CUSTOMER && !currentUser.getOwnedAccounts().contains(accID)) {
			throw new ImpossibleActionException(REMOVE_OWNER_CUSTOMER_NOT_OWN_ACCOUNT_MESSAGE);
		}
		// assume its not a NONE account
		try {
			UserProfile userToRemove = dao.readUserProfile(userToRemoveID);
			BankAccount ba = dao.readBankAccount(accID);
			
			if (userToRemove.getType() == UserProfileType.NONE) {
				throw new ImpossibleActionException(USER_ID_NOT_FOUND_PREFIX + userToRemoveID);
			}
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + accID);
			}
			if (ba.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(REMOVE_OWNER_ACCOUNT_NOT_OPEN_MESSAGE);
			}
			
			if (!userToRemove.getOwnedAccounts().contains(accID)) {
				throw new ImpossibleActionException(REMOVE_OWNER_TARGET_NOT_OWNER);
			}
			
			
			if (ba.getOwners().size() == 1 && ba.getStatus() == BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(REMOVE_OWNER_OPEN_ONLY_ONE_OWNER);
			}
			if (currentUser.getType() == UserProfileType.CUSTOMER 
					&& currentUser.getId() != userToRemoveID) {
				throw new ImpossibleActionException(REMOVE_OWNER_CUSTOMER_CAN_ONLY_REMOVE_THEMSELF_MESSAGE);
			}
			
			// now we can actually do it
			userToRemove.removeAccount(accID);
			dao.write(userToRemove);
			ba.removeOwner(userToRemoveID);
			if (ba.getOwners().size() == 1) {
				ba.setType(BankAccountType.SINGLE);
			}
			dao.write(ba);
			
			io.displayText(REMOVE_OWNER_SUCCESSFUL_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.ACCOUNT_OWNER_REMOVED);
			//tr.setActingUser(currentUser.getId());
			tr.setSourceAccount(userToRemoveID); // iffy on the formatting
			tr.setDestinationAccount(accID);
			saveTransactionRecord(tr);
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
		
	}
	
	/**
	 * Increases the funds in an account.
	 * A customer can only deposit to an open account they own (use transfer instead).
	 * An employee or admin can deposit to any open account.
	 * Assumes the amount is positive (should be sanitized by IO).
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleDeposit(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		int accID = Integer.parseInt(params.get(0));
		int moneyAmount = Integer.parseInt(params.get(1));
		
		try {
			BankAccount ba = dao.readBankAccount(accID);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(ACCOUNT_DOES_NOT_EXIST_PREFIX + accID);
			}

			if (currentUser.getType() == UserProfileType.CUSTOMER 
					&& !currentUser.getOwnedAccounts().contains(accID)) {
				throw new ImpossibleActionException(DEPOSIT_ACCOUNT_NOT_OWNED_MESSAGE);
			}
			
			if (ba.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(DEPOSIT_ACCOUNT_NOT_OPEN_MESSAGE);
			}
			
			// can go ahead now
			ba.setFunds(ba.getFunds() + moneyAmount);
			dao.write(ba);
			
			io.displayText(DEPOSIT_SUCCESSFUL_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.FUNDS_DEPOSITED);
			tr.setDestinationAccount(accID);
			tr.setMoneyAmount(moneyAmount);
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Decreases the funds in an account.
	 * A customer can only withdraw from an open account they own (use transfer instead).
	 * An employee or admin can withdraw from any open account.
	 * Assumes the amount is positive (should be sanitized by IO).
	 * Cannot withdraw below zero (will block whole transaction)
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleWithdraw(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		int accID = Integer.parseInt(params.get(0));
		int moneyAmount = Integer.parseInt(params.get(1));
		
		try {
			BankAccount ba = dao.readBankAccount(accID);
			
			if (ba.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(ACCOUNT_DOES_NOT_EXIST_PREFIX + accID);
			}

			if (currentUser.getType() == UserProfileType.CUSTOMER 
					&& !currentUser.getOwnedAccounts().contains(accID)) {
				throw new ImpossibleActionException(DEPOSIT_ACCOUNT_NOT_OWNED_MESSAGE);
			}
			
			if (ba.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(DEPOSIT_ACCOUNT_NOT_OPEN_MESSAGE);
			}
			
			if (ba.getFunds() < moneyAmount) {
				throw new ImpossibleActionException(WITHDRAW_OVERDRAFT_BLOCK_MESSAGE);
			}
			
			// can go ahead now
			ba.setFunds(ba.getFunds() - moneyAmount);
			dao.write(ba);
			
			io.displayText(WITHDRAW_SUCCESSFUL_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.FUNDS_WITHDRAWN);
			tr.setDestinationAccount(accID);
			tr.setMoneyAmount(moneyAmount);
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Moves funds from one account to another.
	 * User must be a customer who owns the source (withdraw) account, or an emp/admin.
	 * Both accounts must be open.
	 * The source account must have enough money. 
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleTransfer(Request currentRequest) throws ImpossibleActionException {
		
		List<String> params = currentRequest.getParams();
		int sourceAccID = Integer.parseInt(params.get(0)); // money comes from
		int destAccID = Integer.parseInt(params.get(1)); // money goes to
		int moneyAmount = Integer.parseInt(params.get(2)); // how much?
		
		try {
			BankAccount source = dao.readBankAccount(sourceAccID);
			BankAccount dest = dao.readBankAccount(destAccID);
			
			// check the permissions
			
			if (source.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(
						ACCOUNT_DOES_NOT_EXIST_PREFIX + sourceAccID);
			}
			if (dest.getType() == BankAccountType.NONE) {
				throw new ImpossibleActionException(
						ACCOUNT_DOES_NOT_EXIST_PREFIX + destAccID);
			}
			
			// assume no NONE user
			if (currentUser.getType() == UserProfileType.CUSTOMER 
					&& !currentUser.getOwnedAccounts().contains(sourceAccID)) {
				throw new ImpossibleActionException(
						TRANSFER_SOURCE_ACCOUNT_NOT_OWNED_MESSAGE);
			}
			
			
			if (source.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(
						ACCOUNT_NOT_OPEN_PREFIX + sourceAccID);
			}
			if (dest.getStatus() != BankAccountStatus.OPEN) {
				throw new ImpossibleActionException(
						ACCOUNT_NOT_OPEN_PREFIX + destAccID);
			}
			
			// check the funds
			if (source.getFunds() < moneyAmount) {
				throw new ImpossibleActionException(TRANSFER_OVERDRAFT_BLOCK_MESSAGE);
			}
			
			// should finally be good
			source.setFunds(source.getFunds() - moneyAmount);
			dest.setFunds(dest.getFunds() + moneyAmount);
			
			List<BankData> toWrite = new ArrayList<>();
			toWrite.add(source);
			toWrite.add(dest);
			dao.write(toWrite);
			
			io.displayText(TRANSFER_SUCCESSFUL_MESSAGE);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.FUNDS_TRANSFERRED);
			tr.setSourceAccount(sourceAccID);
			tr.setDestinationAccount(destAccID);
			tr.setMoneyAmount(moneyAmount);
			saveTransactionRecord(tr);
		}
		catch(BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Sends a set of accounts to the IO for display.
	 * The request either contains the account IDS directly,
	 * or a userID, in which case all accounts owned by that user are returned.
	 * A customer can only view accounts they own.
	 * If any account IDs are invalid, a separate display message will follow
	 * containing a list of them.
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleViewAccounts(Request currentRequest) throws ImpossibleActionException {
		
		//System.out.println("DEBUG: handleViewAccounts called");
		try {
			List<Integer> lookupIDs = new ArrayList<>();
			List<String> params = currentRequest.getParams();
			
			
			// which kind of request / params format is it?
			if (params.get(0).equals(USER_PROFILE_TAG)) {
				// get all accounts owned by this user
				int ownerID = Integer.parseInt(params.get(1));
				UserProfile owner = dao.readUserProfile(ownerID);
				
				// make sure this user actually exists - can this be reached?
				if (owner.getType() == UserProfileType.NONE) {
					throw new ImpossibleActionException(USER_ID_NOT_FOUND_PREFIX + ownerID);
				}
				
				for (int accID : owner.getOwnedAccounts()) {
					lookupIDs.add(accID);
				}
			} else if (params.get(0).equals(ACCOUNT_TAG)) {
				// get these account IDs directly
				for (int i = 1; i < params.size(); i++) { // skip the tag
					lookupIDs.add(Integer.parseInt(params.get(i)));
				}
			}
			
			// now actually look up the accounts
			List<BankAccount> accounts = new ArrayList<>();
			String unpermittedAccounts = "";
			String nonexistantAccounts = "";
			
			for (int accID : lookupIDs) {
				if (currentUser.getType() == UserProfileType.CUSTOMER
						&& !currentUser.getOwnedAccounts().contains(accID)) {
					unpermittedAccounts = unpermittedAccounts + " " + accID;
					continue;
				}
				
				BankAccount ba = dao.readBankAccount(accID);
				
				if (ba.getType() == BankAccountType.NONE) {
					nonexistantAccounts = nonexistantAccounts + " " + accID;
					continue;
				}
				
				accounts.add(ba);
			} // end lookup for loop
			
			//System.out.println("DEBUG: accounts list is: " + accounts);
			
			if (!accounts.isEmpty()) {
				io.displayBankAccounts(accounts);
			}
			if (!unpermittedAccounts.equals("")) {
				io.displayText(VIEW_ACCOUNTS_NO_PERMISSION_PREFIX + unpermittedAccounts);
			}
			if (!nonexistantAccounts.equals("")) {
				io.displayText(VIEW_ACCOUNTS_NOT_FOUND_PREFIX + nonexistantAccounts);
			}
		}
		catch (BankDAOException e){
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Sends the current user profile to the IO for display.
	 * 
	 * @param currentRequest
	 */
	private void handleViewSelfProfile(Request currentRequest) {
		
		List<UserProfile> users = new ArrayList<>();
		users.add(currentUser);
		io.displayUserProfiles(users);
	}
	
	/**
	 * Sends a set of user profiles to the IO for display.
	 * Emp/admin can view any users. Customers can not take this action.
	 * Any invalid user IDs will be displayed afterward.
	 * 
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleViewUsers(Request currentRequest) throws ImpossibleActionException {

		try {
			String invalidIDs = "";
			List<UserProfile> users = new ArrayList<>();
			
			for (String id : currentRequest.getParams()) {
				UserProfile up = dao.readUserProfile(Integer.parseInt(id));
				
				if (up.getType() == UserProfileType.NONE) {
					//System.out.println("DEBUG: invalid user in handleViewUsers: " + id);
					invalidIDs = invalidIDs + " " + id;
				}
				else {
					users.add(up);
				}
			} // end for id loop
			
			if (!users.isEmpty()) {
				io.displayUserProfiles(users);				
			}
			if (!invalidIDs.equals("")) {
				//System.out.println("DEBUG: printing invalid ids");
				io.displayText(USER_ID_MULTIPLE_NOT_FOUND_PREFIX + invalidIDs);
			}
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Sends a set of Transactions to the IO for display.
	 * Can be grouped by transaction ID, acting user ID, or involved account ID.
	 * Customers can only see transactions grouped by themselves, or an account they own.
	 * Emp/admin can see any.
	 * 
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleViewTransactions(Request currentRequest) throws ImpossibleActionException {
		
		//System.out.println("DEBUG: handleViewTransactions called");
		try {
			// figure out how the TRRs are grouped
			List<String> params = currentRequest.getParams();
			String tag = params.get(0);
			List<TransactionRecord> transactions = new ArrayList<>(); // may be replaced
			String nonpermittedIDs = "";
			String invalidIDs = "";
			//List<TransactionRecord> toDisplay = new ArrayList<>();
			
			if (tag.equals(TRANSACTION_TAG)) {
				
				for (int i = 1; i < params.size(); i++) {
					TransactionRecord tr = dao.readTransactionRecord(Integer.parseInt(params.get(i)));
					if (tr.getType() == TransactionType.NONE) {
						invalidIDs = invalidIDs + " " + tr.getId();
 					}
					else if (currentUser.getType() == UserProfileType.CUSTOMER 
							&& tr.getActingUser() != currentUser.getId()) {
						nonpermittedIDs = nonpermittedIDs + " " + nonpermittedIDs;
					}
					else { // valid, permitted ID
						transactions.add(tr);
					}
				}
			}
			else if (tag.equals(USER_PROFILE_TAG)) {
				int userID = Integer.parseInt(params.get(1));
				if (currentUser.getType() == UserProfileType.CUSTOMER
						&& currentUser.getId() != userID) {
					throw new ImpossibleActionException(
							VIEW_TRANSACTIONS_CUSTOMER_CAN_ONLY_VIEW_SELF_MESSAGE);
				}
				
				transactions = dao.readTransactionRecordByActingUserId(userID);
			}
			else if (tag.equals(ACCOUNT_TAG)) {
				//System.out.println("DEBUG: View TRR reached ACCOUNT_TAG block");
				int accID = Integer.parseInt(params.get(1));
				if (currentUser.getType() == UserProfileType.CUSTOMER 
						&& !currentUser.getOwnedAccounts().contains(accID)) {
					//System.out.println("DEBUG: View TRR reached ACCOUNT_TAG block and threw");
					throw new ImpossibleActionException(
							VIEW_TRANSACTIONS_CUSTOMER_CAN_ONLY_VIEW_SELF_MESSAGE);
				}

				transactions = dao.readTransactionRecordByAccountId(accID);
				//System.out.println("DEBUG: transactions list is " + transactions);
			}
			
			// finally display the transactions
			if (!transactions.isEmpty()) {
				io.displayTransactionRecords(transactions);
			}
			if (!invalidIDs.equals("")) {
				io.displayText(VIEW_TRANSACTIONS_INVALID_IDS_PREFIX + invalidIDs);
			}
			if (!nonpermittedIDs.equals("")) {
				io.displayText(
						VIEW_TRANSACTIONS_NONPERMITTED_IDS_PREFIX + nonpermittedIDs);
			}		
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Creates a new account with employee privileges.
	 * Can only be done by an admin (checked by generic check)
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleCreateEmployee(Request currentRequest) throws ImpossibleActionException {

		try {
			List<String> params = currentRequest.getParams();
			String username = params.get(0);
			
			if (!dao.isUsernameFree(username)) {
				throw new ImpossibleActionException(USERNAME_IN_USE_MESSAGE);
			}
			
			String password = params.get(1); 
			int empID = dao.getHighestUserProfileID() + 1;
			UserProfile employee = new UserProfile(empID);
			employee.setUsername(username);
			employee.setPassword(password);
			employee.setType(UserProfileType.EMPLOYEE);
			dao.write(employee);
			io.displayText(CREATE_EMPLOYEE_SUCCESSFUL_PREFIX + empID);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.USER_REGISTERED);
			tr.setDestinationAccount(empID);
			saveTransactionRecord(tr);
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Creates a new account with admin privileges.
	 * Can only be done by an admin (checked by generic check)
	 * @param currentRequest
	 * @throws ImpossibleActionException
	 */
	private void handleCreateAdmin(Request currentRequest) throws ImpossibleActionException {

		try {
			List<String> params = currentRequest.getParams();
			String username = params.get(0);
			
			if (!dao.isUsernameFree(username)) {
				throw new ImpossibleActionException(USERNAME_IN_USE_MESSAGE);
			}
			
			String password = params.get(1); 
			int adminID = dao.getHighestUserProfileID() + 1;
			UserProfile adm = new UserProfile(adminID);
			adm.setUsername(username);
			adm.setPassword(password);
			adm.setType(UserProfileType.ADMIN);
			dao.write(adm);
			io.displayText(CREATE_EMPLOYEE_SUCCESSFUL_PREFIX + adminID);
			
			TransactionRecord tr = new TransactionRecord();
			tr.setType(TransactionType.USER_REGISTERED);
			tr.setDestinationAccount(adminID);
			saveTransactionRecord(tr);
		}
		catch (BankDAOException e) {
			throw new ImpossibleActionException(GENERIC_DAO_ERROR_MESSAGE);
		}
	}

	// util methods

	/**
	 * Creates an 'empty' UserProfile object, representing that no one is logged in.
	 * @return
	 */
	private static UserProfile getEmptyUser() {
		
		UserProfile empty = new UserProfile(-1);
		empty.setType(UserProfileType.NONE);
		return empty;
	}
	
	/**
	 * Changes the current user to the given user.
	 * @param user
	 */
	private void changeLoggedInUser(UserProfile user) {

		currentUser = user;
	}
	
	/**
	 * Sets the running variable to false, ending the loop.
	 */
	private void stopRunning() {
		
		running = false;
	}
	
	/**
	 * Writes the given TR to the database.
	 * This method will take care of finding the ID, setting the acting user,
	 *  and creating the timestamp (eventually)
	 * @param tr
	 */
	private void saveTransactionRecord(TransactionRecord tr){
		
		try {
			tr.setId(dao.getHighestTransactionRecordID() + 1);
			tr.setActingUser(currentUser.getId());
			tr.setTime(java.time.LocalDateTime.now().toString());
			
			log.log(
					Level.INFO, 
					"About to save transaction: " + transactionRecordToString(tr));
			
			dao.write(tr);			
		}
		catch(BankDAOException e) {
			io.displayText(TRANSACTION_RECORD_NOT_SAVED_MESSAGE);
		}
	}
	
	private String transactionRecordToString(TransactionRecord tr) {
		
		String s = "" + tr.getId() 
				+ " " + tr.getTime() 
				+ " " + tr.getType()
				+ " " + tr.getActingUser()
				+ " " + tr.getSourceAccount()
				+ " " + tr.getDestinationAccount()
				+ " " + tr.getMoneyAmount();
		
		return s;
	}
	
	
}
