package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemEpisodes;
import nemosofts.streambox.item.ItemInfoSeasons;
import nemosofts.streambox.item.ItemSeasons;

public interface SeriesIDListener {

    /**
     * Called to indicate the start of the series ID retrieval operation.
     */
    void onStart();

    /**
     * Called when the operation ends, providing success status and data related to the series and episodes.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param infoSeasons Information about the seasons of the series.
     * @param arrayListSeasons A list of seasons in the series.
     * @param arrayListEpisodes A list of episodes within the series.
     */
    void onEnd(String success, ItemInfoSeasons infoSeasons,
               ArrayList<ItemSeasons> arrayListSeasons, ArrayList<ItemEpisodes> arrayListEpisodes
    );
}