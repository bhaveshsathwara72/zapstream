package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemWallpaper;

public interface WallpaperListener {

    /**
     * Called to indicate the start of the wallpaper-related operation.
     */
    void onStart();

    /**
     * Called when the wallpaper-related operation ends, providing success status and a list of wallpapers.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param arrayList A list of `ItemWallpaper` representing the available wallpapers.
     */
    void onEnd(String success, ArrayList<ItemWallpaper> arrayList);
}