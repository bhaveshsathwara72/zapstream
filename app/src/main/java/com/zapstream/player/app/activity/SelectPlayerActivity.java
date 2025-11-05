package com.zapstream.player.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zapstream.player.app.BuildConfig;
import com.zapstream.player.app.R;
import com.zapstream.player.app.adapter.AdapterSelect;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.item.ItemSelect;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.advertising.RewardAdAdmob;
import com.zapstream.player.app.utils.helper.Helper;
import com.zapstream.player.app.utils.helper.SPHelper;

import java.util.ArrayList;

public class SelectPlayerActivity extends AppCompatActivity {
    long trialDaysLeft;

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

        initializeUI();
        setupRecyclerView();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
        AppCompatTextView textView = findViewById(R.id.tvRegisteredDate);
        SPHelper spHelper = new SPHelper(this);
        try {
            trialDaysLeft = spHelper.getPendingDays();

            if (trialDaysLeft <= 0) {
                textView.setText("Trial Period Expired");
            } else {
                textView.setText("Trial Period " + trialDaysLeft + " Day" + (trialDaysLeft > 1 ? "s" : "") + " Left");
            }

        } catch (Exception e) {
            e.printStackTrace();
            textView.setText("Invalid Date");
        }
        findViewById(R.id.tv_terms).setOnClickListener(view -> {
            Intent intent = new Intent(SelectPlayerActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL + "terms");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(SelectPlayerActivity.this, intent, null);
        });
    }

    private void setupRecyclerView() {
        boolean isTvBox = DeviceUtils.isTvBox(this);

        SPHelper spHelper = new SPHelper(this);

        RecyclerView rv = findViewById(R.id.rv_list);
        GridLayoutManager grid = new GridLayoutManager(this, 2);
        grid.setSpanCount(2);
        rv.setLayoutManager(grid);
        rv.setItemAnimator(new DefaultItemAnimator());

        ArrayList<ItemSelect> arrayList = new ArrayList<>();
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_XUI))) {
            arrayList.add(new ItemSelect(getString(R.string.login_with_xtream_codes), R.drawable.ic_folder_connection, false));
        }
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_STREAM))) {
            arrayList.add(new ItemSelect(getString(R.string._1_stream), R.drawable.ic_mist_line, false));
        }
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_PLAYLIST))) {
            arrayList.add(new ItemSelect(getString(R.string.m3u_playlist), R.drawable.ic_play_list, false));
        }
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_DEVICE))) {
            arrayList.add(new ItemSelect(getString(R.string.login_with_device_id), R.drawable.ic_devices, false));
        }
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_ACTIVATION_CODE))) {
            arrayList.add(new ItemSelect(getString(R.string.login_with_activation_code), R.drawable.ic_unlock, false));
        }
        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_SELECT_SINGLE))) {
            arrayList.add(new ItemSelect(getString(R.string.play_single_stream), R.drawable.ic_movie, false));
        }
        arrayList.add(new ItemSelect(getString(R.string.list_users), R.drawable.ic_user_octagon, true));
        arrayList.add(new ItemSelect(getString(R.string._downloads), R.drawable.iv_downloading, true));

        if (Boolean.TRUE.equals(spHelper.getIsSelect(SPHelper.TAG_IS_LOCAL_STORAGE)) && !isTvBox) {
            arrayList.add(new ItemSelect(getString(R.string._local_storage), R.drawable.ic_hard_drive, true));
        }

        if (Boolean.TRUE.equals(Callback.getIsAdsStatus()) && !(trialDaysLeft > 0 || spHelper.isPurchased() || spHelper.hasActiveAccess())) {
            RewardAdAdmob rewardAdAdmob = new RewardAdAdmob(getApplicationContext());
            rewardAdAdmob.createAd();
        }


        AdapterSelect adapterSelect = new AdapterSelect(arrayList, (item, position) -> {
            if (trialDaysLeft > 0 || spHelper.isPurchased() || spHelper.hasActiveAccess()) {
                select(arrayList.get(position).getTitle());
            } else {
                showTrialPopup();
            }
        });
        rv.setAdapter(adapterSelect);
        if (isTvBox) {
            rv.requestFocus();
        }
    }

    private void showTrialPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SPHelper spHelper = new SPHelper(this);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_watch_ads, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnWatchAds = view.findViewById(R.id.btnWatchAds);
        Button btnPurchase = view.findViewById(R.id.btnPurchase);
        String text = "Watch " + (2 - spHelper.getAdsWatched()) + " Ads";
        btnWatchAds.setText(text);
        btnWatchAds.setOnClickListener(v -> {
            dialog.dismiss();
            new Helper(this).showRewardAdsForAccess(playWhenReady -> {
                if (playWhenReady) {
                    if (spHelper.getAdsWatched() == 1) {
                        spHelper.grantAccessFor24Hours();
                        restartApp(this);
                    } else {
                        spHelper.setAdsWatched(spHelper.getAdsWatched() + 1);
                    }
                }
            });
        });

        btnPurchase.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, SubscriptionActivity.class));
        });
    }

    public void restartApp(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Runtime.getRuntime().exit(0); // Kill the current process
        }
    }


    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(SelectPlayerActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void select(String title) {
        if (title == null) {
            return;
        }
        Intent intent;
        if (title.equals(getString(R.string.login_with_xtream_codes))) {
            intent = new Intent(SelectPlayerActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "xtream");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string._1_stream))) {
            intent = new Intent(SelectPlayerActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "stream");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string.m3u_playlist))) {
            intent = new Intent(SelectPlayerActivity.this, AddPlaylistActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string.login_with_device_id))) {
            intent = new Intent(SelectPlayerActivity.this, SignInDeviceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string.play_single_stream))) {
            intent = new Intent(SelectPlayerActivity.this, AddSingleURLActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string.list_users))) {
            intent = new Intent(SelectPlayerActivity.this, UsersListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string._downloads))) {
            intent = new Intent(SelectPlayerActivity.this, DownloadActivity.class);
            startActivity(intent);
        } else if (title.equals(getString(R.string._local_storage))) {
            new SPHelper(this).setLoginType(Callback.TAG_LOGIN_VIDEOS);
            intent = new Intent(SelectPlayerActivity.this, LocalStorageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        } else if (title.equals(getString(R.string.login_with_activation_code))) {
            intent = new Intent(SelectPlayerActivity.this, SignInCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_select_player;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                DialogUtil.exitDialog(SelectPlayerActivity.this);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_HOME) {
                ApplicationUtil.openHomeActivity(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}