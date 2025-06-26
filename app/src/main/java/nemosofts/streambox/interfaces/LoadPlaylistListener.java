package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemPlaylist;

public interface LoadPlaylistListener {

    /**
     * Called when the playlist loading process starts.
     */
    void onStart();

    /**
     * Called when the playlist loading process ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param msg A message related to the loading process (e.g., error message or status).
     * @param arrayListPlaylist A list of `ItemPlaylist` objects containing the loaded playlists.
     */
    void onEnd(String success, String msg, ArrayList<ItemPlaylist> arrayListPlaylist);
}