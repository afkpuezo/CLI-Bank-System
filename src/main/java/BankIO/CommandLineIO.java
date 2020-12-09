/**
 * An implementation of the BankIO interface that uses the Command Line.
 * 
 * @author Andrew Curry
 */
package BankIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.UserProfile;
import com.revature.bankDataObjects.UserProfile.UserProfileType;

import bankSystem.BankSystem;
import bankSystem.Request;
import bankSystem.Request.RequestType;


public class CommandLineIO implements BankIO {

	// static / class variables / constants
	private static final String FRAME_LINE = "-----------------------------------";
	private static final String DISPLAY_PROFILES_HEADER = "Showing user profiles...";
	private static final String DISPLAY_ACCOUNTS_HEADER = "Showing accounts...";
	private static final String DISPLAY_TRANSACTIONS_HEADER = "Showing transactions...";
	/*
	private static final String CHOICES_HEADER 
			= "Type the number matching one of the following choices: ";
	 */
	private static final String CHOICES_PROMPT 
			= "Enter your choice here: ";
	private static final String DISPLAY_FIELD_EMPTY = "---";
	
	private static final String PARSE_INT_INVALID_INPUT_MESSAGE
			= "Invalid input. Please enter a number.";
	/*
	private static final String PARSE_INT_CHOICE_OUT_OF_BOUNDS_MESSAGE
			= "Invalid input. Please choose one of the available options.";
	*/
	private static final String PARSE_INT_BELOW_MIN_PREFIX
			= "Invalid input: must be at least minimum value of ";
	private static final String PARSE_INT_BELOW_MAX_PREFIX
			= "Invalid input: input must be below maximum value of ";
	
	private static final String PARSE_STRING_WHITESPACE_INVALID
			= "Invalid input. No whitespace characters are allowed"; 
	/*
	private static final String PARSE_STRING_EMPTY_STRING_INVALID
			= "Invalid input - no input detected.";
	*/
	
	private static final String PARSE_MONEY_BAD_FORMAT_PREFIX
			= "Invalid input: ";
	
	private static final String BAD_MONEY_FORMAT_ONLY_TWO_DECIMAL_PLACES_MESSAGE
			= "Input has more than 2 characters after the decimal point.";
	private static final String BAD_MONEY_FORMAT_ONLY_ONE_DOT_MESSAGE
			= "Input has a second decimal point.";
	private static final String BAD_MONEY_FORMAT_DOLLAR_SIGN_WRONG_MESSAGE
			= "'$' character is only valid as the first character.";
	private static final String BAD_MONEY_FORMAT_GENERIC_PREFIX
			= "Input contains an invalid character: ";
	private static final String BAD_MONEY_FORMAT_NEGATIVE_MESSAGE
			= "Negative money amounts are not allowed.";
	
	private static final String USERNAME_PROMPT = "Enter username: ";
	private static final String PASSWORD_PROMPT = "Enter password: ";
	private static final String USER_ID_PROMPT = "Enter user ID: ";
	private static final String ACCOUNT_ID_PROMPT = "Enter account ID: ";
	//private static final String TRANSACTION_ID_PROMPT = "Enter transaction ID: ";
	private static final String MONEY_AMOUNT_PROMPT = "Enter an amount of money: ";
	
	/*
	private static final String REGISTER_HEADER = "Registering new user...";
	
	private static final String LOG_IN_HEADER = "Logging in...";
	
	private static final String LOG_OUT_HEADER = "Loggin out...";
	
	private static final String QUIT_HEADER = "Quitting...";
	
	private static final String APPLY_HEADER = "Applying to open an account...";
	
	private static final String APPROVE_HEADER = "Approving an account...";
	
	private static final String DENY_HEADER = "Denying an account...";
	
	private static final String CLOSE_HEADER = "Closing an account...";
	
	private static final String ADD_OWNER_HEADER 
			= "Adding a new owner to an account...";
	
	private static final String REMOVE_OWNER_HEADER
			= "Removing an owner from an account...";
	
	private static final String DEPOSIT_HEADER = "Depositing funds...";
	
	private static final String WITHDRAW_HEADER = "Withdrawing funds...";
	
	private static final String TRANSFER_HEADER = "Transferring funds...";
	*/
	private static final String TRANSFER_SOURCE_ACCOUNT_PROMPT
			= "Enter source account ID: ";
	private static final String TRANSFER_DESTINATION_ACCOUNT_PROMPT
			= "Enter destination account ID: ";
	
