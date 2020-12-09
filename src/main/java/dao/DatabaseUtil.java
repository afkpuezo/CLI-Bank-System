/**
 * A utility class for managing Connection objects as well as resetting the database to a starting
 * state in case I break it.
 * 
 * @author Andrew Curry
 */
package dao;

import java.sql.Statement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseUtil {

	// constants
	private final static String CONFIG_FILE_ADDRESS = "config\\DatabaseConfig.txt";
	
	// class / static variables
	private static Logger log = Logger.getLogger(DatabaseUtil.class);
	
	private static String databaseAddress;
	private static String databaseUsername;
	private static String databasePassword;
	
	
	/**
	 * Retrieves the necessary information about the database.
	 */
	public static void loadConfiguration() throws IOException{
		
		BufferedReader reader = new BufferedReader(
				new FileReader(CONFIG_FILE_ADDRESS));
		
		String[] lines = new String[3];
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = reader.readLine();
		}
		reader.close();
		
		databaseAddress = lines[0];
		databaseUsername = lines[1];
		databasePassword = lines[2];
	}
	
	/**
	 * Returns the address of the database being used.
	 * @return
	 */
	public static String getAddress() {
		
		return databaseAddress;
	}
	
	/**
	 * Based on the ConnectionUtil method from the demo
	 * Should probably be private but public makes it easier to test
	 * @return
	 */
	public static Connection getConnection() {
		
		Connection conn = null;
		
		try {
			conn = DriverManager.getConnection(
					databaseAddress,
					databaseUsername,
					databasePassword
					);
		} catch (SQLException e) {
			log.log(Level.WARN, "Unable to obtain connection to database: " + e.getMessage());
		}
		
		return conn;
	}
	
	/**
	 * (Should) set the database into a fresh state.
	 */
	public static void resetDatabase() {
		
		log.log(Level.INFO, "resetting database to initial configuration...");
		try (Connection conn = getConnection()){
			// drop tables --------------------------------------------------------------
			dropTableIfExists(conn, "user_profile");
			dropTableIfExists(conn, "bank_account");
			dropTableIfExists(conn, "transaction_record");
			dropTableIfExists(conn, "account_ownership");
			
			// create tables ------------------------------------------------------------
			Statement stm;
			
			// a little clumsy but it works (I hope)
			String createUserProfileTable = "CREATE TABLE \"user_profile\"\n"
					+ "(\n"
					+ "\"user_id\" INT NOT NULL,\n"
					+ "\"username\" VARCHAR(120) NOT NULL,\n"
					+ "\"password\" VARCHAR(120) NOT NULL,\n"
					+ "\"type\" VARCHAR(120) NOT NULL,\n"
					+ "CONSTRAINT \"PK_user_profile\" PRIMARY KEY (\"user_id\")\n"
					+ ");";
			stm = conn.createStatement();
			stm.execute(createUserProfileTable);
			
			String createBankAccountTable = "CREATE TABLE \"bank_account\"\n"
					+ "(\n"
					+ "\"account_id\" INT NOT NULL,\n"
					+ "\"status\" VARCHAR(120) NOT NULL,\n"
					+ "\"type\" VARCHAR(120) NOT NULL,\n"
					+ "\"funds\" INT NOT NULL,\n"
					+ "CONSTRAINT \"PK_bank_account\" PRIMARY KEY (\"account_id\")\n"
					+ ");";
			stm = conn.createStatement();
			stm.execute(createBankAccountTable);
			
			String createTransactionTable = "CREATE TABLE \"transaction_record\"\n"
					+ "(\n"
					+ "\"transaction_id\" INT NOT NULL,\n"
					+ "\"time\" VARCHAR(120) NOT NULL,\n"
					+ "\"type\" VARCHAR(120) NOT NULL,\n"
					+ "\"acting_user\" INT NOT NULL,\n"
					+ "\"source_account\" INT,\n"
					+ "\"destination_account\" INT,\n"
					+ "\"money_amount\" INT,\n"
					+ "CONSTRAINT \"PK_transaction_record\" PRIMARY KEY (\"transaction_id\")\n"
					+ ");";
			stm = conn.createStatement();
			stm.execute(createTransactionTable);
			
			// user ID is foreign keys
			String addTrasactionActingUserIDForeignKey = "ALTER TABLE \"transaction_record\" ADD CONSTRAINT \"FK_acting_user\"\n"
					+ "		FOREIGN KEY (\"acting_user\") REFERENCES \"user_profile\" (\"user_id\")";
			stm = conn.createStatement();
			stm.execute(addTrasactionActingUserIDForeignKey);
			// accounts are not foreign keys because they may be null
			/*
			String addTrasactionActingAccountIDForeignKey = "ALTER TABLE \"transaction_record\" ADD CONSTRAINT \"FK_source_account\"\n"
					+ "		FOREIGN KEY (\"source_account\") REFERENCES \"bank_account\" (\"account_id\")";
			stm = conn.createStatement();
			stm.execute(addTrasactionActingAccountIDForeignKey);
			*/
			
			String createAccountOwnershipTable = "CREATE TABLE \"account_ownership\"\n"
					+ "(\n"
					+ "\"user_id\" INT NOT NULL,\n"
					+ "\"account_id\" INT NOT NULL,\n"
					+ "PRIMARY KEY (\"user_id\", \"account_id\")\n"
					//+ "CONSTRAINT \"FK_user\" FOREIGN KEY (\"user_id\") REFERENCES user_profile (\"user_id\")"
					//+ "CONSTRAINT \"FK_account\" FOREIGN KEY (\"account_id\") REFERENCES bank_account (\"account_id\")"
					+ ");";
			stm = conn.createStatement();
			stm.execute(createAccountOwnershipTable);
			// add the foreign keys
			String addUserIDForeignKey = "ALTER TABLE \"account_ownership\" ADD CONSTRAINT \"FK_user\"\n"
					+ "		FOREIGN KEY (\"user_id\") REFERENCES \"user_profile\" (\"user_id\")";
			stm = conn.createStatement();
			stm.execute(addUserIDForeignKey);
			String addAccountIDForeignKey = "ALTER TABLE \"account_ownership\" ADD CONSTRAINT \"FK_account\"\n"
					+ "		FOREIGN KEY (\"account_id\") REFERENCES \"bank_account\" (\"account_id\")";
			stm = conn.createStatement();
			stm.execute(addAccountIDForeignKey);
			
			// populate with starting data -----------------
			populateUserProfiles(conn);
			populateBankAccounts(conn);
			populateTransactionRecords(conn);
			populateAccountOwnership(conn);
		}
		catch(SQLException e) {
			log.log(Level.WARN, "Problem resetting database: " + e.getMessage());
			
		}
	}

	/**
	 * Helper method that does what it says
	 * @param conn
	 * @param table
	 */
	private static void dropTableIfExists(Connection conn, String table) throws SQLException {
		
		String sql = "DROP TABLE IF EXISTS " + table + " CASCADE";
		Statement stm = conn.createStatement();
		stm.execute(sql);
	}
	
	/**
	 * Another helper for readability
	 * @param conn
	 */
	private static void populateUserProfiles(Connection conn) throws SQLException {
		
		PreparedStatement pstm;
		String insertUserProfileString = 
				"INSERT INTO user_profile (user_id, username, password, type) VALUES (?, ? , ?, ?);";
		
		pstm = conn.prepareStatement(insertUserProfileString);
		pstm.setInt(1, 1);
		pstm.setString(2, "admin");
		pstm.setString(3, "admin");
		pstm.setString(4, "ADMIN");
		pstm.execute();
		
		pstm = conn.prepareStatement(insertUserProfileString);
		pstm.setInt(1, 2);
		pstm.setString(2, "empl");
		pstm.setString(3, "empass");
		pstm.setString(4, "EMPLOYEE");
		pstm.execute();
		
		pstm = conn.prepareStatement(insertUserProfileString);
		pstm.setInt(1, 3);
		pstm.setString(2, "cust");
		pstm.setString(3, "pass");
		pstm.setString(4, "CUSTOMER");
		pstm.execute();
		
		pstm = conn.prepareStatement(insertUserProfileString);
		pstm.setInt(1, 4);
		pstm.setString(2, "cust2");
		pstm.setString(3, "pass");
		pstm.setString(4, "CUSTOMER");
		pstm.execute();
	}
	
	/**
	 * Another helper for readability
	 * @param conn
	 */
	private static void populateBankAccounts(Connection conn) throws SQLException {
		
		PreparedStatement pstm;
		String insertBankAccountString = 
				"INSERT INTO bank_account (account_id, status, type, funds) VALUES (?, ? , ?, ?);";
		
		pstm = conn.prepareStatement(insertBankAccountString);
		pstm.setInt(1, 1); // acc id is 1
		pstm.setString(2, "OPEN"); // status
		pstm.setString(3, "SINGLE"); // type
		pstm.setInt(4, 123456); // funds
		pstm.execute();
		
		pstm = conn.prepareStatement(insertBankAccountString);
		pstm.setInt(1, 2); // acc id is 2
		pstm.setString(2, "CLOSED"); // status
		pstm.setString(3, "SINGLE"); // type
		pstm.setInt(4, 0); // funds
		pstm.execute();
		
	}
	
	private static void populateTransactionRecords(Connection conn) throws SQLException {
		
		PreparedStatement pstm;
		String insertTransactionString = 
				"INSERT INTO transaction_record (transaction_id, time, type, acting_user, "
				+ "source_account, destination_account, money_amount) "
				+ "VALUES (?, ? , ?, ?, ?, ?, ?);";
		
		pstm = conn.prepareStatement(insertTransactionString);
		pstm.setInt(1, 1); // id
		pstm.setString(2, java.time.LocalDateTime.now().toString()); // time
		pstm.setString(3, "FUNDS_DEPOSITED"); // type 
		pstm.setInt(4, 3); // the acting user - the customer profile
		pstm.setInt(5, -1); // the source account, none
		pstm.setInt(6, 1); // the destination account, owned by the customer
		pstm.setInt(7, 123456); // the money amount
		pstm.execute();
	}
	
	private static void populateAccountOwnership(Connection conn) throws SQLException {
		
		PreparedStatement pstm;
		String insertBankAccountString = 
				"INSERT INTO account_ownership (user_id, account_id) VALUES (?, ?);";
		
		pstm = conn.prepareStatement(insertBankAccountString);
		pstm.setInt(1, 3); // owner is user 3
		pstm.setInt(2, 1); // the account is account 1
		pstm.execute();
		
		pstm = conn.prepareStatement(insertBankAccountString);
		pstm.setInt(1, 4); // owner is user 4
		pstm.setInt(2, 2); // the account is account 2
		pstm.execute();
	}
}
