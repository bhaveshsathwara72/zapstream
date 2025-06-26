package nemosofts.streambox.item;

public class ItemVideo {

    private String title;
    private String path;

    /**
     * Constructs an ItemVideo object.
     *
     * @param title The title or name of the video
     * @param path The file path or URL to the video
     */

    public ItemVideo(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}