package nemosofts.streambox.interfaces;

public interface InterAdListener {

    /**
     * Called when an interstitial ad is clicked.
     *
     * @param position The position of the ad in a list or grid.
     * @param type The type of the ad.
     */
    void onClick(int position, String type);
}