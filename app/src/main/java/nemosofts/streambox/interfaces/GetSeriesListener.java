package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemSeries;

public interface GetSeriesListener {

    /**
     * Called when the series data operation starts.
     */
    void onStart();

    /**
     * Called when the series data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayListSeries A list of `ItemSeries` objects containing the series data.
     */
    void onEnd(String success, ArrayList<ItemSeries> arrayListSeries);
}