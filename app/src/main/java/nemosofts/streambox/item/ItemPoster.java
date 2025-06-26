package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemPoster implements Serializable {

	private final String posterData;

	/**
	 * Constructs an ItemPoster object.
	 *
	 * @param poster  URL to poster image
	 */

	public ItemPoster(String poster) {
		this.posterData = poster;
	}

	public String getPoster() {
		return posterData;
	}
}