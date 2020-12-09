/**
 * This class contains Junit tests for the PostgresDAO class.
 * NOTE: Several tests could break if details of the resetDatabase method are changed.
 * 
 * @author Andrew Curry
 */
package bankTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.revature.bankDataObjects.BankAccount;
import com.revature.bankDataObjects.TransactionRecord;
import com.revature.bankDataObjects.TransactionRecord.TransactionType;
import com.revature.bankDataObjects.BankAccount.BankAccountStatus;
import com.revature.bankDataObjects.BankAccount.BankAccountType;
import com.revature.bankDataObjects.UserProfile;
import com.revature.bankDataObjects.UserProfile.UserProfileType;

import dao.BankDAOException;
import dao.DatabaseUtil;
import dao.PostgresDAO;

public class TestPostgresDAO {
	
	// class variables / constants
	//private static final String TEST_ADDRESS = "jdbc:postgresql://localhost:5432/";
	//private static final String TEST_USERNAME = "postgres";
	//private static final String TEST_PASSWORD = "password";

	// instance variables ----------------------------------------------------------
	private PostgresDAO pdao;
	
	// junit util methods ----------------------------------------------------------
	@Before
	public void setupPDAO() throws BankDAOException{
		
		pdao = new PostgresDAO();
		DatabaseUtil.resetDatabase();
	}
	
	@After
	public void cleanupDatabase() {
		DatabaseUtil.resetDatabase();
	}
	
	// test methods ----------------------------------------------------------------
	
	
	@Test
	public void testReadBankAccount() throws BankDAOException{
		
		BankAccount ba = pdao.readBankAccount(1);
		assertEquals(1,  ba.getId());
		assertEquals(BankAccountStatus.OPEN, ba.getStatus());
		assertEquals(BankAccountType.SINGLE, ba.getType());
		assertEquals(123456, ba.getFunds());
		assertEquals(1, ba.getOwners().size());
		assertTrue(3 == ba.getOwners().get(0));
		
		ba = pdao.readBankAccount(1001); // not found
		assertEquals(1001, ba.getId());
		assertEquals(BankAccountType.NONE, ba.getType());

		// test readAll
		
		List<BankAccount> accounts = pdao.readAllBankAccounts();
		assertEquals(2, accounts.size());

		ba = accounts.get(0);
		assertEquals(1,  ba.getId());
		assertEquals(BankAccountStatus.OPEN, ba.getStatus());
		assertEquals(BankAccountType.SINGLE, ba.getType());
		assertEquals(123456, ba.getFunds());
		assertEquals(1, ba.getOwners().size());
		assertTrue(3 == ba.getOwners().get(0));
		
		ba = accounts.get(1);
		assertEquals(2,  ba.getId());
		assertEquals(BankAccountStatus.CLOSED, ba.getStatus());
		assertEquals(BankAccountType.SINGLE, ba.getType());
		assertEquals(0, ba.getFunds());
		assertEquals(1, ba.getOwners().size());
		assertTrue(4 == ba.getOwners().get(0));
	}
	
	@Test
	public void testReadUserProfile() throws BankDAOException{
		
		UserProfile up = pdao.readUserProfile(1); // by ID
		assertEquals(1, up.getId());
		assertEquals("admin", up.getUsername());
		assertEquals("admin", up.getPassword());
		assertEquals(UserProfileType.ADMIN, up.getType());
		assertTrue(up.getOwnedAccounts().isEmpty());
		
		up = pdao.readUserProfile("cust"); // by username
		assertEquals(3, up.getId());
		assertEquals("cust", up.getUsername());
		assertEquals("pass", up.getPassword());
		assertEquals(UserProfileType.CUSTOMER, up.getType());
		assertEquals(1, up.getOwnedAccounts().size());
		assertTrue(1 == up.getOwnedAccounts().get(0));
		
		up = pdao.readUserProfile(1001); // not found
		assertEquals(1001, up.getId());
		assertEquals(UserProfileType.NONE, up.getType());
		
		// test read all
		List<UserProfile> users = pdao.readAllUserProfiles();
		assertEquals(4, users.size());
		
		up = users.get(0); //
		assertEquals(1, up.getId());
		assertEquals("admin", up.getUsername());
		assertEquals("admin", up.getPassword());
		assertEquals(UserProfileType.ADMIN, up.getType());
		assertTrue(up.getOwnedAccounts().isEmpty());
		
		up = users.get(2);
		assertEquals(3, up.getId());
		assertEquals("cust", up.getUsername());
		assertEquals("pass", up.getPassword());
		assertEquals(UserProfileType.CUSTOMER, up.getType());
		assertEquals(1, up.getOwnedAccounts().size());
		assertTrue(1 == up.getOwnedAccounts().get(0));
	}
	
