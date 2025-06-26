package nemosofts.streambox.interfaces;

public interface DataListener {

    /**
     * Called when the data operation starts.
     */
    void onStart();

    /**
     * Called when the data operation ends.
     *
     * @param success Indicates the result of the operation ("1" for success, "0" for failure).
     */
    void onEnd(String success);
}