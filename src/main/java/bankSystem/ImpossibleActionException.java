/**
 * Used by the BankSystem when the user tries to do something forbidden - for example,
 * querying an invalid account number, or acting outside of their permissions.
 * 
 * @author Andrew Curry
 */
package bankSystem;

public class ImpossibleActionException extends Exception {
	
	static final long serialVersionUID = 0; // dunno, this fixed a warning

	// constructor(s)
	public ImpossibleActionException(String message) {
		super(message);
	}
}
