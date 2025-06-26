package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemRadioButton implements Serializable {

	private final int id;
	private final String btnName;

	/**
	 * Constructs an ItemRadioButton object.
	 *
	 * @param id The unique identifier for the radio button
	 * @param name The name or label of the radio button
	 */

	public ItemRadioButton(int id, String name) {
		this.id = id;
		this.btnName = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return btnName;
	}
}