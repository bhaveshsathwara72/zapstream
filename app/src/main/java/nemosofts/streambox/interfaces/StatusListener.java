package nemosofts.streambox.interfaces;

public interface StatusListener {

    /**
     * Called to indicate the start of the status operation.
     */
    void onStart();

    /**
     * Called when the status operation ends, providing success status and messages.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param registerSuccess Indicates if the registration was successful ("1" for success, "0" for failure).
     * @param message A message providing additional details about the operation.
     */
    void onEnd(String success, String registerSuccess, String message);
}