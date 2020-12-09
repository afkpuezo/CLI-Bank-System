/**
 * This file contains Junit tests for the BankSystem class.
 * It uses the MockIO and TextFileDAO classes to accomplish this.
 */
package bankTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
//import org.junit.Rule;
import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.validator.PublicClassValidator;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.BankData;
import com.revature.bankDataObjects.UserProfile;
import com.revature.bankDataObjects.UserProfile.UserProfileType;

import BankIO.MockIO;
import bankSystem.BankSystem;
import bankSystem.Request;
import bankSystem.Request.RequestType;

import com.revature.bankDataObjects.BankAccount.BankAccountStatus;
import com.revature.bankDataObjects.BankAccount.BankAccountType;
//import com.revature.bankDataObjects.TransactionRecord;
//import com.revature.bankDataObjects.TransactionRecord.TransactionType;

//import dao.BankDAO;
import dao.BankDAOException;
import dao.TextFileDAO;

public class TestBankSystem {

	// static variables for use in each test
	private static BankSystem bank;
	private static TextFileDAO tdao;
	private static MockIO mio;
	
	static private final String testFilename = "testfile.bdf"; // 'bank data file'
	static private final String[] FILELINES = {
			"PRF 101 user pass CST 444", "ACC 444 OPN SNG 78923 101", 
			"PRF 103 user2 pass CST 317 515", "ACC 317 OPN SNG 7892312 103", 
			"PRF 999 admin admin ADM", "ACC 515 OPN SNG 111111 103",
			"TRR 1 3:00 FDP 101 -1 444 87654", "TRR 2 3:00 FDP 103 -1 444 225", 
			"TRR 3 4:00 FDP 999 -1 515 12345"
	};
	
	// utility methods ----------
	
	/**
	 * Sets up a text file for use in tests.
	 * @return true if the file could be set up, false otherwise
	 */
	private boolean prepareTextFile() {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(testFilename));
			
			for (String line : FILELINES){
				writer.write(line);
				writer.write("\n");
			}
			
