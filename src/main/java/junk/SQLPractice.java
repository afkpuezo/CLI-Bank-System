package junk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLPractice {

	// class variables / constants
	private static final String TEST_ADDRESS = "jdbc:postgresql://localhost:5432/";
	private static final String TEST_USERNAME = "postgres";
	private static final String TEST_PASSWORD = "password";
		
	public static void main(String[] args) {
		
		try (Connection conn = getConnection()) {
		
			String sql1 = "DROP TABLE IF EXISTS test_table_please";
	        String sql2 = "CREATE TABLE test_table_please (test_id int PRIMARY KEY)";
	        
	        PreparedStatement stm1 = conn.prepareStatement(sql1);
	        stm1.execute();
	        PreparedStatement stm2 = conn.prepareStatement(sql2);
	        stm2.execute();
	        
	        String sql3 = "INSERT INTO test_table_please (test_id) VALUES (?)";
	        PreparedStatement stm3 = conn.prepareStatement(sql3);
	        stm3.setInt(1, 123456);
	        stm3.execute();
	        stm3 = conn.prepareStatement(sql3);
	        stm3.setInt(1, 654321);
	        stm3.execute();
	        stm3 = conn.prepareStatement(sql3);
	        stm3.setInt(1, 999999);
	        stm3.execute();
	        
	        String sql4 = "SELECT * FROM test_table_please;";
	        Statement stm4 = conn.createStatement();
	        ResultSet rs = stm4.executeQuery(sql4);
	        
	        System.out.println("Printing IDs...");
	        while (rs.next()) {
	        	
	        	int id = rs.getInt("test_id");
	        	System.out.println(id);
	        }
	        
		}catch (SQLException ex) {
			ex.printStackTrace();
		}		
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
					TEST_ADDRESS,
					TEST_USERNAME,
					TEST_PASSWORD
					);
		} catch (SQLException e) {
			System.out.println("Unable to obtain connection to database " + e.getMessage());
		}
		
		return conn;
	}
}
