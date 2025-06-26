package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemEpgFull;

public interface EpgFullListener {

    /**
     * Called when the full EPG data operation starts.
     */
    void onStart();

    /**
     * Called when the full EPG data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param epgArrayList A list of `ItemEpgFull` objects containing the EPG data.
     */
    void onEnd(String success, ArrayList<ItemEpgFull> epgArrayList);
}