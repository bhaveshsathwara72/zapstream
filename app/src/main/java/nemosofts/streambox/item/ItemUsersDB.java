package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemUsersDB implements Serializable {

	private final String id;
	private final String anyName;
	private final String userName;
	private final String userPass;
	private final String userUrl;
	private final String userType;

	/**
	 * Constructs an ItemUsersDB object.
	 *
	 * @param id A unique identifier for the user
	 * @param anyName A display-friendly name for the user
	 * @param userName The username used for login
	 * @param userPass The password for the user
	 * @param userUrl The server URL associated with the user
	 * @param userType The type of user (e.g., admin, viewer)
	 */

	public ItemUsersDB(String id, String anyName, String userName, String userPass,
					   String userUrl, String userType) {
		this.id = id;
		this.anyName = anyName;
		this.userName = userName;
		this.userPass = userPass;
		this.userUrl = userUrl;
		this.userType = userType;
	}

	public String getId() {
		return id;
	}

	public String getAnyName() {
		return anyName;
	}

	public String getUseName() {
		return userName;
	}

	public String getUserPass() {
		return userPass;
	}

	public String getUserURL() {
		return userUrl;
	}

	public String getUserType() {
		return userType;
	}
}