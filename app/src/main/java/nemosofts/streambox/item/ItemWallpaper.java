package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemWallpaper implements Serializable {

	private final int height;
	private final int width;
	private final String filePath;

	/**
	 * Constructs an ItemWallpaper object.
	 *
	 * @param height The height of the wallpaper in pixels
	 * @param width The width of the wallpaper in pixels
	 * @param filePath The local or remote file path of the wallpaper image
	 */

    public ItemWallpaper(int height, int width, String filePath) {
        this.height = height;
        this.width = width;
        this.filePath = filePath;
    }

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public String getFilePath() {
		return filePath;
	}
}