	//private static final String VIEW_ACCOUNTS_HEADER = "Viewing accounts...";
	private static final String VIEW_ACCOUNTS_MENU
			= "(1) View all accounts owned by a single user\n"
			+ "(2) Input a list of account IDs to view\n"
			+ FRAME_LINE;
	/*
	private static final String VIEW_ACCOUNTS_ID_LIST_HEADER
			= "Enter a list of account IDs on a single line, separated by spaces.";
	*/
	
	private static final String ID_LIST_PROMPT
			= "Enter the IDs here, separated by spaces: ";
	private static final String ID_LIST_BAD_TOKEN_MESSAGE
			= "Invalid input. Every ID must be numbers only.";
	
	/*
	private static final String VIEW_USERS_HEADER = "Viewing users...";
	
	private static final String VIEW_TRANSACTION_HEADER = "Viewing transactions...";
	private static final String VIEW_TRANSACTIONS_ID_LIST_HEADER
			= "Enter a list of transaction IDs on a single line, separated by spaces.";
	 */
	private static final String VIEW_TRANSACTIONS_MENU
			= "(1) View all transactions made by a single user\n"
			+ "(2) View all transactions involving a certain account\n"
			+ "(3) Input a list of transaction IDs to view\n"
			+ FRAME_LINE;
	
	/*
	private static final String CREATE_EMPLOYEE_HEADER 
			= "Creating new employee account...";
	
	private static final String CREATE_ADMIN_HEADER 
	= "Creating new administrator account...";
	*/
	
	// instance variables (fields)
	private Scanner scan;
	
	// constructor
	public CommandLineIO() {

		scan = new Scanner(System.in);
	}
	
	// helper methods --------------------
	
