/**
 * This DAO implementation used a Postgres server to maintain data.
 * 
 * @author Andrew Curry
 */
package dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.BankData;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.TransactionRecord.TransactionType;
import com.revature.bankDataObjects.UserProfile;
import com.revature.bankDataObjects.UserProfile.UserProfileType;

import com.revature.bankDataObjects.BankAccount.BankAccountStatus;
import com.revature.bankDataObjects.BankAccount.BankAccountType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresDAO implements BankDAO {
	
	// constants
	private static final String ACCOUNT_STATUS_OPEN = "OPEN";
	private static final String ACCOUNT_STATUS_CLOSED = "CLOSED";
	private static final String ACCOUNT_STATUS_PENDING = "PENDING";
	//private static final String ACCOUNT_STATUS_NONE = "NONE"; // shouldn't be used?
	
	//private static final String ACCOUNT_TYPE_NONE = "NONE"; // shouldn't be used?
	private static final String ACCOUNT_TYPE_SINGLE = "SINGLE";
	private static final String ACCOUNT_TYPE_JOINT = "JOINT";
	
	//private static final String PROFILE_TYPE_NONE = "NONE";
	private static final String PROFILE_TYPE_CUSTOMER = "CUSTOMER";
	private static final String PROFILE_TYPE_EMPLOYEE = "EMPLOYEE";
	private static final String PROFILE_TYPE_ADMIN = "ADMIN";
	
	private static final String TRANSACTION_TYPE_ACCOUNT_REGISTERED = "ACCOUNT_REGISTERED";
	private static final String TRANSACTION_TYPE_ACCOUNT_APPROVED = "ACCOUNT_APPROVED";
	private static final String TRANSACTION_TYPE_ACCOUNT_CLOSED = "ACCOUNT_CLOSED";
	private static final String TRANSACTION_TYPE_ACCOUNT_OWNER_ADDED = "ACCOUNT_OWNER_ADDED";
	private static final String TRANSACTION_TYPE_ACCOUNT_OWNER_REMOVED = "ACCOUNT_OWNER_REMOVED";
	private static final String TRANSACTION_TYPE_FUNDS_TRANSFERRED = "FUNDS_TRANSFERRED";
	private static final String TRANSACTION_TYPE_FUNDS_DEPOSITED = "FUNDS_DEPOSITED";
	private static final String TRANSACTION_TYPE_FUNDS_WITHDRAWN = "FUNDS_WITHDRAWN";
	private static final String TRANSACTION_TYPE_USER_REGISTERED = "USER_REGISTERED";
	//private static final String TRANSACTION_TYPE_NONE = "NONE";
	
	private static final String GENERIC_SQL_EXCEPTION_MESSAGE
			= "ALERT: There was a problem communicating with the database.";
	private static final String NULL_CONNECTION_MESSAGE
			= "ALERT: Unable to make connection with database.";
	private static final String RESULT_SET_ERROR_MESSAGE
			= "ALERT: There was a problem processing results from the database.";
	
	private static final String WRITE_BANKDATA_NO_RECOGNIED_MESSAGE
			= "ALERT: Attempting to write invalid data type.";
	
	// class / static variables
	private static Logger log = Logger.getLogger(PostgresDAO.class);
	
	// instance variables
	//private String databaseAddress;
	//private String databaseUsername;
	//private String databasePassword;
	
	// constructor
	public PostgresDAO() throws BankDAOException{

		try {
			DatabaseUtil.loadConfiguration();			
		}
		catch (IOException e) {
			throw new BankDAOException("ERROR: Could not properly locate DatabaseConfig.txt");
		}
	}
	
	// methods from DAO interface ------------------------------------------------

	/**
	 * Returns address of the database
	 */
	@Override
	public String getResourceName() {

		return DatabaseUtil.getAddress();
	}

	/**
	 * Fetches the bank account with the given ID number from the data storage.
	 * If no such account exists, the resulting BankAccount object will have the
	 * status NONE, and -1 in other fields
	 * @param accID
	 * @return BankAccount object
	 */
	@Override
	public BankAccount readBankAccount(int accID) throws BankDAOException {

		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM bank_account WHERE account_id = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, accID);
			ResultSet accSet = pstm.executeQuery();
			
			BankAccount ba = new BankAccount(accID);
			
			while (accSet.next()) { // should only be one result
				//System.out.println("DEBUG: while loop starting");
				//ba.setId(accSet.getInt("account_id"));
				ba.setStatus(stringToBankAccountStatus(accSet.getString("status")));
				ba.setType(stringToBankAccountType(accSet.getString("type")));
				ba.setFunds(accSet.getInt("funds"));
				ba.setOwners(getAccountOwnerList(conn, accID));
			}
			accSet.close();
			
			return ba;
			//return buildBankAccountFromResults(accSet, ownerSet);
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readBankAccount: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches all bank accounts in the data storage.
	 * @return
	 */
	@Override
	public List<BankAccount> readAllBankAccounts() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM bank_account";
			PreparedStatement pstm = conn.prepareStatement(sql);
			ResultSet accSet = pstm.executeQuery();
			
			return getAccountListFromResults(conn, accSet);
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readAllBankAccounts: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches the user profile with the given ID number from the data storage.
	 * If no such account exists, the resulting UserProfile object will have type NONE.
	 * @param userID
	 * @return UserProfile object
	 */
	@Override
	public UserProfile readUserProfile(int userID) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM user_profile WHERE user_id = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, userID);
			ResultSet userSet = pstm.executeQuery();
			
			UserProfile up = new UserProfile(userID);
			while (userSet.next()) { // should only be one result
				//up.setId(userSet.getInt("user_id"));
				up.setUsername(userSet.getString("username"));
				up.setPassword(userSet.getString("password"));
				up.setType(stringToUserProfileType(userSet.getString("type")));
				up.setOwnedAccounts(getUserOwnedAccountsList(conn, userID));
			}
			
			return up;
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readUserProfile by ID: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches the user profile with the given username from the data storage.
	 * If no such account exists, the resulting UserProfile object will have type NONE.
	 * @param userID
	 * @return UserProfile object
	 */
	@Override
	public UserProfile readUserProfile(String username) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM user_profile WHERE username = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setString(1, username);
			ResultSet userSet = pstm.executeQuery();
			
			UserProfile up = new UserProfile();
			while (userSet.next()) { // should only be one result
				int userID = userSet.getInt("user_id");
				up.setId(userID);
				up.setUsername(userSet.getString("username"));
				up.setPassword(userSet.getString("password"));
				up.setType(stringToUserProfileType(userSet.getString("type")));
				up.setOwnedAccounts(getUserOwnedAccountsList(conn, userID));
			}
			
			//up.setOwnedAccounts(ownedAccounts);
			return up;
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readUserProfile by username: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches all user profiles in the data storage.
	 * @return
	 */
	@Override
	public List<UserProfile> readAllUserProfiles() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM user_profile;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			ResultSet userSet = pstm.executeQuery();
			
			return getUserProfileListFromResults(conn, userSet);
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readAllUserProfiles: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches the TransactionRecord with the given ID number from the data storage.
	 * If no such account exists, the resulting TransactionRecord object will have type NONE.
	 * @param recID
	 * @return TransactionRecord
	 */
	@Override
	public TransactionRecord readTransactionRecord(int recID) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM transaction_record WHERE transaction_id = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, recID);
			ResultSet trrSet = pstm.executeQuery();
			
			TransactionRecord tr = new TransactionRecord(recID);
			while (trrSet.next()) { // should only be one result
				// dont need to set ID
				tr.setType(stringToTransactionType(trrSet.getString("type")));
				tr.setTime(trrSet.getString("time"));
				tr.setActingUser(trrSet.getInt("acting_user"));
				tr.setSourceAccount(trrSet.getInt("source_account"));
				tr.setDestinationAccount(trrSet.getInt("destination_account"));
				tr.setMoneyAmount(trrSet.getInt("money_amount"));
			}
			
			return tr;
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readTransactionRecord: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches all TransactionRecords in the data storage.
	 * @return
	 */
	@Override
	public List<TransactionRecord> readAllTransactionRecords() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM transaction_record;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			ResultSet trrSet = pstm.executeQuery();
		
			return getTransactionListFromResults(conn, trrSet);
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readAllTransactionRecords: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches all TransactionRecords that were carried out by the given user.
	 * Returns an empty list if there are no matches.
	 * @param actingUserId
	 * @return
	 * @throws BankDAOException
	 */
	@Override
	public List<TransactionRecord> readTransactionRecordByActingUserId(int actingUserID) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}
			
			String sql = "SELECT * FROM transaction_record WHERE acting_user = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, actingUserID);
			ResultSet trrSet = pstm.executeQuery();
		
			return getTransactionListFromResults(conn, trrSet);
		}
		catch(SQLException e) {
			log.log(Level.ERROR, "SQL exception in readTransactionRecordByActingUserId: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Fetches all TransactionRecords that involved the given account (as source or destination)
	 * Returns an empty list if there are no matches.
	 * @param accID
	 * @return
	 * @throws BankDAOException
	 */
	@Override
	public List<TransactionRecord> readTransactionRecordByAccountId(int accID) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()) {

			if (conn == null) {
				throw new BankDAOException(NULL_CONNECTION_MESSAGE);
			}

			String sql = "SELECT * FROM transaction_record WHERE destination_account = ? OR source_account = ?;";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, accID);
			pstm.setInt(2, accID);
			ResultSet trrSet = pstm.executeQuery();

			return getTransactionListFromResults(conn, trrSet);
		} catch (SQLException e) {
			log.log(Level.ERROR, "SQL exception in readTransactionRecordByAccountId: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Writes the given BankData object to the data storage.
	 * User profiles cannot be changed after being initially written, other than owned accounts.
	 * BankAccounts can change status, type, funds, and owners
	 * TransactionRecords cannot be changed.
	 * @param bd
	 */
	@Override
	public void write(BankData bd) throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			writeHelp(conn, bd);
		}
		catch (SQLException e){
			log.log(Level.ERROR, "SQL exception in write: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
		
	}

	/**
	 * Writes each of the BankData objects in the given List to the data storage. 
	 * User profiles cannot be changed after being initially written, other than owned accounts.
	 * BankAccounts can change status, type, funds, and owners
	 * TransactionRecords cannot be changed.
	 * @param bd
	 */
	@Override
	public void write(List<BankData> toWrite) throws BankDAOException {

		try (Connection conn = DatabaseUtil.getConnection()){
			
			for (BankData bd : toWrite) {
				writeHelp(conn, bd);				
			}
		}
		catch (SQLException e){
			log.log(Level.ERROR, "SQL exception in write-list: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}

	}

	/** 
	 * @return the highest ID currently assigned to a user profile
	 */
	@Override
	public int getHighestUserProfileID() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			String sql;
			PreparedStatement pstm;
			
			sql = "SELECT MAX (user_id) as max_id FROM user_profile;";
			pstm = conn.prepareStatement(sql);
			ResultSet maxSet = pstm.executeQuery();
			
			int max = -1;
			
			while (maxSet.next()) {
				max = maxSet.getInt("max_id");
			}
			
			return max;
		}
		catch (SQLException e){
			log.log(Level.ERROR, "SQL exception in getHighestUserProfileID: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/** 
	 * @return the highest ID currently assigned to a bank account
	 */
	@Override
	public int getHighestBankAccountID() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			String sql;
			PreparedStatement pstm;
			
			sql = "SELECT MAX (account_id) as max_id FROM bank_account;";
			pstm = conn.prepareStatement(sql);
			ResultSet maxSet = pstm.executeQuery();
			
			int max = -1;
			
			while (maxSet.next()) {
				max = maxSet.getInt("max_id");
			}
			
			return max;
		}
		catch (SQLException e){
			log.log(Level.ERROR, "SQL exception in getHighestBankAccountID: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/** 
	 * @return the highest ID currently assigned to a transaction record
	 */
	@Override
	public int getHighestTransactionRecordID() throws BankDAOException {
		
		try (Connection conn = DatabaseUtil.getConnection()){
			
			String sql;
			PreparedStatement pstm;
			
			sql = "SELECT MAX (transaction_id) as max_id FROM transaction_record;";
			pstm = conn.prepareStatement(sql);
			ResultSet maxSet = pstm.executeQuery();
			
			int max = -1;
			
			while (maxSet.next()) {
				max = maxSet.getInt("max_id");
			}
			
			return max;
		}
		catch (SQLException e){
			log.log(Level.ERROR, "SQL exception in getHighestTransactionRecordID: " + e.getMessage());
			throw new BankDAOException(GENERIC_SQL_EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Determines whether or not the given username is free to use. Used during registration, to make sure that usernames are unique.
	 * @param username
	 * @return
	 */
	@Override
	public boolean isUsernameFree(String username) throws BankDAOException {
		
		UserProfile up = readUserProfile(username); // hacky but it works for now
		return up.getType() == UserProfileType.NONE;
	}

	// helper methods -------------------------------------------------------------
	
	/**
	 * Gets the list of owning user IDs for the indicated account
	 * @param conn : an already open connection
	 * @param accID
	 * @return
	 * @throws BankDAOException
	 */
	private List<Integer> getAccountOwnerList(Connection conn, int accID) throws BankDAOException{
		
		try {
			String sql = "SELECT user_id FROM account_ownership WHERE account_id = ?";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, accID);
			ResultSet ownerSet = pstm.executeQuery();
			
			List<Integer> owners = new ArrayList<>();
			
			while (ownerSet.next()) { // should be AT LEAST one
				//System.out.println("DEBUG: in ownerSet loop");
				int ownerID = ownerSet.getInt("user_id");
				owners.add(ownerID);
			}
			return owners;
		}
		catch (SQLException e) {
			log.log(Level.ERROR, "SQL exception in getAccountOwnerList: " + e.getMessage());
			throw new BankDAOException(RESULT_SET_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Converts the results of a query into a list of BankAccount objects.
	 * @param conn
	 * @param accSet
	 * @return
	 * @throws SQLException
	 * @throws BankDAOException
	 */
	private List<BankAccount> getAccountListFromResults(Connection conn, ResultSet accSet) 
			throws SQLException, BankDAOException{
		
		List<BankAccount> accounts = new ArrayList<>();
		
		while (accSet.next()) { // should only be one result
			BankAccount ba = new BankAccount();
			int accID = accSet.getInt("account_id");
			ba.setId(accID);
			ba.setStatus(stringToBankAccountStatus(accSet.getString("status")));
			ba.setType(stringToBankAccountType(accSet.getString("type")));
			ba.setFunds(accSet.getInt("funds"));
			ba.setOwners(getAccountOwnerList(conn, accID));
			accounts.add(ba);
		}
		
		return accounts;
	}
	
	/**
	 * Gets the list of accounts owned by the indicated user
	 * @param conn
	 * @param userID
	 * @return
	 * @throws BankDAOException
	 */
	private List<Integer> getUserOwnedAccountsList(Connection conn, int userID) throws BankDAOException{
		
		try {
			String sql = "SELECT account_id FROM account_ownership WHERE user_id = ?";
			PreparedStatement pstm = conn.prepareStatement(sql);
			pstm.setInt(1, userID);
			ResultSet accSet = pstm.executeQuery();
			
			List<Integer> accounts = new ArrayList<>();
			
			while (accSet.next()) { // should be AT LEAST one
				//System.out.println("DEBUG: in ownerSet loop");
				accounts.add(accSet.getInt("account_id"));
			}
			return accounts;
		}
		catch (SQLException e) {
			log.log(Level.ERROR, "SQL exception in getUserOwnedAccountsList: " + e.getMessage());
			throw new BankDAOException(RESULT_SET_ERROR_MESSAGE);
		}
	}
	
	/**
	 * Converts the results of a query into a list of UserProfile objects.
	 * @param conn
	 * @param userSet
	 * @return
	 * @throws SQLException
	 * @throws BankDAOException
	 */
	private List<UserProfile> getUserProfileListFromResults(Connection conn, ResultSet userSet)
			throws SQLException, BankDAOException{
		
		List<UserProfile> users = new ArrayList<>();
		while (userSet.next()) { // should only be one result
			UserProfile up = new UserProfile();
			int userID = userSet.getInt("user_id");
			up.setId(userID);
			up.setUsername(userSet.getString("username"));
			up.setPassword(userSet.getString("password"));
			up.setType(stringToUserProfileType(userSet.getString("type")));
			up.setOwnedAccounts(getUserOwnedAccountsList(conn, userID));
			users.add(up);
		}
		return users;
	}
	
	/**
	 * Converts the results of a query into a list of TransactionRecords objects.
	 * @param conn
	 * @param trrSet
	 * @return
	 * @throws SQLException
	 * @throws BankDAOException
	 */
	private List<TransactionRecord> getTransactionListFromResults(Connection conn, ResultSet trrSet)
			throws SQLException, BankDAOException{
		
		List<TransactionRecord> transactions = new ArrayList<>();
		while (trrSet.next()) { // should only be one result
			TransactionRecord tr = new TransactionRecord();
			tr.setId(trrSet.getInt("transaction_id"));
			tr.setType(stringToTransactionType(trrSet.getString("type")));
			tr.setTime(trrSet.getString("time"));
			tr.setActingUser(trrSet.getInt("acting_user"));
			tr.setSourceAccount(trrSet.getInt("source_account"));
			tr.setDestinationAccount(trrSet.getInt("destination_account"));
			tr.setMoneyAmount(trrSet.getInt("money_amount"));
			transactions.add(tr);
		}
		
		return transactions;
	}
	
	/**
	 * Who doesn't love nested helper methods?
	 * Figures out which helper method to send the given Bankdata to 
	 * @param conn
	 * @param bd
	 */
	private void writeHelp(Connection conn, BankData bd) throws BankDAOException, SQLException{
		
		if (bd instanceof UserProfile) {
			writeUserProfile(conn, (UserProfile)bd);
		}
		else if (bd instanceof BankAccount) {
			writeBankAccount(conn, (BankAccount)bd);
		}
		else if (bd instanceof TransactionRecord) {
			writeTransactionRecord(conn, (TransactionRecord)bd);
		}
		else { // should never be reached
			log.log(Level.ERROR, "Unrecognized child of BankData in writeHelp: " + bd.getClass());
			throw new BankDAOException(WRITE_BANKDATA_NO_RECOGNIED_MESSAGE);
		}
	}

	/**
	 * Helper method to write a single user profile
	 * @param up
	 */
	private void writeUserProfile(Connection conn, UserProfile up) throws SQLException{
		
		// the only thing that changes is the owned accounts.
		// try to insert the user, if there's a conflict, don't change anything.
		// separately, update the owned accounts relationship
		
		String sql;
		PreparedStatement pstm;
		
		sql = "INSERT INTO user_profile (user_id, username, password, type)" 
				+ "VALUES (?, ?, ? ,?)"
				+ "ON CONFLICT (user_id) DO NOTHING;";
		pstm = conn.prepareStatement(sql);
		pstm.setInt(1, up.getId());
		pstm.setString(2, up.getUsername());
		pstm.setString(3, up.getPassword());
		pstm.setString(4, "" + up.getType()); // easy way of enum to string
		pstm.execute();
		
		// now handle the owned accounts
		// I think the easiest way to do this is to delete all of the ownership records for this user
		// and then re-add only the ones that still exist
		sql = "DELETE FROM account_ownership WHERE user_id = ?;";
		pstm = conn.prepareStatement(sql);
		pstm.setInt(1, up.getId());
		pstm.execute();
		
		sql = "INSERT INTO account_ownership (user_id, account_id) VALUES (?, ?);";
		for (int accID : up.getOwnedAccounts()) {
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, up.getId());
			pstm.setInt(2, accID);
			pstm.execute();
		}
	}
	
	/**
	 * Helper method to write a single bank account
	 * @param ba
	 */
	private void writeBankAccount(Connection conn, BankAccount ba) throws SQLException{
		
		String sql;
		PreparedStatement pstm;
		
		sql = "INSERT INTO bank_account (account_id, status, type, funds)" 
				+ "VALUES (?, ?, ? ,?) "
				+ "ON CONFLICT (account_id) DO UPDATE "
				+ "SET status = ?,"
				+ "type = ?,"
				+ "funds = ?;";
		pstm = conn.prepareStatement(sql);
		pstm.setInt(1, ba.getId());
		pstm.setString(2, "" + ba.getStatus());
		pstm.setString(3, "" + ba.getType());
		pstm.setInt(4, ba.getFunds());
		pstm.setString(5, "" + ba.getStatus());
		pstm.setString(6, "" + ba.getType());
		pstm.setInt(7, ba.getFunds());
		pstm.execute();
		
		// now update the ownership relation
		sql = "DELETE FROM account_ownership WHERE account_id = ?;";
		pstm = conn.prepareStatement(sql);
		pstm.setInt(1, ba.getId());
		pstm.execute();
		
		sql = "INSERT INTO account_ownership (user_id, account_id) VALUES (?, ?);";
		for (int ownerID : ba.getOwners()) {
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, ownerID);
			pstm.setInt(2, ba.getId());
			pstm.execute();
		}
	}
	
	/**
	 * Helper method to write a single TransactionRecord
	 * @param conn
	 * @param tr
	 */
	private void writeTransactionRecord(Connection conn, TransactionRecord tr) throws SQLException {
		
		String sql;
		PreparedStatement pstm;

		sql = "INSERT INTO transaction_record (transaction_id, time, type, acting_user, "
				+ "source_account, destination_account, money_amount) "
				+ "VALUES (?, ? , ?, ?, ?, ?, ?) "
				+ "ON CONFLICT (transaction_id) DO NOTHING;"; // should never be overwritten
		pstm = conn.prepareStatement(sql);
		pstm.setInt(1, tr.getId());
		pstm.setString(2, tr.getTime());
		pstm.setString(3, "" + tr.getType());
		pstm.setInt(4, tr.getActingUser());
		pstm.setInt(5, tr.getSourceAccount());
		pstm.setInt(6, tr.getDestinationAccount());
		pstm.setInt(7, tr.getMoneyAmount());
		pstm.execute();
	}
	
	// util methods ------------------------------------------------------------
	
	/**
	 * String -> enum
	 * @param s
	 * @return
	 */
	private BankAccountType stringToBankAccountType(String s) {

		switch (s) { // set the account type
			case ACCOUNT_TYPE_JOINT:
				return BankAccountType.JOINT;
			case ACCOUNT_TYPE_SINGLE:
				return (BankAccountType.SINGLE);
			default:
				return BankAccountType.NONE;
		}
	}
	
	/**
	 * String -> enum
	 * @param s
	 * @return
	 */
	private BankAccountStatus stringToBankAccountStatus(String s) {
		
		switch(s) { // set the status
			case ACCOUNT_STATUS_OPEN:
				return BankAccountStatus.OPEN;
			case ACCOUNT_STATUS_CLOSED:
				return BankAccountStatus.CLOSED;
			case ACCOUNT_STATUS_PENDING:
				return BankAccountStatus.PENDING;
			default:
				return BankAccountStatus.NONE;
		}
	}
	
	/**
	 * String -> enum
	 * @param s
	 * @return
	 */
	private UserProfileType stringToUserProfileType(String s) {
		
		switch(s) { // set the type
			case PROFILE_TYPE_ADMIN:
				return UserProfileType.ADMIN;
			case PROFILE_TYPE_CUSTOMER:
				return UserProfileType.CUSTOMER;
			case PROFILE_TYPE_EMPLOYEE:
				return UserProfileType.EMPLOYEE;
			default:
				return UserProfileType.NONE;
		}
	}
	
	/**
	 * String -> enum
	 * @param s
	 * @return
	 */
	private TransactionType stringToTransactionType(String s) {
		
		switch(s) { // type
			case TRANSACTION_TYPE_ACCOUNT_REGISTERED:
				return TransactionType.ACCOUNT_REGISTERED;
			case TRANSACTION_TYPE_ACCOUNT_APPROVED:
				return TransactionType.ACCOUNT_APPROVED;
			case TRANSACTION_TYPE_ACCOUNT_CLOSED:
				return TransactionType.ACCOUNT_CLOSED;
			case TRANSACTION_TYPE_FUNDS_DEPOSITED:
				return TransactionType.FUNDS_DEPOSITED;
			case TRANSACTION_TYPE_FUNDS_WITHDRAWN:
				return TransactionType.FUNDS_WITHDRAWN;
			case TRANSACTION_TYPE_FUNDS_TRANSFERRED:
				return TransactionType.FUNDS_TRANSFERRED;
			case TRANSACTION_TYPE_USER_REGISTERED:
				return TransactionType.USER_REGISTERED;
			case TRANSACTION_TYPE_ACCOUNT_OWNER_ADDED:
				return TransactionType.ACCOUNT_OWNER_ADDED;
			case TRANSACTION_TYPE_ACCOUNT_OWNER_REMOVED:
				return TransactionType.ACCOUNT_OWNER_REMOVED;
			default:
				return TransactionType.NONE;
		}
	}
}
