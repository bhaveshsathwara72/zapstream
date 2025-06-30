package com.zapstream.player.app.item;

import java.io.Serializable;

public class ItemVideoDownload implements Serializable {

	private final String name;
	private final String streamID;
	private String streamIcon;
	private String videoURl;
	private final String containerExtension;
	private String tempName;
	private int progress = 0;

	/**
	 * Constructs an ItemVideoDownload object.
	 *
	 * @param name The name/title of the video
	 * @param streamID The stream ID of the video
	 * @param streamIcon The URL or path to the stream's icon/poster
	 * @param videoURl The URL of the video to download
	 * @param containerExtension The container format (e.g., ".mp4", ".mkv")
	 */

	public ItemVideoDownload(String name, String streamID, String streamIcon,
							 String videoURl, String containerExtension) {
		this.name = name;
		this.streamID = streamID;
		this.streamIcon = streamIcon;
		this.videoURl = videoURl;
		this.containerExtension = containerExtension;
	}

	public String getName() {
		return name;
	}

	public String getStreamID() {
		return streamID;
	}

	public String getContainerExtension() {
		return containerExtension;
	}

	public String getStreamIcon() {
		return streamIcon;
	}
	public void setStreamIcon(String streamIcon) {
		this.streamIcon = streamIcon;
	}

	public void setTempName(String tempName) {
		this.tempName = tempName;
	}
	public String getTempName() {
		return tempName;
	}

	public String getVideoURL() {
		return videoURl;
	}
	public void setVideoURL(String videoUrl) {
		this.videoURl = videoUrl;
	}

	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
}