	/**
	 * Converts an integer amount of money to a user-friendly string representation.
	 * Eg, 12345 -> "$123.45"
	 * @param funds
	 * @return
	 */
	private String intToMoneyString(int funds) {
		
		String temp = "" + funds;
		
		// we need to pad it if it's too small
		// hopefully this padding doesn't break the entire javascript ecosystem
		int padNeeded = 3 - temp.length();
		
		for (int i = 0; i < padNeeded; i++) {
			temp = "0" + temp;
		}
		
		return "$" + temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2);
	}
	
	/**
	 * Converts a user-friendly string representation of money to an int.
	 * EG, "$123.45" -> 12345
	 * "123.45" -> 12345
	 * "12345" -> 12345 (as in, 12345.00)
	 * @param funds
	 * @return
	 */
	private int moneyStringToInt(String funds) throws BadMoneyFormatException{
		
		int startingIndex = 0;
		if (funds.charAt(0) == '$') {
			startingIndex += 1;
		} else if (funds.charAt(0) == '-') {
			throw new BadMoneyFormatException(BAD_MONEY_FORMAT_NEGATIVE_MESSAGE);
		}
		
		
		String clean = "";
		boolean dotFound = false;
		int dotFoundIndex = -1;
		for (int i = startingIndex; i < funds.length(); i++) {
			
			if (dotFound && (i - dotFoundIndex) > 2) { // only 2 decimal places
				throw new BadMoneyFormatException(
						BAD_MONEY_FORMAT_ONLY_TWO_DECIMAL_PLACES_MESSAGE);
			}
			
			char c = funds.charAt(i);
			if (Character.isDigit(c)) { // numerical digit
				clean = clean + c;
			}
			else if (c == '.') { // only valid once
				if (dotFound) { // this is the 2nd dot, which is invalid
					throw new BadMoneyFormatException(
							BAD_MONEY_FORMAT_ONLY_ONE_DOT_MESSAGE);
				}
				else {
					dotFound = true;
					dotFoundIndex = i;
				}
			}
			else if (c == '$') { // only valid at the start
				throw new BadMoneyFormatException(
						BAD_MONEY_FORMAT_DOLLAR_SIGN_WRONG_MESSAGE);
			}
			else { // other invalid
				throw new BadMoneyFormatException(
						BAD_MONEY_FORMAT_GENERIC_PREFIX + c);
			}
		}
		
		int money = Integer.parseInt(clean);
		// if no dot, we have to add .00, effectively
		if (!dotFound) {
			money *= 100;
		}
		
		return money; 
	}
	
	
	// methods from IO interface ----------
	
	/**
	 * Displays the given text to the user.
	 * @param text
	 */
	@Override
	public void displayText(String text) {
		
		System.out.println(text);
	}

	/**
	 * Displays information about the given user profile(s) to the user.
	 * @param up
	 */
	@Override
	public void displayText(String text, boolean frame) {
		
		if (frame) {
			System.out.println("\n" + FRAME_LINE);
			displayText(text);
			System.out.println(FRAME_LINE + "\n");
		}
		else {
			displayText(text);
		}
	}

	/**
	 * Displays information about the given bank accounts to the user.
	 * @param accounts
	 */
	@Override
	public void displayUserProfiles(List<UserProfile> users) {
		
		displayText(DISPLAY_PROFILES_HEADER, true);
		
		for (UserProfile up : users) {
			String line = "|ID: " + up.getId();
			line = line + " |Username: " + up.getUsername();
			line = line + " |Type: " + cleanUpGenericEnumString("" + up.getType());
			
			if (up.getType() == UserProfileType.CUSTOMER) {
				line = line + " |Owned Account ID(s): ";
				for (int accID : up.getOwnedAccounts()) {
					line = line + " " + accID;
				} // end inner for loop
			}
			
			System.out.println(line);
		} // end outer for loop
	}

	/**
	 * Displays information about the given transaction records to the user.
	 * @param accounts
	 */
	@Override
	public void displayBankAccounts(List<BankAccount> accounts) {
		
		displayText(DISPLAY_ACCOUNTS_HEADER, true);
		
		for (BankAccount ba : accounts) {
			String line = "|ID: " + ba.getId();
			line = line + " |Type: " + cleanUpGenericEnumString("" + ba.getType());
			line = line + " |Status: " + cleanUpGenericEnumString("" + ba.getStatus());
			line = line + " |Funds: " + intToMoneyString(ba.getFunds());
			line = line + " |Owner ID(s): "; // assume not empty
			
			for (int ownerID : ba.getOwners()) {
				line = line + " " + ownerID;
			} // end inner for loop
			
			System.out.println(line);
		} // end outer for loop
	}

	/**
	 * Returns a Request object based on the user's responding input.
	 * @param permittedRequestTypes : the user chooses one of these
	 * @return Request
	 */
	@Override
	public void displayTransactionRecords(List<TransactionRecord> transactions) {
		
		displayText(DISPLAY_TRANSACTIONS_HEADER, true);
		
		for (TransactionRecord tr : transactions) {
			String line = "|ID: " + tr.getId();
			line = line + " |Type: " + cleanUpGenericEnumString("" + tr.getType());
			line = line + " |Time: " + tr.getTime();
			line = line + " |Acting User ID: " + tr.getActingUser();
			
			line = line + " |Source Account: ";
			if (tr.getSourceAccount() == -1) {
				line = line + DISPLAY_FIELD_EMPTY;
			}
			else {
				line = line + tr.getSourceAccount();
			}
			
			line = line + " |Destination Account: ";
			if (tr.getDestinationAccount() == -1) {
				line = line + DISPLAY_FIELD_EMPTY;
			}
			else {
				line = line + tr.getDestinationAccount();
			}
			
			line = line + " |Money amount: ";
			if (tr.getMoneyAmount() == -1) {
				line = line + DISPLAY_FIELD_EMPTY;
			}
			else {
				line = line + intToMoneyString(tr.getMoneyAmount());
			}
			
			System.out.println(line);
		} // end outer for loop
	}
	
	/**
	 * Called by the BankSystem when execution is ending. Allows for cleanup.
	 */
	public void close() {
		
		scan.close();
	}

	/**
	 * Returns a Request object based on the user's responding input.
	 * @param permittedRequestTypes : the user chooses one of these
	 * @return Request
	 */
	@Override
	public Request prompt(RequestType[] permittedRequestTypes) {
		
		// Figure out what kind of request the user wants to make
		// Then, handle getting the parameters from the user
		// then, return the request
		RequestType rtype = permittedRequestTypes[chooseRequestType(permittedRequestTypes)];
		Request req = null; // filled in later
		
		displayText("You chose: " + cleanUpRequestType(rtype), true);
		
		switch(rtype) {
		
			case REGISTER_USER:
				req = buildRegisterUser();
				break;
			case LOG_IN:
				req = buildLogIn();
				break;
			case LOG_OUT:
				req = buildLogOut();
				break;
			case QUIT:
				req = buildQuit();
				break;
			case APPLY_OPEN_ACCOUNT:
				req = buildApplyToOpenAccount();
				break;
			case APPROVE_OPEN_ACCOUNT:
				req = buildApproveOpenAccount();
				break;
			case DENY_OPEN_ACCOUNT:
				req = buildDenyOpenAccount();
				break;
			case CLOSE_ACCOUNT:
				req = buildCloseAccount();
				break;
			case ADD_ACCOUNT_OWNER:
				req = buildAddAccountOwner();
				break;
			case REMOVE_ACCOUNT_OWNER:
				req = buildRemoveAccountOwner();
				break;
			case DEPOSIT:
				req = buildDeposit();
				break;
			case WITHDRAW:
				req = buildWithdraw();
				break;
			case TRANSFER:
				req = buildTransfer();
				break;
			case VIEW_ACCOUNTS:
				req = buildViewAccounts();
				break;
			case VIEW_SELF_PROFILE:
				req = buildViewSelfProfile();
				break;
			case VIEW_USERS:
				req = buildViewUsers();
				break;
			case VIEW_TRANSACTIONS:
				req = buildViewTransactions();
				break;
			case CREATE_EMPLOYEE:
				req = buildCreateEmployee();
				break;
			case CREATE_ADMIN:
				req = buildCreateAdmin();
				break;
		}
		
		return req;
	}
	
	/**
	 * Gets the username and password for the new admin account
	 * @return
	 */
	private Request buildCreateAdmin() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(CREATE_ADMIN_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add(parseString(USERNAME_PROMPT));
		params.add(parseString(PASSWORD_PROMPT));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.CREATE_ADMIN,
				params);
	}

	/**
	 * Gets the username and password for the new employee account
	 * @return
	 */
	private Request buildCreateEmployee() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(CREATE_EMPLOYEE_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add(parseString(USERNAME_PROMPT));
		params.add(parseString(PASSWORD_PROMPT));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.CREATE_EMPLOYEE,
				params);
	}

	/**
	 * Figures out if they want to search by trr ID, acting user ID, or
	 * source/dest bank ID, then hands off to helper method.
	 * @return
	 */
	private Request buildViewTransactions() {
		
		//displayText(VIEW_TRANSACTION_HEADER, true);
		
		System.out.println(VIEW_TRANSACTIONS_MENU);
		int choice = parseInt(CHOICES_PROMPT, 1, 4); // max NOT inclusive
		
		if (choice == 1) { 
			return viewTransactionsByUser(); 
		}
		else if (choice == 2){
			return viewTransactionsByAccount();
		} else { // only other choice is 2
			return viewTransactionsByID();
		}
	}

	/**
	 * Gets the account ID
	 * @return
	 */
	private Request viewTransactionsByAccount() {
		
		int id = parseInt(ACCOUNT_ID_PROMPT);
		List<String> params = new ArrayList<>();
		params.add(BankSystem.ACCOUNT_TAG);
		params.add("" + id);
		
		return new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
	}

	/**
	 * Gets the user ID
	 * @return
	 */
	private Request viewTransactionsByUser() {
		
		int id = parseInt(USER_ID_PROMPT);
		List<String> params = new ArrayList<>();
		params.add(BankSystem.USER_PROFILE_TAG);
		params.add("" + id);
		
		return new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
	}

	/**
	 * Gets the list of trr IDs
	 * @return
	 */
	private Request viewTransactionsByID() {
		
		//System.out.println(VIEW_TRANSACTIONS_ID_LIST_HEADER);
		
		List<String> params = parseIDList(ID_LIST_PROMPT);
		params.add(0, BankSystem.TRANSACTION_TAG);
		return new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
	}

	/**
	 * Gets the list of user IDs
	 * @return
	 */
	private Request buildViewUsers() {
		
		//displayText(VIEW_ACCOUNTS_HEADER, true);
		
		List<String> params = parseIDList(ID_LIST_PROMPT);
		return new Request(
				RequestType.VIEW_USERS,
				params);
	}

	/**
	 * No params needed for this
	 * @return
	 */
	private Request buildViewSelfProfile() {
		
		return new Request(RequestType.VIEW_SELF_PROFILE);
	}

	/**
	 * Figures out if they want to search by owning user or by number, then
	 * hand off to helper method.
	 * @return
	 */
	private Request buildViewAccounts() {
		
		//displayText(VIEW_ACCOUNTS_HEADER, true);
		
		System.out.println(VIEW_ACCOUNTS_MENU);
		int choice = parseInt(CHOICES_PROMPT, 1, 3); // max NOT inclusive
		
		if (choice == 1) { 
			return viewAccountsByUser(); 
		}
		else { // only other choice is 2
			return viewAccountsByID();
		}
	}

	/**
	 * Helper for buildViewAccounts
	 * Gets the owning user's ID
	 * @return
	 */
	private Request viewAccountsByUser() {
		
		List<String> params = new ArrayList<>();
		params.add(BankSystem.USER_PROFILE_TAG);
		params.add("" + parseInt(USER_ID_PROMPT));
		
		return new Request(
				RequestType.VIEW_ACCOUNTS,
				params);
	}
	
	/**
	 * Gets the list of IDs
	 * @return
	 */
	private Request viewAccountsByID() {
		
		//System.out.println(VIEW_ACCOUNTS_ID_LIST_HEADER);
		
		List<String> params = parseIDList(ID_LIST_PROMPT);
		
		// insert the tag last because otherwise it would be erased
		params.add(0, BankSystem.ACCOUNT_TAG);
		
		return new Request(
				RequestType.VIEW_ACCOUNTS,
				params);
	}

	/**
	 * Gets the source account ID, the destination account ID,
	 * and money amount
	 * @return
	 */
	private Request buildTransfer() {
		
		//displayText(TRANSFER_HEADER, true); // why was I not doing this for every method...
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(TRANSFER_SOURCE_ACCOUNT_PROMPT));
		params.add("" + parseInt(TRANSFER_DESTINATION_ACCOUNT_PROMPT));
		params.add("" + parseMoney(MONEY_AMOUNT_PROMPT));
		
		return new Request(
				RequestType.TRANSFER,
				params);
	}

	/**
	 * Gets the account ID and money amount
	 * @return
	 */
	private Request buildWithdraw() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(WITHDRAW_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		params.add("" + parseMoney(MONEY_AMOUNT_PROMPT));
		
		return new Request(
				RequestType.WITHDRAW,
				params);
	}

	/**
	 * Gets the account ID and money amount
	 * @return
	 */
	private Request buildDeposit() {

		/*
		System.out.println(FRAME_LINE);
		System.out.println(DEPOSIT_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		params.add("" + parseMoney(MONEY_AMOUNT_PROMPT));
		
		return new Request(
				RequestType.DEPOSIT,
				params);
	}

	/**
	 * Gets the account and user IDs
	 * @return
	 */
	private Request buildRemoveAccountOwner() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(REMOVE_OWNER_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		params.add("" + parseInt(USER_ID_PROMPT, 0, Integer.MAX_VALUE));
	
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.REMOVE_ACCOUNT_OWNER,
				params);
	}

	/**
	 * Gets the account and user IDs
	 * @return
	 */
	private Request buildAddAccountOwner() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(ADD_OWNER_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		params.add("" + parseInt(USER_ID_PROMPT, 0, Integer.MAX_VALUE));
	
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.ADD_ACCOUNT_OWNER,
				params);
	}

	/**
	 * Gets the ID of the account to close.
	 * @return
	 */
	private Request buildCloseAccount() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(CLOSE_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.CLOSE_ACCOUNT,
				params);
	}

	/**
	 * Gets the ID of the account to deny.
	 * @return
	 */
	private Request buildDenyOpenAccount() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(DENY_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.DENY_OPEN_ACCOUNT,
				params);
	}

	/**
	 * Gets the ID of the account to approve.
	 * @return
	 */
	private Request buildApproveOpenAccount() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(APPROVE_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add("" + parseInt(ACCOUNT_ID_PROMPT, 0, Integer.MAX_VALUE));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.APPROVE_OPEN_ACCOUNT,
				params);
	}

	/**
	 * Creates a request to open an account
	 * @return
	 */
	private Request buildApplyToOpenAccount() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(APPLY_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		System.out.println(FRAME_LINE);
		return new Request(RequestType.APPLY_OPEN_ACCOUNT);
	}

	/**
	 * Creates a quit request.
	 * @return
	 */
	private Request buildQuit() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(QUIT_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		System.out.println(FRAME_LINE);
		return new Request(RequestType.QUIT);
	}

	/**
	 * Creates a log out request.
	 * @return
	 */
	private Request buildLogOut() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(LOG_OUT_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		System.out.println(FRAME_LINE);
		return new Request(RequestType.LOG_OUT);
	}

	/**
	 * Gets the username and password.
	 * @return
	 */
	private Request buildLogIn() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(LOG_IN_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add(parseString(USERNAME_PROMPT));
		params.add(parseString(PASSWORD_PROMPT));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.LOG_IN,
				params);
	}

	/**
	 * Gets the desired username and password.
	 * @return
	 */
	private Request buildRegisterUser() {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(REGISTER_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		List<String> params = new ArrayList<>();
		params.add(parseString(USERNAME_PROMPT));
		params.add(parseString(PASSWORD_PROMPT));
		
		System.out.println(FRAME_LINE);
		return new Request(
				RequestType.REGISTER_USER,
				params);
	}

	/**
	 * Helper method that asks the user which of the provided choices they want.
	 * @param permittedRequestTypes
	 * @return
	 */
	private int chooseRequestType(RequestType[] permittedRequestTypes) {
		
		/*
		System.out.println(FRAME_LINE);
		System.out.println(CHOICES_HEADER);
		System.out.println(FRAME_LINE);
		*/
		
		for (int i = 0; i < permittedRequestTypes.length; i++) {
			// print (i + 1) to avoid starting on 0
			String line = "(" + (i + 1) + ") " + cleanUpRequestType(permittedRequestTypes[i]);
			System.out.println(line);
		}
		
		System.out.println(FRAME_LINE);
		// add 1 to length and subtract 1 to correct for (i + 1)
		return (parseInt(CHOICES_PROMPT, 1, (permittedRequestTypes.length + 1))) - 1; 
	}
	
	/**
	 * Helper method that formats RequestType enums in a more friendly way
	 * 
	 * @param rtype
	 * @return
	 */
	private String cleanUpRequestType(RequestType rtype) {

		String ans = "";

		switch (rtype) {

			case REGISTER_USER:
				ans = "Register a new customer profile";
				break;
			case LOG_IN:
				ans = "Log in to an existing profile";
				break;
			case LOG_OUT:
				ans = "Log out";
				break;
			case QUIT:
				ans = "Exit the application";
				break;
			case APPLY_OPEN_ACCOUNT:
				ans = "Apply to open a new account";
				break;
			case APPROVE_OPEN_ACCOUNT:
				ans = "Approve an application to open a new account";
				break;
			case DENY_OPEN_ACCOUNT:
				ans = "Deny an application to open a new account";
				break;
			case CLOSE_ACCOUNT:
				ans = "Close an existing account";
				break;
			case ADD_ACCOUNT_OWNER:
				ans = "Add a user as an owner to an account";
				break;
			case REMOVE_ACCOUNT_OWNER:
				ans = "Remove a user as an owner from an account";
				break;
			case DEPOSIT:
				ans = "Deposit funds into an account";
				break;
			case WITHDRAW:
				ans = "Withdraw funds from an account";
				break;
			case TRANSFER:
				ans = "Transfer funds from one account to another";
				break;
			case VIEW_ACCOUNTS:
				ans = "View account(s)";
				break;
			case VIEW_SELF_PROFILE:
				ans = "View your own user profile";
				break;
			case VIEW_USERS:
				ans = "View user profile(s)";
				break;
			case VIEW_TRANSACTIONS:
				ans = "View transaction(s)";
				break;
			case CREATE_EMPLOYEE:
				ans = "Create a new Employee account";
				break;
			case CREATE_ADMIN:
				ans = "Create a new Administrator account";
				break;
		}
		
		return ans;
	}
	
	/**
	 * Helper method that prompts the user for an int (non-money) value.
	 * Will loop until they give valid input.
	 * @param promptText
	 * @param min : minimum choice value allowed (inclusive)
	 * @param max : minimum choice value allowed (NOT inclusive)
	 * @return
	 */
	private int parseInt(String promptText, int min, int max) {
		
		
		int choice = 0;
		boolean isValid = false;
		do {
			System.out.print(promptText);
			String input = "";
			while (input.equals("")) {
				input = scan.nextLine();
			}
			try {
				choice = Integer.parseInt(input);
				// it's an int, is it a valid int?
				if (min > choice) {
					System.out.println(PARSE_INT_BELOW_MIN_PREFIX + min);
				}
				else if (choice >= max) {
					System.out.println(PARSE_INT_BELOW_MAX_PREFIX + max);
				}
				else {
					isValid = true;					
				}
			}
			catch (NumberFormatException e) {
				System.out.println(PARSE_INT_INVALID_INPUT_MESSAGE);
			}
		} while(!isValid);
		
		return choice;
	}
	
	/**
	 * Helper method that prompts the user for an int (non-money) value.
	 * Will loop until they give valid input (any positive int)
	 * @param promptText
	 * @return
	 */
	private int parseInt(String promptText) {
		return parseInt(promptText, 0, Integer.MAX_VALUE);
	}
	
	/**
	 * Helper method that prompts the user for a string.
	 * Currently, only whitespace characters or the empty string are invalid.
	 * @param promptText
	 * @return
	 */
	private String parseString(String promptText) {
		
		boolean isValid = false;
		String input = ""; // will be filled in
		do {
			System.out.print(promptText);
			input = scan.nextLine();
			
			while (input.equals("")) {
				input = scan.nextLine();
			}
			boolean foundWhite = false;
			for (char c : input.toCharArray()) {
				if (Character.isWhitespace(c)){
					foundWhite = true;
					System.out.println(PARSE_STRING_WHITESPACE_INVALID);
					break;
				}
			}
			if (!foundWhite) {
				isValid = true;
			}
		} while(!isValid);
		
		return input;
	}
	
	/**
	 * Helper method that prompts the user for an amount of money.
	 * @param promptText
	 * @return int representation of money
	 */
	private int parseMoney(String promptText) {
		
		boolean isValid = false;
		int input = -1; // will be filled in
		
		do {
			try {
				System.out.print(promptText);
				String moneyText = scan.next();
				input = moneyStringToInt(moneyText);
				// if we get here, it's valid
				isValid = true;
			}
			catch (BadMoneyFormatException e){
				System.out.println(PARSE_MONEY_BAD_FORMAT_PREFIX + e.getMessage());
			}
		} while(!isValid);
		
		return input;
	}
	
	/**
	 * Helper method that prompts the user for a list of IDs.
	 * Loops until the input is in the right format (numbers seperated by spaces)
	 * @param promptText
	 * @return
	 */
	private List<String> parseIDList(String promptText){
		
		List<String> params;
		boolean isValid = false;
		do {
			System.out.print(promptText);
			String idLine = scan.nextLine();
			while (idLine.equals("")) { // not sure why this is necessary
				idLine = scan.nextLine();
			}
			String[] tokens = idLine.split(" ");
			params = new ArrayList<>();
			
			try {
				for (String t : tokens) {
					Integer.parseInt(t); // only to check the format
					params.add(t);
				}
				// if we read every token, we're good
				isValid = true;
			}
			catch(NumberFormatException e){ // if one of the tokens was bad
				System.out.println(ID_LIST_BAD_TOKEN_MESSAGE);
			} 
		} while(!isValid);
		
		return params;
	}
	
	/**
	 * When enums are converted to a string, they are ALL_CAPS. This method
	 * cleans them up to a more friendly format.
	 * EXAMPLE_ENUM -> Example enum
	 * @param s
	 * @return
	 */
	private String cleanUpGenericEnumString(String s) {
		
		String ans = "";
		
		if (s.length() > 0) {
			ans = ans + s.charAt(0);
		}
		
		for (int i = 1; i < s.length(); i ++) {
			
			char old = s.charAt(i);
			char fresh; // can't use 'new'...
			
			if (old == '_') {
				fresh = ' ';
			}
			else {
				fresh = Character.toLowerCase(old);
			}
			
			ans = ans + fresh;
		}
		
		return ans;
	}
}
