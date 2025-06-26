package nemosofts.streambox.item;

import java.io.Serializable;

public class ItemNotification implements Serializable {

    private final String notificationID;
    private final String title;
    private final String msg;
    private final String description;
    private final String date;

    /**
     * Constructs an ItemNotification object.
     *
     * @param id The unique identifier for the notification
     * @param title The title of the notification
     * @param msg The message of the notification
     * @param description A detailed description of the notification
     * @param date The date when the notification was created
     */

    public ItemNotification(String id, String title, String msg, String description, String date) {
        this.notificationID = id;
        this.title = title;
        this.msg = msg;
        this.description = description;
        this.date = date;
    }

    public String getId() {
        return notificationID;
    }

    public String getTitle() {
        return title;
    }

    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}