package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemCat;

public interface GetCategoryListener {

    /**
     * Called when the category data operation starts.
     */
    void onStart();

    /**
     * Called when the category data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayListCat A list of `ItemCat` objects containing the category data.
     */
    void onEnd(String success, ArrayList<ItemCat> arrayListCat);
}