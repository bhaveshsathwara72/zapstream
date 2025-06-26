package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemEpisodes implements Serializable {

	private final String id;
	private final String title;
	private final String containerExtension;
	private final String season;
	private final String plot;
	private final String duration;
	private final String rating;
	private final String coverBig;

	/**
	 * Constructs an ItemEpisodes object.
	 *
	 * @param id Unique identifier
	 * @param title Episode title
	 * @param containerExtension File extension (e.g., "mp4")
	 * @param season Season number
	 * @param plot Episode plot summary
	 * @param duration Duration of the episode
	 * @param rating User or critic rating (e.g., "4.5")
	 * @param coverBig URL to cover image
	 */

	public ItemEpisodes(String id, String title, String containerExtension,
						String season, String plot, String duration,
						String rating, String coverBig) {
		this.id = id;
		this.title = title;
		this.containerExtension = containerExtension;
		this.season = season;
		this.plot = plot;
		this.duration = duration;
		this.rating = rating;
		this.coverBig = coverBig;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContainerExtension() {
		return containerExtension;
	}

	public String getSeason() {
		return season;
	}

	public String getPlot() {
		return plot;
	}

	public String getDuration() {
		return duration;
	}

	public String getRating() {
		return rating;
	}

	public String getCoverBig() {
		return coverBig;
	}
}