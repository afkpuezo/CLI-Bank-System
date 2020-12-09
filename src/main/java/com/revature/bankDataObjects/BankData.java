/**
 * the BankData class represents things like accounts, user profiles, and transactions -
 * individual nuggets of data that the program will need to run. Each specific type of
 * data will have its own class which extends this one.
 * 
 * Andrew Curry, Project 0
 */
package com.revature.bankDataObjects;

public abstract class BankData {
	
	// instance variables
	private int id;

	
	// getset
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
