/**
 * The BankAccount class is used to encapsulate information about
 * bank accounts in the banking system.
 * 
 * Andrew Curry, Project 0
 */
package com.revature.bankDataObjects;

import java.util.ArrayList;
import java.util.List;

public class BankAccount extends BankData {
	
	// enums
	public enum BankAccountStatus{
		NONE, OPEN, CLOSED, PENDING
	}
	
	public enum BankAccountType{
		NONE, SINGLE, JOINT
	}
	
	// instance variables
	// id in super
	List<Integer> owners; // could be a single or joint account
	private BankAccountStatus status;
	private BankAccountType type;
	private int funds; // could be a special Money class or something
	
	
	// constructor(s)
	public BankAccount() {
		super();
		owners = new ArrayList<Integer>();
		funds = 0;
		type = BankAccountType.NONE;
	}
	
	public BankAccount(int id) {
		super();
		super.setId(id);
		owners = new ArrayList<Integer>();
		funds = 0;
		type = BankAccountType.NONE;
	}
	
	// util methods

	@Override
	public String toString() {
		return "ACCOUNT " + super.getId(); 
	}
	
	// getters and setters
	
	
	// technically I'm not sure if the List should have getters/setters or just
	// add/remove methods, for now I'll keep both in.
	public List<Integer> getOwners() {
		return owners;
	}

	
	public void setOwners(List<Integer> owners) {
		this.owners = owners;
	}
	
	
	/**
	 * Prevents repeats
	 * @param ownerID
	 */
	public void addOwner(int ownerID) {
		if (!owners.contains(ownerID)) {
			owners.add(ownerID);
		}
	}
	
	
	public void removeOwner(int ownerID) {
		owners.remove(owners.indexOf(ownerID));
	}

	
	public BankAccountStatus getStatus() {
		return status;
	}

	
	public void setStatus(BankAccountStatus status) {
		this.status = status;
	}

	public int getFunds() {
		return funds;
	}

	public void setFunds(int funds) {
		this.funds = funds;
	}
	
	public BankAccountType getType() {
		return type;
	}

	public void setType(BankAccountType type) {
		this.type = type;
	}
}
