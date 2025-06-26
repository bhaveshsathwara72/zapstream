package nemosofts.streambox.interfaces;

public interface RewardAdListener {

    /**
     * Called to indicate whether the reward ad is playing or not.
     *
     * @param playWhenReady A Boolean indicating if the reward ad is playing.
     */
    void isPlaying(Boolean playWhenReady);
}
