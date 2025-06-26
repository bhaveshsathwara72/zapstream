package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemMovies;

public interface GetMovieListener {

    /**
     * Called when the movie data operation starts.
     */
    void onStart();

    /**
     * Called when the movie data operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayListMovies A list of `ItemMovies` objects containing the movie data.
     */
    void onEnd(String success, ArrayList<ItemMovies> arrayListMovies);
}