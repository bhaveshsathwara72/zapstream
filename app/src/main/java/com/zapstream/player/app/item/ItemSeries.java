package com.zapstream.player.app.item;

import java.io.Serializable;

public class ItemSeries implements Serializable {

	private final String name;
	private final String seriesID;
	private final String cover;
	private final String rating;

	/**
	 * Constructs an ItemSeries object.
	 *
	 * @param name The name/title of the series
	 * @param seriesID The unique identifier for the series
	 * @param cover The URL or path to the series cover image
	 * @param rating The rating of the series (e.g., "8.5", "PG-13")
	 */

	public ItemSeries(String name, String seriesID, String cover, String rating) {
		this.name = name;
		this.seriesID = seriesID;
		this.cover = cover;
		this.rating = rating;
	}

	public String getName() {
		return name;
	}

	public String getSeriesID() {
		return seriesID;
	}

	public String getCover() {
		return cover;
	}

	public String getRating() {
		return rating;
	}
}