package nemosofts.streambox.interfaces;

import java.util.ArrayList;

import nemosofts.streambox.item.ItemUsers;

public interface UsersListener {

    /**
     * Called to indicate the start of the user-related operation.
     */
    void onStart();

    /**
     * Called when the user-related operation ends, providing success status, verification status, message,
     * and a list of users.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param verifyStatus The verification status of the operation ("verified", "unverified", etc.).
     * @param message A message providing additional details about the operation.
     * @param arrayListUsers A list of `ItemUsers` representing the users involved in the operation.
     */
    void onEnd(String success,
               String verifyStatus, String message, ArrayList<ItemUsers> arrayListUsers
    );
}