/**
 * The TransactionRecord class is used to encapsulate information about
 * transactions made with the banking system.
 */
package com.revature.bankDataObjects;

public class TransactionRecord extends BankData {
	
	
	// enums
	public enum TransactionType {
		ACCOUNT_REGISTERED, 
		ACCOUNT_APPROVED, 
		ACCOUNT_CLOSED, 
		FUNDS_TRANSFERRED,
		FUNDS_DEPOSITED, 
		FUNDS_WITHDRAWN, 
		USER_REGISTERED, 
		ACCOUNT_OWNER_ADDED, 
		ACCOUNT_OWNER_REMOVED, 
		NONE
	}
	
	
	// instance variables
	// id in super
	private String time;
	private TransactionType type;
	private int actingUser; // who triggered it
	private int sourceAccount; // might not be used in all transaction types
	private int destinationAccount; // might not be used in all transaction types
	private int moneyAmount; // might not be used in all transaction types
	
	
	// constructors
	public TransactionRecord() {
		super();
		type = TransactionType.NONE;
	}
	
	
	public TransactionRecord(int id) {
		super();
		super.setId(id);
		actingUser = -1;
		sourceAccount = -1;
		destinationAccount = -1;
		moneyAmount = -1;
		type = TransactionType.NONE;
	}
	
	
	// util methods
	
	
	@Override
	public String toString() {
		return "TRANSACTION " + super.getId();
	}


	// getters and setters
	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public TransactionType getType() {
		return type;
	}


	public void setType(TransactionType type) {
		this.type = type;
	}


	public int getActingUser() {
		return actingUser;
	}


	public void setActingUser(int actingUser) {
		this.actingUser = actingUser;
	}


	public int getSourceAccount() {
		return sourceAccount;
	}


	public void setSourceAccount(int sourceAccount) {
		this.sourceAccount = sourceAccount;
	}


	public int getDestinationAccount() {
		return destinationAccount;
	}


	public void setDestinationAccount(int destinationAccount) {
		this.destinationAccount = destinationAccount;
	}


	public int getMoneyAmount() {
		return moneyAmount;
	}


	public void setMoneyAmount(int moneyAmount) {
		this.moneyAmount = moneyAmount;
	}
	
	
}
