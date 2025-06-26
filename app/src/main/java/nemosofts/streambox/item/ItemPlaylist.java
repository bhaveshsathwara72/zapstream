package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemPlaylist implements Serializable {

	private final String playlistName;
	private final String logo;
	private final String group;
	private final String url;

	/**
	 * Constructs an ItemPlaylist object.
	 *
	 * @param name The name of the playlist
	 * @param logo The URL or path to the logo image of the playlist
	 * @param group The group or category that the playlist belongs to
	 * @param url The URL of the playlist (where it can be accessed or played)
	 */

	public ItemPlaylist(String name, String logo, String group, String url) {
		this.playlistName = name;
		this.logo = logo;
		this.group = group;
		this.url = url;
	}

	public String getName() {
		return playlistName;
	}

	public String getLogo() {
		return logo;
	}

	public String getGroup() {
		return group;
	}

	public String getUrl() {
		return url;
	}
}