	@Test
	public void testReadTransactionRecord() throws BankDAOException{
		
		TransactionRecord tr = pdao.readTransactionRecord(1);
		assertEquals(1, tr.getId());
		assertEquals(TransactionType.FUNDS_DEPOSITED, tr.getType());
		assertEquals(3, tr.getActingUser());
		assertEquals(1, tr.getDestinationAccount());
		assertEquals(123456, tr.getMoneyAmount());
		
		tr = pdao.readTransactionRecord(1001); // not found
		assertEquals(1001, tr.getId());
		assertEquals(TransactionType.NONE, tr.getType());
		
		List<TransactionRecord> transactions = pdao.readAllTransactionRecords();
		assertEquals(1, transactions.size());
		tr = transactions.get(0);
		assertEquals(1, tr.getId());
		assertEquals(TransactionType.FUNDS_DEPOSITED, tr.getType());
		assertEquals(3, tr.getActingUser());
		assertEquals(1, tr.getDestinationAccount());
		assertEquals(123456, tr.getMoneyAmount());
		
		transactions = pdao.readTransactionRecordByActingUserId(3);
		assertEquals(1, transactions.size());
		tr = transactions.get(0);
		assertEquals(1, tr.getId());
		assertEquals(TransactionType.FUNDS_DEPOSITED, tr.getType());
		assertEquals(3, tr.getActingUser());
		assertEquals(1, tr.getDestinationAccount());
		assertEquals(123456, tr.getMoneyAmount());
		
		transactions = pdao.readTransactionRecordByActingUserId(1001); // should find nothing
		assertEquals(0, transactions.size());
		
		transactions = pdao.readTransactionRecordByAccountId(1);
		assertEquals(1, transactions.size());
		tr = transactions.get(0);
		assertEquals(1, tr.getId());
		assertEquals(TransactionType.FUNDS_DEPOSITED, tr.getType());
		assertEquals(3, tr.getActingUser());
		assertEquals(1, tr.getDestinationAccount());
		assertEquals(123456, tr.getMoneyAmount());
	}
	
	@Test
	public void testWriteUserProfile() throws BankDAOException{
		
		UserProfile up;
		List<Integer> ownedAccounts;
		
		up = pdao.readUserProfile(111); // should not be found yet
		assertEquals(UserProfileType.NONE, up.getType());
		
		up = new UserProfile(111); // write this new profile
		up.setUsername("new_cust");
		up.setPassword("new_cust");
		up.setType(UserProfileType.CUSTOMER);
		ownedAccounts = new ArrayList<>();
		ownedAccounts.add(1);
		up.setOwnedAccounts(ownedAccounts);
		pdao.write(up);
		
		// assume reading works
		up = pdao.readUserProfile(111);
		assertEquals(111, up.getId());
		assertEquals("new_cust", up.getUsername());
		assertEquals("new_cust", up.getPassword());
		assertEquals(UserProfileType.CUSTOMER, up.getType());
		ownedAccounts = up.getOwnedAccounts();
		assertEquals(1, ownedAccounts.size());
		assertTrue(ownedAccounts.contains(1));
		
		// try to overwrite
		up = new UserProfile(111);
		up.setUsername("new_cust2");
		up.setPassword("new_cust2");
		up.setType(UserProfileType.ADMIN);
		ownedAccounts = new ArrayList<>();
		ownedAccounts.add(1);
		ownedAccounts.add(2);
		up.setOwnedAccounts(ownedAccounts);
		pdao.write(up);
		
		up = pdao.readUserProfile(111);
		// should not be changed
		assertEquals(111, up.getId());
		assertEquals("new_cust", up.getUsername());
		assertEquals("new_cust", up.getPassword());
		assertEquals(UserProfileType.CUSTOMER, up.getType());
		// should be changed
		ownedAccounts = up.getOwnedAccounts();
		assertEquals(2, ownedAccounts.size());
		assertTrue(ownedAccounts.contains(1));
		assertTrue(ownedAccounts.contains(2));
		
		// make sure the accounts were updated as well
		BankAccount ba;
		List<Integer> owners;
		
		ba = pdao.readBankAccount(1);
		owners = ba.getOwners();
		assertTrue(owners.contains(111));
		
		ba = pdao.readBankAccount(2);
		owners = ba.getOwners();
		assertTrue(owners.contains(111));
	}
	
