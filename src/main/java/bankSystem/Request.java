package bankSystem;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single requested action/input/change from a user.
 * 
 * @author Andrew Curry
 *
 */
public class Request {

	// enum(s)
	public enum RequestType{
		REGISTER_USER, 
		LOG_IN,
		LOG_OUT,
		QUIT,
		APPLY_OPEN_ACCOUNT, 
		APPROVE_OPEN_ACCOUNT,
		DENY_OPEN_ACCOUNT,
		CLOSE_ACCOUNT,
		ADD_ACCOUNT_OWNER,
		REMOVE_ACCOUNT_OWNER,
		DEPOSIT,
		WITHDRAW,
		TRANSFER,
		VIEW_ACCOUNTS,
		VIEW_SELF_PROFILE,
		VIEW_USERS,
		VIEW_TRANSACTIONS,
		CREATE_EMPLOYEE,
		CREATE_ADMIN,
	}
	
	// instance variables
	private RequestType type;
	private List<String> params; // order WILl matter for this
	
	// constructor(s)
	
	public Request (RequestType type, List<String> params) {
		this.type = type;
		this.params = params;
	}
	
	public Request (RequestType type) {
		this.type = type;
		this.params = new ArrayList<>();
	}
	
	// getters
	
	public RequestType getType() {
		return this.type;
	}
	
	public List<String> getParams() {
		return params;
	}
}
