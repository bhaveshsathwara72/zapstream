package com.zapstream.player.app.item;

import java.io.Serializable;

public class ItemMovies implements Serializable {

	private final String name;
	private final String streamID;
	private final String streamIcon;
	private final String rating;
	private final String catName;

	/**
	 * Constructs an ItemMovies object.
	 *
	 * @param name The title of the movie
	 * @param streamID The unique identifier for the movie stream
	 * @param streamIcon The URL to the movie's stream icon or thumbnail
	 * @param rating The movie's rating (e.g., "PG-13", "5.6")
	 * @param catName The genre or category name of the movie
	 */

	public ItemMovies(String name, String streamID, String streamIcon, String rating, String catName) {
		this.name = name;
		this.streamID = streamID;
		this.streamIcon = streamIcon;
		this.rating = rating;
		this.catName = catName;
	}

	public String getName() {
		return name;
	}

	public String getStreamID() {
		return streamID;
	}

	public String getStreamIcon() {
		return streamIcon;
	}

	public String getRating() {
		return rating;
	}

	public String getCatName() {
		return catName;
	}
}