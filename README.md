**Description**
Leveraging Java 8, create an application that simulates simple banking transactions

**Requirements**
*	Build the application using Java 8
*	All interaction with the user should be done through the console using the `Scanner` class
*	Customers of the bank should be able to register with a username and password, and apply to open an account.
    * Stretch Goal: Customers should be able to apply for joint accounts
*	Once the account is open, customers should be able to withdraw, deposit, and transfer funds between accounts
    * All basic validation should be done, such as trying to input negative amounts, overdrawing from accounts etc.
*	Employees of the bank should be able to view all of their customers information. This includes:
    * Account information
    * Account balances
    * Personal information
*	Employees should be able to approve/deny open applications for accounts
*	Bank admins should be able to view and edit all accounts. This includes:
    * Approving/denying accounts
    * withdrawing, depositing, transferring from all accounts
    * canceling accounts
*	Reasonable test coverage for the service layer is expected using JUnit.
    * TDD is encouraged.
*	Logging should be accomplished using Log4J
    * All transactions should be logged
