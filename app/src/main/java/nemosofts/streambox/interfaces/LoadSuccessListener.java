package nemosofts.streambox.interfaces;

public interface LoadSuccessListener {

    /**
     * Called when the loading process starts.
     */
    void onStart();

    /**
     * Called when the loading process ends.
     *
     * @param success Indicates whether the operation was successful ("1" for success, "0" for failure).
     * @param msg A message related to the outcome of the loading process (e.g., success message or error description).
     */
    void onEnd(String success, String msg);
}