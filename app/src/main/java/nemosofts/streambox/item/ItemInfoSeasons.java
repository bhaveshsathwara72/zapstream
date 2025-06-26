package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemInfoSeasons implements Serializable {

	private final String seasonsName;
	private final String cover;
	private final String plot;
	private final String director;
	private final String genre;
	private final String releaseDate;
	private final String rating;
	private final String rating5based;
	private final String youtubeTrailer;

	/**
	 * Constructs an ItemInfoSeasons object.
	 *
	 * @param name Title of the season
	 * @param cover URL to the season poster
	 * @param plot Season plot summary
	 * @param director Director name(s)
	 * @param genre Season genre(s)
	 * @param releaseDate Season release date (e.g., "YYYY-MM-DD")
	 * @param rating General rating of the season (e.g., "PG-13", "5.6")
	 * @param rating5based Rating on a 5-point scale (e.g., "4.5")
	 * @param youtubeTrailer URL to the season trailer
	 */

	public ItemInfoSeasons(String name, String cover, String plot, String director, String genre,
						   String releaseDate, String rating, String rating5based, String youtubeTrailer) {
		this.seasonsName = name;
		this.cover = cover;
		this.plot = plot;
		this.director = director;
		this.genre = genre;
		this.releaseDate = releaseDate;
		this.rating = rating;
		this.rating5based = rating5based;
		this.youtubeTrailer = youtubeTrailer;
	}

	public String getName() {
		return seasonsName;
	}

	public String getCover() {
		return cover;
	}

	public String getPlot() {
		return plot;
	}

	public String getDirector() {
		return director;
	}

	public String getGenre() {
		return genre;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public String getRating() {
		return rating;
	}

	public String getRating5based() {
		return rating5based;
	}

	public String getYoutubeTrailer() {
		return youtubeTrailer;
	}
}