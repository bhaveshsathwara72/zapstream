package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemEpg;

public interface EpgListener {

    /**
     * Called when the EPG data operation starts.
     */
    void onStart();

    /**
     * Called when the EPG data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param epgArrayList A list of `ItemEpg` objects containing the EPG data.
     */
    void onEnd(String success, ArrayList<ItemEpg> epgArrayList);
}