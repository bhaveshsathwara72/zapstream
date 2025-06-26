package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemSingleURL implements Serializable {

	private final String id;
	private final String anyName;
	private final String singleUrl;

	/**
	 * Constructs an ItemSingleURL object.
	 *
	 * @param id A unique identifier for the URL entry
	 * @param anyName A user-friendly name or label for the URL
	 * @param singleUrl The actual URL string
	 */

	public ItemSingleURL(String id, String anyName, String singleUrl) {
		this.id = id;
		this.anyName = anyName;
		this.singleUrl = singleUrl;
	}

	public String getId() {
		return id;
	}

	public String getAnyName() {
		return anyName;
	}

	public String getSingleURL() {
		return singleUrl;
	}
}