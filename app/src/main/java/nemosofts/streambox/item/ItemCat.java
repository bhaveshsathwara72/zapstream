package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemCat implements Serializable {
	
	private final String id;
	private final String name;
	private final String page;

	private boolean isChecked;

	/**
	 * Constructs an ItemCat object.
	 *
	 * @param id Unique identifier for the category
	 * @param name Category name
	 * @param page page
	 */

	public ItemCat(String id, String name, String page) {
		this.id = id;
		this.name = name;
		this.page = page;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}

	public void setCheckbox(Boolean checked) {
		this.isChecked = checked;
	}
	public Boolean isChecked() {
		return isChecked;
	}
}