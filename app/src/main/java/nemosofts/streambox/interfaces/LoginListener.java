package nemosofts.streambox.interfaces;

import nemosofts.streambox.item.ItemLoginServer;
import nemosofts.streambox.item.ItemLoginUser;

public interface LoginListener {

    /**
     * Called when the login process starts.
     */
    void onStart();

    /**
     * Called when the login process ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param itemLoginUser An instance of the ItemLoginUser class containing user login information.
     * @param itemLoginServer An instance of the ItemLoginServer class containing server-related information.
     * @param allowedOutputFormats A string representing the allowed output formats (e.g., "m3u8, ts").
     */
    void onEnd(String success, ItemLoginUser itemLoginUser,
               ItemLoginServer itemLoginServer, String allowedOutputFormats
    );
}