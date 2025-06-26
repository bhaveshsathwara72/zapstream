package nemosofts.streambox.item;

import androidx.annotation.DrawableRes;

import java.io.Serializable;

public class ItemSelect implements Serializable {

	private final String title;
	@DrawableRes
	public final int logoResId;
	public final Boolean isMore;

	/**
	 * Constructs an ItemSelect object.
	 *
	 * @param title The title of the item (e.g., "Popular Shows")
	 * @param logoResId The resource ID of the logo image
	 * @param isMore icon ic_arrow_right VISIBLE / GONE
	 */

	public ItemSelect(String title, @DrawableRes int logoResId, Boolean isMore) {
		this.title = title;
		this.logoResId = logoResId;
		this.isMore = isMore;
	}

	public String getTitle() {
		return title;
	}

	public int getLogoResId() {
		return logoResId;
	}

	public Boolean getIsMore() {
		return isMore;
	}
}