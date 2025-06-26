package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemChannel;

public interface GetChannelListener {

    /**
     * Called when the channel data operation starts.
     */
    void onStart();

    /**
     * Called when the channel data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayListLive A list of `ItemChannel` objects containing the channel data.
     */
    void onEnd(String success, ArrayList<ItemChannel> arrayListLive);
}