package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemLoginUser implements Serializable {

	private final String username;
	private final String password;
	private final String message;
	private final String status;
	private final String expDate;
	private final String activeCons;
	private final String maxCons;

	/**
	 * Constructs an ItemLoginUser object.
	 *
	 * @param username The username for the login
	 * @param password The password associated with the username
	 * @param message A message, such as an error or success message related to the login attempt
	 * @param status The status of the user (e.g., active, inactive)
	 * @param expDate The expiration date of the user account
	 * @param activeCons The number of active connections currently being used by the user
	 * @param maxConnections The maximum number of allowed connections for the user
	 */

    public ItemLoginUser(String username, String password, String message, String status,
						 String expDate, String activeCons, String maxConnections) {
        this.username = username;
        this.password = password;
        this.message = message;
        this.status = status;
        this.expDate = expDate;
        this.activeCons = activeCons;
        this.maxCons = maxConnections;
    }

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getMessage() {
		return message;
	}

	public String getStatus() {
		return status;
	}

	public String getExpDate() {
		return expDate;
	}

	public String getActiveCons() {
		return activeCons;
	}

	public String getMaxConnections() {
		return maxCons;
	}
}