			writer.close();
		}
		catch (IOException e) {
			System.out.println("ALERT: prepareTextFile could not complete writing the text file.");
			return false;
		}
		
		return true; // only reached if successful
	}
	
	/**
	 * Sets up the text DAO for use
	 * @return true if successful
	 */
	private boolean prepareTextFileDAO() {
		
		try {
			tdao = new TextFileDAO(testFilename);
		}
		catch (BankDAOException e){
			System.out.println("ALERT: prepareTextFileDAO could not create a TextFileDAO");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Prepares the bank system into a default state
	 */
	@Before
	public void setup() {
		
		prepareTextFile();
		prepareTextFileDAO();
		mio = new MockIO();
		bank = new BankSystem(mio, tdao);
	}
	
	/**
	 * Logs into the given user. Used to avoid repitition.
	 * @param user
	 * @param pass
	 */
	public void logInHelp(String user, String pass) {
		List<String> params = new ArrayList<String>();
		params.add(user);
		params.add(pass);
		Request request = new Request(
				RequestType.LOG_IN, 
				params);
		mio.setNextRequest(request);
		bank.testLoop();
	}
	
	// tests -------------------------
	
	/**
	 * For my own sanity, makes sure that the @Before method setup works
	 */
	@Test
	public void testSetup() {
		
		assertNotEquals(null, bank);
		assertNotEquals(null, tdao);
		assertNotEquals(null, mio);
		assertEquals(testFilename, tdao.getResourceName());
	}
	
	@Test
	public void testRegisterUser() throws BankDAOException{
		
		//System.out.println(UserProfile.UserProfileType.CUSTOMER);
		List<String> params = new ArrayList<String>();
		params.add("newuser");
		params.add("newpass");
		Request request = new Request(
				RequestType.REGISTER_USER, 
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(output.get(1), BankSystem.USER_REGISTERED_MESSAGED);
		
		UserProfile up = tdao.readUserProfile("newuser");
		assertEquals(UserProfileType.CUSTOMER, up.getType());
		assertEquals("newpass", up.getPassword());
	}
	
	@Test
	public void testRegisterUserWithUsernameTaken() throws BankDAOException{
		
		List<String> params = new ArrayList<String>();
		params.add("user");
		params.add("newpass");
		Request request = new Request(
				RequestType.REGISTER_USER, 
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(output.get(1), BankSystem.USERNAME_IN_USE_MESSAGE);
		
		List<UserProfile> users = tdao.readAllUserProfiles();
		// any new users? could change if i change the test file
		assertEquals(3, users.size()); 
	}
	
	@Test
	public void testLogIn(){
		
		logInHelp("user", "pass");
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(output.get(1), BankSystem.LOG_IN_SUCCESSFUL_PREFIX + "user");
	}
	
	@Test
	public void testLogInBadPass(){
		
		logInHelp("user", "badpass");
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(output.get(1), BankSystem.LOGIN_INVALID_PASSWORD_MESSAGE);
	}
	
	@Test
	public void testLogInUserNotFound(){
		
		logInHelp("baduser", "badpass");
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(
				output.get(1), 
				BankSystem.LOGIN_USER_NOT_FOUND_PREFIX + "baduser");
	}
	
	/**
	 * Tests the generic catch-all permissions check before the big switch case
	 */
	@Test
	public void testActOutsidePermissions() {
		
		Request request = new Request(
				RequestType.LOG_OUT, // can't log out if you aren't logged in
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(
				output.get(1), 
				BankSystem.GENERIC_NO_PERMISSION_MESSAGE);
	}
	
	@Test
	public void testLogOut() {
		
		logInHelp("user", "pass");
		
		Request request = new Request(
				RequestType.LOG_OUT,
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(4, output.size());
		assertEquals(
				output.get(3), 
				BankSystem.LOGOUT_MESSAGE);
		
		mio.setNextRequest(request);
		bank.testLoop();
		
		output = mio.getCachedOutput();
		assertEquals(6, output.size());
		assertEquals(
				output.get(5), 
				BankSystem.GENERIC_NO_PERMISSION_MESSAGE);
	}
	
	@Test
	public void testQuit() {
		
		Request request = new Request(
				RequestType.QUIT,
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(2, output.size());
		assertEquals(
				output.get(1), 
				BankSystem.QUIT_MESSAGE);
	}
	
	@Test
	public void testApply() throws BankDAOException{
		
		logInHelp("user", "pass");
		
		Request request = new Request(RequestType.APPLY_OPEN_ACCOUNT);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(4, output.size());
		assertEquals(
				BankSystem.APPLY_OPEN_ACCOUNT_MESSAGE,
				output.get(3));
		
		UserProfile up = tdao.readUserProfile("user");
		assertEquals(2, up.getOwnedAccounts().size());
		
		int accID = up.getOwnedAccounts().get(1); // new one should be last
		BankAccount ba = tdao.readBankAccount(accID);
		assertTrue(ba.getOwners().contains(up.getId()));
		assertEquals(BankAccount.BankAccountStatus.PENDING, ba.getStatus());
	}
	
	@Test
	public void testApproveAccount() throws BankDAOException{
		
		logInHelp("user", "pass");
		
		Request request = new Request(RequestType.APPLY_OPEN_ACCOUNT);
		mio.setNextRequest(request);
		bank.testLoop();
		
		request = new Request(
				RequestType.LOG_OUT,
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		UserProfile up = tdao.readUserProfile("user");
		int accID = up.getOwnedAccounts().get(1); // new one should be last
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("" + accID);
		request = new Request(
				RequestType.APPROVE_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		// is the account actually approved?
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.ACCOUNT_APPROVED_MESSAGE, 
				output.get(output.size() - 1));
		BankAccount ba = tdao.readBankAccount(accID);
		assertEquals(BankAccountStatus.OPEN, ba.getStatus());
	}
	
	@Test
	public void testApproveBadAccount() throws BankDAOException {
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("3234259");
		Request request = new Request(
				RequestType.APPROVE_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + "3234259", 
				output.get(output.size() - 1));
		
		params = new ArrayList<String>();
		params.add("444");
		request = new Request(
				RequestType.APPROVE_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		output = mio.getCachedOutput();
		assertEquals(
				BankSystem.BANK_ACCOUNT_NOT_PENDING_MESSAGE, 
				output.get(output.size() - 1));
	}
	
	@Test
	public void testDenyAccount() throws BankDAOException {
		
		logInHelp("user", "pass");
		
		Request request = new Request(RequestType.APPLY_OPEN_ACCOUNT);
		mio.setNextRequest(request);
		bank.testLoop();
		
		request = new Request(
				RequestType.LOG_OUT,
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		UserProfile up = tdao.readUserProfile("user");
		int accID = up.getOwnedAccounts().get(1); // new one should be last
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("" + accID);
		request = new Request(
				RequestType.DENY_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		// is the account actually approved?
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.ACCOUNT_DENIED_MESSAGE, 
				output.get(output.size() - 1));
		BankAccount ba = tdao.readBankAccount(accID);
		assertEquals(BankAccountStatus.CLOSED, ba.getStatus());
	}
	
	@Test
	public void testDenyBadAccount() throws BankDAOException {
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("3234259");
		Request request = new Request(
				RequestType.DENY_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + "3234259", 
				output.get(output.size() - 1));
		
		params = new ArrayList<String>();
		params.add("444");
		request = new Request(
				RequestType.DENY_OPEN_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		output = mio.getCachedOutput();
		assertEquals(
				BankSystem.BANK_ACCOUNT_NOT_PENDING_MESSAGE, 
				output.get(output.size() - 1));
	}
	
	@Test
	public void testCloseAccount() throws BankDAOException {
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("444");
		Request request = new Request(
				RequestType.CLOSE_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.CLOSE_ACCOUNT_MESSAGE, 
				output.get(output.size() - 1));
		
		BankAccount ba = tdao.readBankAccount(444);
		assertEquals(BankAccountStatus.CLOSED, ba.getStatus());
		assertEquals(0, ba.getFunds());
	}
	
	@Test
	public void testCloseBadAccount() {
		
		logInHelp("admin", "admin");
		List<String> params = new ArrayList<String>();
		params.add("3234259");
		Request request = new Request(
				RequestType.CLOSE_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.BANK_ACCOUNT_DOES_NOT_EXIST_PREFIX + "3234259", 
				output.get(output.size() - 1));
		
		// try to close an account that's already closed
		params = new ArrayList<String>();
		params.add("444");
		request = new Request(
				RequestType.CLOSE_ACCOUNT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		bank.testLoop();
		
		output = mio.getCachedOutput();
		assertEquals(
				BankSystem.CLOSE_ACCOUNT_NOT_OPEN_MESSAGE, 
				output.get(output.size() - 1));
	}
	
	@Test
	public void testAddOwner() throws BankDAOException {
		
		logInHelp("user", "pass");
		List<String> params = new ArrayList<String>();
		params.add("444"); // the account
		params.add("103"); // the user to add
		Request request = new Request(
				RequestType.ADD_ACCOUNT_OWNER,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.ADD_OWNER_TO_ACCOUNT_MESSAGE, 
				output.get(output.size() - 1));
		
		// now check to see if it was actually done
		BankAccount ba = tdao.readBankAccount(444);
		assertEquals(BankAccountType.JOINT, ba.getType());
		List<Integer> owners = ba.getOwners();
		assertEquals(2, owners.size());
		//assertEquals(103, owners.get(1)); // no idea why this won't work
		assertTrue(103 == owners.get(1));
		
		UserProfile up = tdao.readUserProfile(103);
		List<Integer> accounts = up.getOwnedAccounts();
		assertEquals(3, accounts.size()); // already owns 2, before this
		//assertEquals(444, owners.get(2));
		assertTrue(444 == accounts.get(2));
	}
	
	@Test
	public void testRemoveOwner() throws BankDAOException{
		
		// first we have to adsd a user to create a joint account in the first place
		logInHelp("user", "pass");
		List<String> params = new ArrayList<String>();
		params.add("444"); // the account
		params.add("103"); // the user to add
		Request request = new Request(
				RequestType.ADD_ACCOUNT_OWNER,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		params.set(1, "101"); // have the user remove themselves
		request = new Request(
				RequestType.REMOVE_ACCOUNT_OWNER,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.REMOVE_OWNER_SUCCESSFUL_MESSAGE, 
				output.get(output.size() - 1));
		
		// check the data
		BankAccount ba = tdao.readBankAccount(444);
		assertFalse(ba.getOwners().contains(101));
		UserProfile up = tdao.readUserProfile(101);
		assertFalse(up.getOwnedAccounts().contains(444));
	}
	
	@Test
	public void testDeposit() throws BankDAOException{
		
		// get the original money amount
		BankAccount ba = tdao.readBankAccount(444);
		int originalFunds = ba.getFunds();
		
		logInHelp("user", "pass");
		List<String> params = new ArrayList<String>();
		params.add("444"); // the account
		params.add("2000"); // the money to add
		Request request = new Request(
				RequestType.DEPOSIT,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.DEPOSIT_SUCCESSFUL_MESSAGE, 
				output.get(output.size() - 1));
		
		ba = tdao.readBankAccount(444);
		assertEquals(originalFunds + 2000, ba.getFunds());
	}
	
	@Test
	public void testWithdraw() throws BankDAOException{
		
		// get the original money amount
		BankAccount ba = tdao.readBankAccount(444);
		int originalFunds = ba.getFunds();
		
		logInHelp("user", "pass");
		List<String> params = new ArrayList<String>();
		params.add("444"); // the account
		params.add("2000"); // the money to add
		Request request = new Request(
				RequestType.WITHDRAW,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.WITHDRAW_SUCCESSFUL_MESSAGE, 
				output.get(output.size() - 1));
		
		ba = tdao.readBankAccount(444);
		assertEquals(originalFunds - 2000, ba.getFunds());
	}
	
	@Test
	public void testTransfer() throws BankDAOException{
		
		//ACC 444 -> ACC 317
		int sourceID = 444;
		BankAccount source = tdao.readBankAccount(sourceID);
		int sourceOriginal = source.getFunds();
		int destID = 317;
		BankAccount dest = tdao.readBankAccount(destID);
		int destOriginal = dest.getFunds();
		int transferAmount = 100;
		
		logInHelp("user", "pass");
		
		List<String> params = new ArrayList<String>();
		params.add("" + sourceID); 
		params.add("" + destID);
		params.add("" + transferAmount);
		Request request = new Request(
				RequestType.TRANSFER,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Object> output = mio.getCachedOutput();
		assertEquals(
				BankSystem.TRANSFER_SUCCESSFUL_MESSAGE, 
				output.get(output.size() - 1));
		
		source = tdao.readBankAccount(sourceID);
		assertEquals(
				sourceOriginal - transferAmount, 
				source.getFunds());
		dest = tdao.readBankAccount(destID);
		assertEquals(
				destOriginal + transferAmount, 
				dest.getFunds());
	}
	
	/**
	 * A helper method for the handle view tests.
	 * @param output
	 * @return a list of the ID numbers of all the BankData objects 
	 * 			in the given list.
	 */
	public List<Integer> parseOutputForIDs(List<Object> output){
		
		List<Integer> foundIDs = new ArrayList<Integer>();
		
		for (Object o : output) {
			if (o instanceof BankData) {
				BankData bd = (BankData)o;
				foundIDs.add(bd.getId());
			}
		}
		
		return foundIDs;
	}
	
	@Test
	public void testViewAccountsAdmin() throws BankDAOException{
		
		// these are the accounts: 444, 317, 515. User 103 owns 317 and 515
		logInHelp("admin", "admin"); // should view all of the accounts
		
		List<String> params = new ArrayList<String>();
		params.add(BankSystem.ACCOUNT_TAG);
		params.add("444");
		params.add("317");
		params.add("515");
		Request request = new Request(
				RequestType.VIEW_ACCOUNTS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		//System.out.println(mio.getCachedOutput());
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(3, foundIDs.size());
		assertTrue(foundIDs.contains(444));
		assertTrue(foundIDs.contains(317));
		assertTrue(foundIDs.contains(515));
	}
	
	@Test
	public void testViewAccountsCustomer() throws BankDAOException{
		
		// these are the accounts: 444, 317, 515. User 103 owns 317 and 515
		logInHelp("user2", "pass"); // this is user 103
		
		List<String> params = new ArrayList<String>();
		params.add(BankSystem.USER_PROFILE_TAG);
		params.add("" + 103);
		Request request = new Request(
				RequestType.VIEW_ACCOUNTS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		//System.out.println(mio.getCachedOutput());
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(2, foundIDs.size());
		assertFalse(foundIDs.contains(444));
		assertTrue(foundIDs.contains(317));
		assertTrue(foundIDs.contains(515));
	}

	@Test
	public void testViewSelfProfile() throws BankDAOException{
		
		logInHelp("user", "pass");
		
		Request request = new Request(
				RequestType.VIEW_SELF_PROFILE,
				new ArrayList<>());
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(1, foundIDs.size());
		//assertEquals(101, foundIDs.get(0)); // why is this broken???
		assertTrue(101 == foundIDs.get(0));
	}
	
	@Test
	public void testViewUsers() throws BankDAOException{
		
		logInHelp("admin", "admin");
		
		List<String> params = new ArrayList<String>();
		params.add("101");
		params.add("103");
		params.add("999");
		params.add("9872384");
		Request request = new Request(
				RequestType.VIEW_USERS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(3, foundIDs.size());
		assertTrue(foundIDs.contains(101));
		assertTrue(foundIDs.contains(103));
		assertTrue(foundIDs.contains(999));
	}
	
	@Test
	public void testViewTransactions() throws BankDAOException{
		
		//"TRR 1 3:00 FDP 101 -1 444 87654", "TRR 2 3:00 FDP 103 -1 444 225", 
		//"TRR 3 4:00 FDP 999 -1 515 12345"
		logInHelp("admin", "admin");
		
		List<String> params = new ArrayList<String>();
		params.add(BankSystem.TRANSACTION_TAG);
		params.add("" + 1);
		params.add("" + 2);
		params.add("" + 3);
		Request request = new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(3, foundIDs.size());
		assertTrue(foundIDs.contains(1));
		assertTrue(foundIDs.contains(2));
		assertTrue(foundIDs.contains(3));
	}
	
	@Test
	public void testViewTransactionsActingUser() throws BankDAOException{
		
		//"TRR 1 3:00 FDP 101 -1 444 87654", "TRR 2 3:00 FDP 103 -1 444 225", 
		//"TRR 3 4:00 FDP 999 -1 515 12345"
		
		// U101, can only see T1 by acting user and T1 + T2 by owned accounts
		logInHelp("user", "pass");  
		
		List<String> params = new ArrayList<String>();
		params.add(BankSystem.USER_PROFILE_TAG);
		params.add("101");
		Request request = new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(1, foundIDs.size());
		assertTrue(foundIDs.contains(1));

	}
	
	@Test
	public void testViewTransactionsOwnedAccount() throws BankDAOException{
		
		//"TRR 1 3:00 FDP 101 -1 444 87654", "TRR 2 3:00 FDP 103 -1 444 225", 
		//"TRR 3 4:00 FDP 999 -1 515 12345"
		
		// U101, can only see T1 by acting user and T1 + T2 by owned accounts
		logInHelp("user", "pass"); 
		
		List<String> params = new ArrayList<String>();
		params.add(BankSystem.ACCOUNT_TAG);
		params.add("444");
		Request request = new Request(
				RequestType.VIEW_TRANSACTIONS,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		List<Integer> foundIDs = parseOutputForIDs(mio.getCachedOutput());
		assertEquals(2, foundIDs.size());
		assertTrue(foundIDs.contains(1));
		assertTrue(foundIDs.contains(2));
	}
	
	@Test
	public void testCreateEmployee() throws BankDAOException{
		
		logInHelp("admin", "admin");
		
		// get the ID number that will be used
		int empID = tdao.getHighestUserProfileID() + 1;
		
		List<String> params = new ArrayList<String>();
		params.add("newEmp");
		params.add("pass");
		Request request = new Request(
				RequestType.CREATE_EMPLOYEE,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		UserProfile emp = tdao.readUserProfile(empID);
		//System.out.println("DEBUG: new employee ID is " + empID);
		assertEquals(UserProfileType.EMPLOYEE, emp.getType());
		assertEquals("newEmp", emp.getUsername());
		assertEquals("pass", emp.getPassword());
	}
	
	@Test
	public void testCreateAdmin() throws BankDAOException{
		
		logInHelp("admin", "admin");
		
		// get the ID number that will be used
		int adminID = tdao.getHighestUserProfileID() + 1;
		
		List<String> params = new ArrayList<String>();
		params.add("newAdm");
		params.add("pass");
		Request request = new Request(
				RequestType.CREATE_ADMIN,
				params);
		mio.setNextRequest(request);
		bank.testLoop();
		
		UserProfile adm = tdao.readUserProfile(adminID);
		//System.out.println("DEBUG: new employee ID is " + empID);
		assertEquals(UserProfileType.ADMIN, adm.getType());
		assertEquals("newAdm", adm.getUsername());
		assertEquals("pass", adm.getPassword());
	}
}
