package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemMoviesData implements Serializable {

	private final String streamID;
	private final String name;
	private final String containerExtension;
	Boolean isDownload = false;

	/**
	 * Constructs an ItemMoviesData object.
	 *
	 * @param streamID The unique identifier for the movie stream
	 * @param name The title of the movie
	 * @param containerExtension The container format or extension of the movie file (e.g., .mp4, .mkv)
	 */

	public ItemMoviesData(String streamID, String name, String containerExtension) {
		this.streamID = streamID;
		this.name = name;
		this.containerExtension = containerExtension;
	}

	public String getStreamID() {
		return streamID;
	}

	public String getName() {
		return name;
	}

	public String getContainerExtension() {
		return containerExtension;
	}

	public void setDownload(Boolean isDownload) {
		this.isDownload = isDownload;
	}

	public Boolean isDownload() {
		return isDownload;
	}
}