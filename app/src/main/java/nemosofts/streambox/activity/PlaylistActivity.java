package nemosofts.streambox.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ShimmerEffects;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nemosofts.streambox.R;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.dialog.DialogUtil;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.advertising.AdManagerInterAdmob;
import nemosofts.streambox.utils.advertising.GDPRChecker;
import nemosofts.streambox.utils.advertising.RewardAdAdmob;
import nemosofts.streambox.utils.helper.JSHelper;
import nemosofts.streambox.utils.helper.SPHelper;

public class PlaylistActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private ShimmerEffects shimmerLive;
    private ShimmerEffects shimmerMovie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.theme_bg), (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemInsets.left, systemInsets.top, systemInsets.right, systemInsets.bottom);
            return insets;
        });
        IfSupported.statusAndNavigationBars(this);

        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(PlaylistActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        spHelper = new SPHelper(this);

        getInfo();
        setListenerHome();

        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.select_live).requestFocus();
        }

        shimmerLive = findViewById(R.id.shimmer_view_live);
        shimmerMovie = findViewById(R.id.shimmer_view_movie);
        changeIcon();

        setAds();

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        OneSignal.getNotifications().requestPermission(false, Continue.none());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()){
                return;
            }
            DialogUtil.popupAdsDialog(PlaylistActivity.this);
        }, 600);
    }

    private void setAds() {
        if (DeviceUtils.isTvBox(this)){
            return;
        }
        new GDPRChecker(PlaylistActivity.this).check();
        if (Boolean.TRUE.equals(Callback.getRewardAdMovie() || Callback.getRewardAdEpisodes()
                || Callback.getRewardAdLive() || Callback.getRewardAdSingle() || Callback.getRewardAdLocal())) {
            RewardAdAdmob rewardAdAdmob = new RewardAdAdmob(getApplicationContext());
            rewardAdAdmob.createAd();
        }
        if (Boolean.TRUE.equals(Callback.getIsInterAd())) {
            AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
            adManagerInterAdmob.createAd();
        }
    }

    private void changeIcon() {
        if (Boolean.FALSE.equals(spHelper.getIsShimmeringHome())){
            shimmerLive.setVisibility(View.GONE);
            shimmerMovie.setVisibility(View.GONE);
        } else {
            shimmerLive.setVisibility(View.VISIBLE);
            shimmerMovie.setVisibility(View.VISIBLE);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setListenerHome() {
        findViewById(R.id.iv_notifications).setOnClickListener(v -> startActivity(new Intent(PlaylistActivity.this, NotificationsActivity.class)));
        findViewById(R.id.iv_file_download).setOnClickListener(v -> startActivity(new Intent(PlaylistActivity.this, DownloadActivity.class)));
        findViewById(R.id.iv_profile_re).setOnClickListener(v -> signOut());
        findViewById(R.id.iv_settings).setOnClickListener(v -> startActivity(new Intent(PlaylistActivity.this, SettingActivity.class)));
        findViewById(R.id.select_multiple_screen).setOnClickListener(v -> startActivity(new Intent(PlaylistActivity.this, MultipleScreenActivity.class)));
        findViewById(R.id.select_live).setOnClickListener(v -> {
            Intent intent = new Intent(PlaylistActivity.this, LiveTvActivity.class);
            intent.putExtra("pageType", LiveTvActivity.TAG_TYPE_PLAYLIST);
            startActivity(intent);
        });
        findViewById(R.id.select_movie).setOnClickListener(v -> {
            Intent intent = new Intent(PlaylistActivity.this, MovieActivity.class);
            intent.putExtra("pageType", MovieActivity.TAG_TYPE_PLAYLIST);
            startActivity(intent);
        });
    }

    private void getInfo() {
        ImageView imageView = findViewById(R.id.iv_wifi);
        if (!NetworkUtils.isConnected(this)) {
            imageView.setImageResource(R.drawable.ic_wifi_off);
        }
        if (NetworkUtils.isConnectedMobile(this)) {
            imageView.setImageResource(R.drawable.selector_none);
        } else if (NetworkUtils.isConnectedWifi(this)) {
            imageView.setImageResource(R.drawable.ic_wifi);
        } else if (NetworkUtils.isConnectedEthernet(this)) {
            imageView.setImageResource(R.drawable.ic_ethernet);
        }

        TextView appDate = findViewById(R.id.iv_app_date);
        try {
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
            appDate.setText(df.format(Calendar.getInstance().getTime()));
        } catch (Exception e) {
            ApplicationUtil.log("setFormattedDate", "Date formatting error", e);
        }

        TextView tvUserName = findViewById(R.id.tv_user_name);
        try {
            String userName = getString(R.string.user_list_user_name)+" "+ spHelper.getAnyName();
            tvUserName.setText(userName);
        } catch (Exception e) {
            ApplicationUtil.log("setFormattedDate", "Date formatting error", e);
        }
    }

    private void signOut() {
        DialogUtil.logoutDialog(PlaylistActivity.this, () -> {
            spHelper.setLoginType(Callback.TAG_LOGIN);
            Intent intent = new Intent(PlaylistActivity.this, UsersListActivity.class);
            new JSHelper(this).removeAllPlaylist();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("from", "");
            Toast.makeText(PlaylistActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        });
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_ui_playlist_one_ui;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)) {
            DialogUtil.exitDialog(PlaylistActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        if (Boolean.TRUE.equals(Callback.getIsDataUpdate())) {
            Callback.setIsDataUpdate(false);
            changeIcon();
        }
        if (Boolean.TRUE.equals(Callback.getIsRecreate())) {
            Callback.setIsRecreate(false);
            recreate();
        }
        super.onResume();
    }
}