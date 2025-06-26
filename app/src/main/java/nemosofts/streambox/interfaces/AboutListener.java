package nemosofts.streambox.interfaces;

public interface AboutListener {

    /**
     * Called when the About operation starts.
     */
    void onStart();

    /**
     * Called when the About operation ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param verifyStatus Status of any verification logic involved (e.g., license verification).
     * @param message A message describing the result or any error.
     */
    void onEnd(String success, String verifyStatus, String message);
}