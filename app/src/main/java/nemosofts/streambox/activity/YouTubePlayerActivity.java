package nemosofts.streambox.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.nemosofts.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import nemosofts.streambox.R;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.player.CustomYouTubePlayerView;

public class YouTubePlayerActivity extends AppCompatActivity {

    YouTubePlayerView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.hideStatusBar(this);

        String streamID = getIntent().getStringExtra("stream_id");

        view = findViewById(R.id.youtube_player_view);
        view.setEnableAutomaticInitialization(false);
        getLifecycle().addObserver(view);

        View customPlayerUi = view.inflateCustomPlayerUi(R.layout.custom_player_ui);

        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                CustomYouTubePlayerView customYouTubePlayerView = new CustomYouTubePlayerView(customPlayerUi, youTubePlayer, view);
                youTubePlayer.addListener(customYouTubePlayerView);
                if (streamID != null) {
                    YouTubePlayerUtils.loadOrCueVideo(youTubePlayer, getLifecycle(), streamID, 0F);
                }
            }
        };

        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();
        view.initialize(listener, options);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_youtube_player;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK){
                finish();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_HOME){
                ApplicationUtil.openHomeActivity(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}