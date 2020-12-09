/**
 * Contains the main method for the project. Prepares and starts the BankSystem
 * and attendant classes.
 */
package driver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import BankIO.BankIO;
import BankIO.CommandLineIO;
import bankSystem.BankSystem;
import dao.BankDAO;
import dao.BankDAOException;
import dao.DatabaseUtil;
import dao.PostgresDAO;
import dao.TextFileDAO;

public class Driver {

	// constants
	private static final String USE_TEXT_ARG = "-t";
	private static final String RESET_DATABSE_ARG = "-r";
	
	// class / static vars
	private static Logger log = Logger.getLogger(Driver.class);
	
	static private final String testFilename = "testfile.bdf"; // 'bank data file'
	static private final String[] FILELINES = {
			"PRF 101 user pass CST 444", "ACC 444 OPN SNG 78923 101", 
			"PRF 103 user2 pass CST 317 515", "ACC 317 OPN SNG 7892312 103", 
			"PRF 999 admin admin ADM", "ACC 515 OPN SNG 111111 103",
			"TRR 1 3:00 FDP 101 -1 444 87654", "TRR 2 3:00 FDP 103 -1 444 225", 
			"TRR 3 4:00 FDP 999 -1 515 12345"
	};
	
	public static void main(String[] args) {
		
		log.log(Level.INFO, "Project0 Bank online");
		// look for flags in the params
		boolean useText = false;
		boolean resetDatabase = false;
		
		for (String s : args) {
			
			if (s.equals(USE_TEXT_ARG)){
				useText = true;
			}
			else if (s.equals(RESET_DATABSE_ARG)) {
				resetDatabase = true;
			}
		}
		
		BankIO io = new CommandLineIO();
		prepareTextFile();
		
		BankDAO dao = null; // will be instantiated (or crash)
		
		try {
			if (useText) {
				dao = new TextFileDAO(testFilename);
				
			}
			else {
				dao = new PostgresDAO();
				if (resetDatabase) {
					DatabaseUtil.resetDatabase();
				}
			}			
		}
		catch (BankDAOException e) {
			System.out.println("ERROR: Could not connect to database. Terminating.");
			log.log(Level.FATAL, "Error while instantiating DAO object: " + e.getMessage());
			System.exit(1);
		}
		
		
		BankSystem bank = new BankSystem(io, dao);
		bank.start();
		// clean things up
		io.close();
		log.log(Level.INFO, "Project0 Bank offline");
	}
	
	/**
	 * Sets up a text file for use in tests.
	 * @return true if the file could be set up, false otherwise
	 */
	private static boolean prepareTextFile() {
		
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
}
