package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;

import java.util.Objects;

import com.zapstream.player.app.R;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.SPHelper;

public class DialogActivity extends AppCompatActivity {

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

        String from = getIntent().getStringExtra("from");
        switch (Objects.requireNonNull(from)){
            case Callback.DIALOG_TYPE_UPDATE:
                DialogUtil.upgradeDialog(this, this::openMainActivity);
                break;
            case Callback.DIALOG_TYPE_MAINTENANCE:
                DialogUtil.maintenanceDialog(this);
                break;
            case Callback.DIALOG_TYPE_DEVELOPER:
                DialogUtil.dModeDialog(this);
                break;
            case Callback.DIALOG_TYPE_VPN:
                DialogUtil.vpnDialog(this);
                break;
            default:
                openMainActivity();
                break;
        }
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
    }

    private void openMainActivity() {
        SPHelper spHelper = new SPHelper(this);
        if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_SINGLE_STREAM)){
            new Handler(Looper.getMainLooper()).postDelayed(this::openSingleStream, 2000);
        } else if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_ONE_UI) || spHelper.getLoginType().equals(Callback.TAG_LOGIN_STREAM)){
            if (Boolean.TRUE.equals(spHelper.getIsFirst())) {
                new Handler(Looper.getMainLooper()).postDelayed(this::openSelectPlayer, 2000);
            } else {
                if (Boolean.FALSE.equals(spHelper.getIsAutoLogin())) {
                    new Handler(Looper.getMainLooper()).postDelayed(this::openSelectPlayer, 2000);
                } else {
                    ApplicationUtil.openThemeActivity(DialogActivity.this);
                }
            }
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::openSelectPlayer, 2000);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openSelectPlayer() {
        Intent intent = new Intent(DialogActivity.this, SelectPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openSingleStream() {
        Intent intent = new Intent(DialogActivity.this, SingleStreamActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_launcher;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}