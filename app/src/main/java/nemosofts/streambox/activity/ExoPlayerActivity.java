package nemosofts.streambox.activity;


import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.FormatUtils;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.nemosofts.utils.PlayerAPI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import nemosofts.streambox.BuildConfig;
import nemosofts.streambox.R;
import nemosofts.streambox.adapter.AdapterEpisodesPlayer;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.item.ItemEpisodes;
import nemosofts.streambox.item.ItemMovies;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.encrypter.EncryptedFileDataSourceFactory;
import nemosofts.streambox.utils.helper.DBHelper;
import nemosofts.streambox.utils.helper.Helper;
import nemosofts.streambox.utils.helper.SPHelper;
import nemosofts.streambox.utils.player.BrightnessVolumeControl;
import nemosofts.streambox.utils.player.CustomPlayerView;
import nemosofts.streambox.utils.player.PlayerUtils;


@UnstableApi
public class ExoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "ExoPlayerActivity";
    public static final String TAG_TYPE_MOVIE = "movie";
    public static final String TAG_TYPE_EPISODES = "episodes";
    public static final String TAG_TYPE_LOCAL = "local";
    public static final String TAG_TYPE_SINGLE_URL = "single";
    public static final String TAG_TYPE_DOWNLOAD = "download";

    private Helper helper;
    private DBHelper dbHelper;
    private SPHelper spHelper;
    private boolean isTvBox;

    // Movie Data ----------------------------------------------------------------------------------
    private String movieID = "";
    private String movieContainer = ".mp4";
    private String movieRating = "";
    private String moviePoster = "";

    // Episodes Data -------------------------------------------------------------------------------
    private LinearLayout skipNext;
    private boolean isVisibleDialogEpisodes = false;

    // Video Data ----------------------------------------------------------------------------------
    private String videoUrl = "";

    // Download Data ----------------------------------------------------------------------------------
    Cipher mCipher = null;
    SecretKeySpec secretKeySpec;
    byte[] secretKey = BuildConfig.ENC_KEY.getBytes();
    byte[] initialIv = BuildConfig.IV.getBytes();

    // Player --------------------------------------------------------------------------------------
    private String playerType = "";
    private String playerTitle = "";

    private CustomPlayerView playerView;
    private ExoPlayer exoPlayer;
    private DefaultBandwidthMeter bandwidthMeter;
    private DataSource.Factory mediaDataSourceFactory;
    private MediaSession mediaSession;
    private PlayerListener playerListener;
    private LoudnessEnhancer loudnessEnhancer;
    private BroadcastReceiver batteryReceiver;

    private ProgressBar pb;
    private int playback = 0;
    private int resize = 1;
    private boolean isInPicInPicMode = false;
    private boolean controllerVisible;
    private boolean controllerVisibleFully;
    private AudioManager mAudioManager;
    private int boostLevel = 0;
    private boolean isVisibleDialogInfo = false;

    // RewardAd ------------------------------------------------------------------------------------
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    // Cast ----------------------------------------------------------------------------------------
    private CastContext castContext;
    private CastSession castSession;
    private SessionManager sessionManager;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private final SessionManagerListener<CastSession> sessionManagerListener = new SessionManagerListener<>() {
        @Override
        public void onSessionStarted(@NonNull CastSession session, @NonNull String sessionId) {
            if (isFinishing()){
                return;
            }
            ApplicationUtil.log(TAG, "Cast session started: " + sessionId);
            castSession = session;

            // Pause local player when casting starts
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                exoPlayer.pause();
            }
            castVideo();
        }

        @Override
        public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
            if (isFinishing()){
                return;
            }
            ApplicationUtil.log(TAG, "Cast session resumed");
            castSession = session;

            // If returning from suspension, recast the video
            if (wasSuspended) {

                // Pause local player when casting starts
                if (exoPlayer != null && exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                }
                castVideo();
            }
        }

        @Override
        public void onSessionStarting(@NonNull CastSession session) {
            ApplicationUtil.log(TAG, "Cast session starting...");
        }

        @Override
        public void onSessionEnding(@NonNull CastSession session) {
            ApplicationUtil.log(TAG, "Cast session ending...");
        }

        @Override
        public void onSessionEnded(@NonNull CastSession session, int error) {
            if (isFinishing()){
                return;
            }
            ApplicationUtil.log(TAG, "Cast session ended. Error: " + error);
            castSession = null;
        }

        @Override
        public void onSessionResumeFailed(@NonNull CastSession session, int error) {
            ApplicationUtil.log(TAG, "Cast session resume failed: " + error);
        }

        @Override
        public void onSessionResuming(@NonNull CastSession session, @NonNull String sessionId) {
            ApplicationUtil.log(TAG, "Cast session resuming...");
        }

        @Override
        public void onSessionStartFailed(@NonNull CastSession session, int error) {
            ApplicationUtil.log(TAG, "Cast session start failed: " + error);
        }

        @Override
        public void onSessionSuspended(@NonNull CastSession session, int reason) {
            ApplicationUtil.log(TAG, "Cast session suspended. Reason: " + reason);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        EdgeToEdge.enable(this);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.hideStatusBar(this);
        IfSupported.playerHideStatusBar(this);

        if (Build.VERSION.SDK_INT >= 35) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        helper = new Helper(this);
        dbHelper = new DBHelper(this);
        spHelper = new SPHelper(this);
        isTvBox = DeviceUtils.isTvBox(this);

        playerType = getIntent().getStringExtra("player_type");
        getPlayerData();

        pb = findViewById(R.id.exo_pb_player);
        skipNext = findViewById(R.id.exo_skip_next);
        skipNext.setVisibility(View.GONE);

        timeLeftInMillis = Callback.getRewardMinutes() * 60 * 1000; // minutes in milliseconds

        // Set default cookie manager if not already set
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        if (playerType.equals(TAG_TYPE_DOWNLOAD)){
            if (mCipher == null) {
                String aes = "AES";
                String tr = "AES/CTR/NoPadding";
                secretKeySpec = new SecretKeySpec(secretKey, aes);
                try {
                    mCipher = Cipher.getInstance(tr);
                    mCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(initialIv));
                } catch (Exception e) {
                    Log.e(TAG, "Error onCreate mCipher: ", e);
                }
            }
            mediaDataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, secretKeySpec, new IvParameterSpec(initialIv), bandwidthMeter);
        } else {
            mediaDataSourceFactory = buildDataSourceFactory(true);
        }

        setUpPlayer();
        setUpPlayerControls();

        runOnUiThread(this::setMediaSource);

        if (isTvBox){
            findViewById(R.id.back_player).setVisibility(View.GONE);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        MediaRouteButton mediaRouteButton = findViewById(R.id.media_route_button);
        if (isTvBox || TAG_TYPE_LOCAL.equals(playerType) || TAG_TYPE_DOWNLOAD.equals(playerType)) {
            mediaRouteButton.setVisibility(View.GONE);
        } else {
            castContext = CastContext.getSharedInstance(this);
            sessionManager = castContext.getSessionManager();
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
            mediaRouteButton.setVisibility(View.VISIBLE);
        }

        setupBackPressHandler();
    }

    private void castVideo() {
        if (castSession == null || !castSession.isConnected()) {
            Toast.makeText(this, "Cast device not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;
        if (playerType.equals(TAG_TYPE_MOVIE)) {
            url = PlayerAPI.getMovieURL(spHelper.getServerURL(),spHelper.getUserName(),
                    spHelper.getPassword(),movieID,movieContainer);
        } else if (playerType.equals(TAG_TYPE_EPISODES)) {
            String id = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes()).getId();
            String extension = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes()).getContainerExtension();
            url = PlayerAPI.getEpisodeURL(spHelper.getServerURL(),spHelper.getUserName(),
                    spHelper.getPassword(),id, extension);
            ItemEpisodes episodes = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes());
            playerTitle = episodes.getTitle();
        } else {
            url = videoUrl;
        }

        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        metadata.putString(MediaMetadata.KEY_TITLE, playerTitle);
        metadata.putString(MediaMetadata.KEY_SUBTITLE, getString(R.string.app_name));

        MediaInfo mediaInfo = new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(FormatUtils.getMimeType(url))
                .setMetadata(metadata)
                .build();

        MediaLoadOptions loadOptions = new MediaLoadOptions.Builder()
                .setAutoplay(true)
                .build();

        if (castSession.getRemoteMediaClient() == null) {
            Toast.makeText(this, "Cast device not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        castSession.getRemoteMediaClient().load(mediaInfo, loadOptions);
    }

    private void setUpPlayerControls() {
        setBatteryInfo();
        findViewById(R.id.exo_resize).setOnClickListener(v -> setResize());
        findViewById(R.id.exo_media_info).setOnClickListener(v -> visibleDialogInfo(true));
        findViewById(R.id.back_player).setOnClickListener(v -> finish());

        ImageView controllerClose = findViewById(R.id.exo_controller_close);
        if (!isTvBox){
            controllerClose.setOnClickListener(v -> playerView.hideController());
        } else {
            if (controllerClose.getVisibility() != View.GONE){
                controllerClose.setVisibility(View.GONE);
            }
        }

        if (playerType.equals(TAG_TYPE_EPISODES)){
            skipNext.setVisibility(View.GONE);
            skipNext.setOnClickListener(v -> next());
            findViewById(R.id.exo_episodes).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_episodes).setOnClickListener(view -> {
                playerView.hideController();
                skipNext.setVisibility(View.GONE);
                visibleDialogEpisodes(true);
            });
        }

        // Set up the pip button
        ImageView pipButton  = findViewById(R.id.exo_pip);
        if (isPipSupported()) {
            pipButton.setVisibility(View.VISIBLE);
            pipButton.setOnClickListener(v -> enterPipMode());
        } else {
            pipButton.setVisibility(View.GONE);
        }
    }

    private void enterPipMode() {
        if (exoPlayer == null || playerView == null || !isPipSupported()) {
            Toasty.makeText(this, true, "PiP mode is not supported or player is not ready.", Toasty.ERROR);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode()) {
            final Format format = exoPlayer.getVideoFormat();
            if (format != null) {
                // ExoPlayer issue: https://github.com/google/ExoPlayer/issues/8611
                View videoSurfaceView = playerView.getVideoSurfaceView();
                if (videoSurfaceView instanceof SurfaceView surfaceView && format.width > 0 && format.height > 0) {
                    surfaceView.getHolder().setFixedSize(format.width, format.height);
                }
            }

            final Rational rationalLimitWide = new Rational(239, 100);
            final Rational rationalLimitTall = new Rational(100, 239);

            Rational rational = ApplicationUtil.getRational(format);
            if (rational.floatValue() > rationalLimitWide.floatValue()) {
                rational = rationalLimitWide;
            } else if (rational.floatValue() < rationalLimitTall.floatValue()) {
                rational = rationalLimitTall;
            }

            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(rational)
                    .build();

            enterPictureInPictureMode(params);
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isInPicInPicMode = isInPictureInPictureMode;
        if (playerView == null) {
            return;
        }
        if (isInPictureInPictureMode) {
            playerView.hideController();
            playerView.setControllerAutoShow(false);
        } else {
            playerView.setControllerAutoShow(true);
            playerView.showController();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPipSupported() && exoPlayer != null && exoPlayer.isPlaying()) {
            enterPipMode();
        }
    }

    private boolean isPipSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    private void visibleDialogInfo(boolean visible) {
        if (visible){
            if (exoPlayer == null) {
                return;
            }
            boolean isPlaying = exoPlayer.getPlayWhenReady();
            boolean hasVideo = exoPlayer.getVideoFormat() != null;
            if (isPlaying && hasVideo) {
                playerView.hideController();
                playerView.setPadding(100,100,100,100);
                isVisibleDialogInfo = true;
                findViewById(R.id.ll_media).setVisibility(View.VISIBLE);
                findViewById(R.id.vw_close).setVisibility(View.VISIBLE);
                findViewById(R.id.vw_close).setOnClickListener(v -> visibleDialogInfo(false));
                findViewById(R.id.back_player_info).setOnClickListener(v -> closeDialog());
                if (isTvBox){
                    findViewById(R.id.back_player_info).requestFocus();
                }
                String infoVideo = ApplicationUtil.getInfoVideo(exoPlayer, false);
                TextView mediaVideo = findViewById(R.id.text_info_video);
                mediaVideo.setText(infoVideo);

                String infoAudio = ApplicationUtil.getInfoAudio(exoPlayer);
                TextView mediaAudio = findViewById(R.id.text_info_audio);
                mediaAudio.setText(infoAudio);
            } else {
                Toasty.makeText(this,true, getString(R.string.please_wait_a_minute), Toasty.ERROR);
            }
        } else {
            playerView.setPadding(0,0,0,0);
            isVisibleDialogInfo = false;
            findViewById(R.id.vw_close).setVisibility(View.GONE);
            findViewById(R.id.ll_media).setVisibility(View.GONE);
        }
    }

    private void visibleDialogEpisodes(boolean visible) {
        if (visible){
            playerView.hideController();
            playerView.setPadding(100,100,100,100);
            isVisibleDialogEpisodes = true;
            findViewById(R.id.vw_close).setVisibility(View.VISIBLE);
            findViewById(R.id.vw_close).setOnClickListener(v -> closeDialog());

            RecyclerView rv = findViewById(R.id.rv_dialog);
            LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
            AdapterEpisodesPlayer adapter = new AdapterEpisodesPlayer(this, Callback.getArrayListEpisodes(), (item, position) -> {
                Callback.setPlayPosEpisodes(position);
                setMediaSource();
            });
            rv.setAdapter(adapter);
            rv.scrollToPosition(Callback.getPlayPosEpisodes());
            adapter.select(Callback.getPlayPosEpisodes());
            rv.setVisibility(View.VISIBLE);
            if (isTvBox){
                rv.requestFocus();
            }
        } else {
            playerView.setPadding(0,0,0,0);
            isVisibleDialogEpisodes = false;
            findViewById(R.id.vw_close).setVisibility(View.GONE);
            findViewById(R.id.rv_dialog).setVisibility(View.GONE);
        }
    }

    private void closeDialog() {
        if (isVisibleDialogEpisodes){
            visibleDialogEpisodes(false);
        } else if (isVisibleDialogInfo){
            visibleDialogInfo(false);
        }
    }

    private void setResize() {
        if (exoPlayer == null || playerView == null) {
            return;
        }

        switch (resize) {
            case 1:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                resize = 2;
                break;
            case 2:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                resize = 3;
                break;
            case 3:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                resize = 1;
                break;
            default:
                resize = 1;
        }

        exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
        playerView.showController();
    }

    private void setBatteryInfo() {
        ImageView batteryInfo = findViewById(R.id.exo_battery_info);
        if (!isTvBox){
            batteryReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    batteryInfo.setImageResource(ApplicationUtil.getBatteryDrawable(status,level,scale));
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batteryReceiver, filter);
        } else {
            batteryInfo.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpPlayer() {
        // https://github.com/google/ExoPlayer/issues/8571
        DefaultExtractorsFactory extractorsFactory = PlayerUtils.getDefaultExtractorsFactory();
        DefaultRenderersFactory renderersFactory = PlayerUtils.getDefaultRenderersFactory(this, spHelper.isHardwareDecoding());

        DefaultTrackSelector trackSelector = PlayerUtils.getDefaultTrackSelector(this);

        // Build ExoPlayer instance
        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .build();

        // Set audio attributes for the player
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        exoPlayer.setAudioAttributes(audioAttributes, true);
        exoPlayer.setHandleAudioBecomingNoisy(!isTvBox);

        setMediaSession();

        // Attach ExoPlayer to the player view
        playerView = findViewById(R.id.nSoftsPlayerView);
        playerView.setPlayer(exoPlayer);
        playerView.setShowVrButton(spHelper.getIsVR());
        playerView.setShowSubtitleButton(spHelper.getIsSubtitle());
        playerView.setShowFastForwardButton(true);
        playerView.setShowRewindButton(true);
        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerAutoShow(true);

        playerView.setBrightnessControl(new BrightnessVolumeControl(this));

        // Set controller visibility listener
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            controllerVisible = visibility == View.VISIBLE;
            controllerVisibleFully = playerView.isControllerFullyVisible();

            if (isInPicInPicMode){
                findViewById(R.id.player_toolbar).setVisibility(View.INVISIBLE);
                if (playerType.equals(TAG_TYPE_EPISODES) && skipNext.getVisibility() != View.GONE){
                    skipNext.setVisibility(View.GONE);
                }
                return;
            }

            findViewById(R.id.player_toolbar).setVisibility(visibility);
            if (playerType.equals(TAG_TYPE_EPISODES) && Callback.getPlayPosEpisodes() < (Callback.getArrayListEpisodes().size())) {
                skipNext.setVisibility(visibility);
            }

            if (isTvBox && visibility == View.VISIBLE) {
                // Because when using dpad controls, focus resets to first item in bottom controls bar
                try {
                    findViewById(androidx.media3.ui.R.id.exo_play_pause).requestFocus();
                } catch (Exception e) {
                    ApplicationUtil.log(TAG, "Error play pause requestFocus");
                }
            }
        });

        PlayerUtils.setCustomTrackNameProvider(this, playerView);
        PlayerUtils.setCustomSubtitle(this, playerView);

        // Set player event listeners
        playerListener = new PlayerListener();
        exoPlayer.addListener(playerListener);
    }

    private void setMediaSource() {
        if (!playerType.equals(TAG_TYPE_LOCAL) && !playerType.equals(TAG_TYPE_DOWNLOAD) && !NetworkUtils.isConnected(this)){
            Toasty.makeText(ExoPlayerActivity.this,true, getString(R.string.err_internet_not_connected), Toasty.ERROR);
            return;
        }

        if (playerType.equals(TAG_TYPE_EPISODES) && Callback.getArrayListEpisodes().isEmpty()){
            Toasty.makeText(ExoPlayerActivity.this,true, getString(R.string.err_no_data_found), Toasty.ERROR);
            return;
        }

        TextView title = findViewById(R.id.player_title);

        Uri uri = null;

        int currentPosition = 0;
        switch (playerType) {
            case TAG_TYPE_MOVIE -> {
                uri = Uri.parse(PlayerAPI.getMovieURL(spHelper.getServerURL(),spHelper.getUserName(),
                        spHelper.getPassword(),movieID,movieContainer)
                );
                currentPosition = dbHelper.getSeek(DBHelper.TABLE_SEEK_MOVIE, movieID, playerTitle);
                title.setText(playerTitle);
            }
            case TAG_TYPE_EPISODES -> {
                String id = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes()).getId();
                String extension = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes()).getContainerExtension();
                uri = Uri.parse(PlayerAPI.getEpisodeURL(spHelper.getServerURL(),spHelper.getUserName(),
                        spHelper.getPassword(),id, extension)
                );
                ItemEpisodes episodes = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes());
                currentPosition = dbHelper.getSeek(DBHelper.TABLE_SEEK_EPISODES, episodes.getId(), episodes.getTitle());
                playerTitle = episodes.getTitle();
                title.setText(playerTitle);
            }
            case TAG_TYPE_LOCAL, TAG_TYPE_SINGLE_URL, TAG_TYPE_DOWNLOAD -> {
                uri = Uri.parse(videoUrl);
                title.setText(playerTitle);
            }
            default -> title.setText(playerTitle);
        }

        if (uri == null){
            return;
        }
        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.setMediaSource(mediaSource);

        setLoudnessEnhancer(exoPlayer.getAudioSessionId());

        exoPlayer.seekTo(currentPosition);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.prepare();

        addToRecent();
    }

    private class PlayerListener implements Player.Listener {

        @Override
        public void onAudioSessionIdChanged(int audioSessionId) {
            Player.Listener.super.onAudioSessionIdChanged(audioSessionId);
            if (isFinishing()){
                return;
            }
            setLoudnessEnhancer(audioSessionId);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Player.Listener.super.onIsPlayingChanged(isPlaying);
            if (isFinishing()){
                return;
            }
            playerView.setKeepScreenOn(isPlaying);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            Player.Listener.super.onPlaybackStateChanged(state);
            if (isFinishing()){
                return;
            }

            if (state == Player.STATE_READY) {
                pb.setVisibility(View.GONE);
                playback = 1;
                startTimerRewardAd();
            } else if (state == Player.STATE_BUFFERING) {
                pb.setVisibility(View.VISIBLE);
            } else if (state == Player.STATE_ENDED){
                removeSeek();
            }
        }
        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            if (isFinishing()){
                return;
            }
            if (playback < 5){
                playback = playback + 1;
                Toast.makeText(ExoPlayerActivity.this,"Playback error - "+ playback +"/5 ", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isFinishing()){
                        return;
                    }
                    setMediaSource();
                }, 600);
            } else {
                playback = 1;
                exoPlayer.stop();
                pb.setVisibility(View.GONE);
                Toast.makeText(ExoPlayerActivity.this,"Failed : " + error.getErrorCodeName(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
            Player.Listener.super.onVideoSizeChanged(videoSize);
            View videoSurfaceView = playerView.getVideoSurfaceView();
            if (videoSurfaceView instanceof SurfaceView surfaceView && videoSize.width > 0 && videoSize.height > 0) {
                surfaceView.getHolder().setFixedSize(videoSize.width, videoSize.height);
            }
        }
    }

    public void startTimerRewardAd() {
        if (isFinishing() || isInPicInPicMode || isTvBox || isVisibleDialogInfo){
            return;
        }
        if (countDownTimer == null && Boolean.TRUE.equals(Callback.getRewardAdMovie())){
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    if (isFinishing() || isInPicInPicMode || isVisibleDialogInfo){
                        return;
                    }
                    boolean isPlaying = exoPlayer != null && exoPlayer.isPlaying();
                    helper.showRewardAds(Callback.getRewardAdMovie(),isPlaying, playWhenReady1 -> {
                    });
                }
            }.start();
        }
    }

    private void getPlayerData() {
        Intent intent = getIntent();
        if (playerType.equals(TAG_TYPE_MOVIE)){
            movieID = intent.getStringExtra("movie_id");
            playerTitle = intent.getStringExtra("movie_title");
            movieContainer = intent.getStringExtra("movie_container");
            movieRating = intent.getStringExtra("movie_rating");
            moviePoster = intent.getStringExtra("movie_poster");
        } else if (playerType.equals(TAG_TYPE_LOCAL) || playerType.equals(TAG_TYPE_SINGLE_URL) || playerType.equals(TAG_TYPE_DOWNLOAD)){
            playerTitle = intent.getStringExtra("video_name");
            videoUrl = intent.getStringExtra("video_url");
        }
    }

    private void setMediaSession() {
        if (mediaSession != null) {
            mediaSession.release();
        }
        if (exoPlayer.canAdvertiseSession()) {
            try {
                mediaSession = new MediaSession.Builder(this, exoPlayer).build();
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Failed to create media session", e);
            }
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_player_exoplayer;
    }

    @SuppressLint("SwitchIntDef")
    @NonNull
    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        if (playerType.equals(TAG_TYPE_DOWNLOAD)){
            return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
        } else {
            return switch (type) {
                case C.CONTENT_TYPE_SS ->
                    // For SmoothStreaming (SS)
                        new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                                buildDataSourceFactory(false)).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_DASH ->
                    // For Dynamic Adaptive Streaming over HTTP (DASH)
                        new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                                buildDataSourceFactory(false)).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_HLS ->
                    // For HTTP Live Streaming (HLS)
                        new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_RTSP ->
                    // For Real-Time Streaming Protocol (RTSP)
                        new RtspMediaSource.Factory().createMediaSource(mediaItem);
                case C.CONTENT_TYPE_OTHER ->
                    // For Progressive Media
                        new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
                default ->
                        new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
            };
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? bandwidthMeter : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        HttpDataSource.Factory httpDataSourceFactory = buildHttpDataSourceFactory(bandwidthMeter);
        return new DefaultDataSource.Factory(this, httpDataSourceFactory);
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);

        return new DefaultHttpDataSource.Factory()
                .setUserAgent(spHelper.getAgentName().isEmpty()
                        ? Util.getUserAgent(ExoPlayerActivity.this, "ExoPlayerDemo")
                        : spHelper.getAgentName())
                .setTransferListener(bandwidthMeter)
                .setAllowCrossProtocolRedirects(true)
                .setKeepPostFor302Redirects(true);
    }

    private void setLoudnessEnhancer(int audioSessionId) {
        try {
            if (audioSessionId <= 0) {
                ApplicationUtil.log(TAG, "Invalid audioSessionId: " + audioSessionId);
                return;
            }

            if (loudnessEnhancer != null) {
                loudnessEnhancer.release();
            }
            loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
            notifyAudioSessionUpdate(true);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to create loudness enhancer", e);
            loudnessEnhancer = null;  // Ensure it is null in case of failure
        }
    }

    void notifyAudioSessionUpdate(final boolean active) {
        final Intent intent = new Intent(active
                ? AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                : AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
        );
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, exoPlayer.getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        if (active) {
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE);
        }
        try {
            sendBroadcast(intent);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to send audio session update broadcast", e);
        }
    }

    private void addToRecent() {
        try {
            if (dbHelper == null){
                return;
            }
            if (playerType.equals(TAG_TYPE_MOVIE)){
                ItemMovies itemMovies = new ItemMovies(playerTitle,movieID,moviePoster,movieRating,"");
                dbHelper.addToMovie(DBHelper.TABLE_RECENT_MOVIE, itemMovies, spHelper.getMovieLimit());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to add to recent", e);
        }
    }

    public void removeSeek() {
        try {
            if (dbHelper == null){
                return;
            }
            if (playerType.equals(TAG_TYPE_MOVIE)){
                dbHelper.removeSeekID(DBHelper.TABLE_SEEK_MOVIE, movieID, playerTitle);
            } else if (playerType.equals(TAG_TYPE_EPISODES)){
                ItemEpisodes episodes = Callback.getArrayListEpisodes().get(Callback.getPlayPosEpisodes());
                dbHelper.removeSeekID(DBHelper.TABLE_SEEK_EPISODES,episodes.getId(), episodes.getTitle());
                if (spHelper.getIsAutoplayEpisode()){
                    new Handler(Looper.getMainLooper()).postDelayed(this::next, 30);
                }
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to remove seek", e);
        }
    }

    private void next() {
        if (playerType.equals(TAG_TYPE_EPISODES) && Callback.getPlayPosEpisodes() < (Callback.getArrayListEpisodes().size() - 1)) {
            Callback.setPlayPosEpisodes(Callback.getPlayPosEpisodes() + 1);
            setMediaSource();
        } else {
            skipNext.setVisibility(View.GONE);
        }
    }

    public void addToSeek() {
        try {
            if (exoPlayer == null || dbHelper == null){
                return;
            }
            if (playerType.equals(TAG_TYPE_MOVIE)){
                dbHelper.addToSeek(DBHelper.TABLE_SEEK_MOVIE,
                        String.valueOf(exoPlayer.getCurrentPosition()),
                        String.valueOf(getProgressPercentage()), movieID, playerTitle);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to remove seek", e);
        }
    }

    private int getProgressPercentage() {
        if (exoPlayer == null){
            return 0;
        }
        if (exoPlayer.getDuration() > 0) {
            return (int) ((exoPlayer.getCurrentPosition() * 100) / exoPlayer.getDuration());
        }
        return 0;
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isVisibleDialogInfo || isVisibleDialogEpisodes){
                    closeDialog();
                    return;
                }
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onStop() {
        super.onStop();
        playWhenReady(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (castContext != null && sessionManager != null) {
            sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()) {
                if (exoPlayer != null && exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                }
            } else {
                playWhenReady(false);
            }
        } else {
            playWhenReady(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        playWhenReady(true);
        if (castContext != null && sessionManager != null) {
            sessionManager.addSessionManagerListener(sessionManagerListener, CastSession.class);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        playWhenReady(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    private void playWhenReady(boolean setPlayWhenReady) {
        try {
            if (exoPlayer == null) {
                return;
            }
            exoPlayer.setPlayWhenReady(setPlayWhenReady);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to set play when ready", e);
        }
    }

    private void release() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (batteryReceiver != null){
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }

        addToSeek();

        if (exoPlayer != null) {
            notifyAudioSessionUpdate(false);
            exoPlayer.removeListener(playerListener);
            exoPlayer.release();
            exoPlayer = null;
        }

        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
    }

    // onKeyUp -----------------------------------------------------------
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (playerView  == null) {
            return super.onKeyUp(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode ==  KeyEvent.KEYCODE_VOLUME_DOWN){
            playerView.postDelayed(playerView.textClearRunnable, CustomPlayerView.MESSAGE_TIMEOUT_KEY);
        }
        return super.onKeyUp(keyCode, event);
    }

    // onKeyDown --------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (exoPlayer == null) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE,
                 KeyEvent.KEYCODE_BUTTON_SELECT -> {
                handlePlayPauseToggle(keyCode);
                return true;
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> {
                handlePlayPause();
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                adjustVolume(keyCode == KeyEvent.KEYCODE_VOLUME_UP, event.getRepeatCount() == 0);
                return true;
            }
            case KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER,
                 KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_NUMPAD_ENTER,
                 KeyEvent.KEYCODE_SPACE -> {
                if (!controllerVisibleFully) {
                    handlePlayPause();
                    return true;
                }
            }
            case KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2,
                 KeyEvent.KEYCODE_MEDIA_REWIND -> {
                if (!controllerVisibleFully) {
                    handleSeekBy(-10 * 1000L); // Seek backward by 10 seconds
                    return true;
                }
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_R2,
                 KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                if (!controllerVisibleFully) {
                    handleSeekBy(10 * 1000L); // Seek forward by 10 seconds
                    return true;
                }
            }
            case KeyEvent.KEYCODE_BACK -> {
                handleBackButton();
                return true;
            }
            case KeyEvent.KEYCODE_UNKNOWN -> {
                return super.onKeyDown(keyCode, event);
            }
            default -> {
                ApplicationUtil.log(TAG, "Unhandled key event: " + keyCode);
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleSeekBy(long positionMs) {
        try {
            if (exoPlayer == null) {
                return;
            }
            long currentPosition = exoPlayer.getCurrentPosition();
            long duration = exoPlayer.getDuration();

            if (duration == C.TIME_UNSET) {
                ApplicationUtil.log(TAG, "Cannot seek, duration is unknown");
                return;
            }

            // Calculate and constrain new position
            long newPosition = Math.max(0, Math.min(currentPosition + positionMs, duration));

            // Seek to the new position
            exoPlayer.seekTo(newPosition);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to seek", e);
        }
    }

    private void handlePlayPauseToggle(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                exoPlayer.pause();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                exoPlayer.play();
                break;
            default:
                handlePlayPause();
                break;
        }
    }

    private void handlePlayPause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            exoPlayer.play();
        }
    }

    private void handleBackButton() {
        if (isVisibleDialogInfo || isVisibleDialogEpisodes){
            closeDialog();
            return;
        }
        if (!DeviceUtils.isTvBox(this) ) {
            finish();
            return;
        }
        if (exoPlayer == null || !exoPlayer.isPlaying()) {
            finish();
            return;
        }
        if (controllerVisible){
            playerView.hideController();
            controllerVisible = false;
        } else {
            finish();
        }
    }

    private void adjustVolume(final boolean raise, boolean canBoost) {
        if (mAudioManager == null) {
            return;
        }

        if (playerView != null) {
            playerView.removeCallbacks(playerView.textClearRunnable);
        }

        final int currentVolume = ApplicationUtil.getVolume(this, false, mAudioManager);
        final int maxVolume = ApplicationUtil.getVolume(this, true, mAudioManager);
        boolean volumeActive = currentVolume != 0;

        if (currentVolume != maxVolume) boostLevel = 0;

        if (loudnessEnhancer == null){
            canBoost = false;
        }

        if (currentVolume != maxVolume || (boostLevel == 0 && !raise)) {
            mAudioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    raise ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            );
            handleVolumeStep(raise, currentVolume);
        } else {
            handleBoost(raise, canBoost, maxVolume);
        }

        if (playerView != null) {
            playerView.setIconVolume(volumeActive);
            playerView.setHighlight(boostLevel > 0);
            playerView.postDelayed(playerView.textClearRunnable, CustomPlayerView.MESSAGE_TIMEOUT_KEY);
        }
    }

    private void handleVolumeStep(boolean raise, int oldVolume) {
        final int newVolume = ApplicationUtil.getVolume(this, false, mAudioManager);
        if (raise && oldVolume == newVolume) {
            CustomPlayerView.incrementVolumeUpsInRow();
        } else {
            CustomPlayerView.setVolumeUpsInRow(0);
        }

        if (CustomPlayerView.getVolumeUpsInRow() > 4 && !ApplicationUtil.isVolumeMin(mAudioManager)) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI
            );
        } else {
            playerView.setCustomErrorMessage(newVolume != 0 ? " " + newVolume : "");
        }
    }

    private void handleBoost(boolean raise, boolean canBoost, int maxVolume) {
        if (canBoost && raise && boostLevel < 10) {
            boostLevel++;
        } else if (!raise && boostLevel > 0) {
            boostLevel--;
        }

        if (loudnessEnhancer != null) {
            try {
                loudnessEnhancer.setTargetGain(boostLevel * 200);
                loudnessEnhancer.setEnabled(boostLevel > 0);
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Failed to set loudness enhancer target gain", e);
            }
        }
        playerView.setCustomErrorMessage(" " + (maxVolume + boostLevel));
    }
}