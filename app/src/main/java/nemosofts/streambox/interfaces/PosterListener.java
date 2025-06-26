package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemPoster;

public interface PosterListener {

    /**
     * Called when the poster request starts.
     */
    void onStart();

    /**
     * Called when the poster request ends.
     *
     * @param success A string indicating whether the operation was successful ("1" for success, "0" for failure).
     * @param verifyStatus A string indicating the verification status (e.g., "verified", "not_verified").
     * @param message A message providing additional details about the operation.
     * @param arrayList A list of `ItemPoster` objects containing the poster data.
     */
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemPoster> arrayList);
}