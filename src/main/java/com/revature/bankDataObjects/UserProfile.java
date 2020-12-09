/**
 * The UserProfile class is used to encapsulate information about 
 * users of the banking system.
 * 
 * Andrew Curry, Project 0
 */
package com.revature.bankDataObjects;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends BankData {
	
	// enum(s)
	public enum UserProfileType {
		NONE, CUSTOMER, EMPLOYEE, ADMIN
	}
	
	
	// instance variables
	// ID is in super
	private String username;
	private String password; // not encrypted
	private UserProfileType type;
	private List<Integer> ownedAccounts; // referenced by ID number (should this be a set?)
	
	// constructor(s)
	public UserProfile() {
		super(); // will this when extending an abstract class?
		ownedAccounts = new ArrayList<Integer>();
		type = UserProfileType.NONE;
	}
	
	public UserProfile(int id) {
		super(); // will this when extending an abstract class?
		super.setId(id);
		ownedAccounts = new ArrayList<Integer>();
		type = UserProfileType.NONE;
	}
	
	
	// util methods
	
	
	@Override
	public String toString() {
		return "PROFILE " + super.getId() + " " + username; 
	}
	
	
	// getters and setters
	
	
	public String getUsername() {
		return username;
	}
	
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	
	public String getPassword() {
		return password;
	}
	
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public UserProfileType getType() {
		return type;
	}
	
	
	public void setType(UserProfileType type) {
		this.type = type;
	}
	
	
	// technically I'm not sure if the List should have getters/setters or just
	// add/remove methods, for now I'll keep both in.
	public List<Integer> getOwnedAccounts() {
		return ownedAccounts;
	}
	
	
	public void setOwnedAccounts(List<Integer> ownedAccounts) {
		this.ownedAccounts = ownedAccounts;
	}
	
	
	/**
	 * Prevents repeats
	 * @param accID
	 */
	public void addAccount(int accID) {
		if (!ownedAccounts.contains(accID)) {
			ownedAccounts.add(accID);
		}
	}
	
	
	public void removeAccount(int accID) {
		ownedAccounts.remove(ownedAccounts.indexOf(accID));
	}
	
	
}
