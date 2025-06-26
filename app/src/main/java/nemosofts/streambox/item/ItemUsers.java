package nemosofts.streambox.item;

public class ItemUsers {

	private final String id;
	private final String userType;
	private final String userName;
	private final String userPassword;
	private final String dnsBase;
	private final String deviceID;

	/**
	 * Constructs an ItemUsers object.
	 *
	 * @param id A unique identifier for the user
	 * @param userType The type of user (e.g., admin, viewer)
	 * @param userName The username used for login
	 * @param userPassword The password for the user
	 * @param dnsBase The DNS base or server address
	 * @param deviceID The unique ID of the user's device
	 */

	public ItemUsers(String id, String userType, String userName, String userPassword,
					 String dnsBase, String deviceID) {
		this.id = id;
		this.userType = userType;
		this.userName = userName;
		this.userPassword = userPassword;
		this.dnsBase = dnsBase;
		this.deviceID = deviceID;
	}

	public String getId() {
		return id;
	}

	public String getUserType() {
		return userType;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public String getDnsBase() {
		return dnsBase;
	}

	public String getDeviceID() {
		return deviceID;
	}
}