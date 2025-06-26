package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemInfoMovies;
import nemosofts.streambox.item.ItemMoviesData;

public interface MovieIDListener {

    /**
     * Called when the movie ID request starts.
     */
    void onStart();

    /**
     * Called when the movie ID request ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayListInfo A list of `ItemInfoMovies` objects containing detailed movie information.
     * @param arrayListMoviesData A list of `ItemMoviesData` objects containing additional movie data.
     */
    void onEnd(String success, ArrayList<ItemInfoMovies> arrayListInfo ,
               ArrayList<ItemMoviesData> arrayListMoviesData
    );
}