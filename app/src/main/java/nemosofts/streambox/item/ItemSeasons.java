package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemSeasons implements Serializable {

	private final String name;
	private final String seasonNumber;

	/**
	 * Constructs an ItemSeasons object.
	 *
	 * @param name The name of the season (e.g., "Season 1")
	 * @param seasonNumber The season number (e.g., "1" for Season 1)
	 */

	public ItemSeasons(String name, String seasonNumber) {
		this.name = name;
		this.seasonNumber = seasonNumber;
	}

	public String getName() {
		return name;
	}

	public String getSeasonNumber() {
		return seasonNumber;
	}
}