	@Test
	public void testWriteBankAccount() throws BankDAOException{
		
		BankAccount ba;
		List<Integer> owners;
		
		ba = pdao.readBankAccount(111); // should not be found
		assertEquals(BankAccountType.NONE, ba.getType());
		
		ba = new BankAccount(111); // write this new account
		ba.setStatus(BankAccountStatus.PENDING);
		ba.setType(BankAccountType.SINGLE);
		owners = new ArrayList<>();
		owners.add(3);
		owners.add(4);
		ba.setOwners(owners);
		pdao.write(ba);
		
		ba = pdao.readBankAccount(111); // should be found now
		assertEquals(BankAccountType.SINGLE, ba.getType());
		assertEquals(BankAccountStatus.PENDING, ba.getStatus());
		owners = ba.getOwners();
		assertEquals(2, owners.size());
		assertTrue(owners.contains(3));
		assertTrue(owners.contains(4));
		
		// see if the users were updated too
		UserProfile up;
		List<Integer> ownedAccounts;
		
		up = pdao.readUserProfile(3);
		ownedAccounts = up.getOwnedAccounts();
		assertTrue(ownedAccounts.contains(111));
		
		up = pdao.readUserProfile(4);
		ownedAccounts = up.getOwnedAccounts();
		assertTrue(ownedAccounts.contains(111));
	}
	
	@Test
	public void testWriteTransactionRecord() throws BankDAOException{
		
		TransactionRecord tr;
		
		tr = pdao.readTransactionRecord(111); // should not be found
		assertEquals(TransactionType.NONE, tr.getType());
		
		tr = new TransactionRecord(111);
		tr.setTime("3:00");
		tr.setType(TransactionType.FUNDS_TRANSFERRED);
		tr.setActingUser(1);
		tr.setSourceAccount(1);
		tr.setDestinationAccount(2);
		tr.setMoneyAmount(444);
		pdao.write(tr);
		
		tr = pdao.readTransactionRecord(111); // should be found now
		assertEquals("3:00", tr.getTime());
		assertEquals(TransactionType.FUNDS_TRANSFERRED, tr.getType());
		assertEquals(1, tr.getActingUser());
		assertEquals(1, tr.getSourceAccount());
		assertEquals(2, tr.getDestinationAccount());
		assertEquals(444, tr.getMoneyAmount());
	}
	
	@Test
	public void testGetHighestID() throws BankDAOException{
		
		int max;
		
		max = pdao.getHighestUserProfileID();
		assertEquals(4, max);
		
		max = pdao.getHighestBankAccountID();
		assertEquals(2, max);
		
		max = pdao.getHighestTransactionRecordID();
		assertEquals(1, max);
	}
	
	@Test
	public void testIsUsernameFree() throws BankDAOException{
		
		assertTrue(pdao.isUsernameFree("unused"));
		assertFalse(pdao.isUsernameFree("admin"));
	}
}
