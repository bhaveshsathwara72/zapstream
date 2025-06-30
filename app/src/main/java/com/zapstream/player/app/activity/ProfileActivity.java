package com.zapstream.player.app.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.FormatUtils;

import com.zapstream.player.app.R;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.SPHelper;

public class ProfileActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private TextView profileName;
    private TextView activeConnections;
    private TextView cardExpiry;
    private TextView anyName;
    private TextView note;
    private TextView active;
    private TextView activeNone;

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

        spHelper = new SPHelper(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        profileName =  findViewById(R.id.tv_profile_name);
        activeConnections =  findViewById(R.id.tv_active_connections);
        cardExpiry =  findViewById(R.id.tv_card_expiry);
        anyName =  findViewById(R.id.card_any_name);
        note = findViewById(R.id.tv_profile_note);
        active =  findViewById(R.id.tv_active);
        activeNone =  findViewById(R.id.tv_active_none);

        setInfo();
    }

    private void setInfo() {
        profileName.setText(spHelper.getUserName());

        String connections = spHelper.getActiveConnections() +" / "+ spHelper.getMaxConnections();
        activeConnections.setText(connections);

        String expiry = FormatUtils.convertIntToDate(spHelper.getExpDate(), "MMMM dd, yyyy");
        cardExpiry.setText(expiry);

        anyName.setText(spHelper.getAnyName());

        note.setText(spHelper.getCardMessage());

        if (spHelper.getIsStatus().equals("Active")){
            active.setVisibility(View.VISIBLE);
            activeNone.setVisibility(View.GONE);
        } else {
            active.setVisibility(View.GONE);
            activeNone.setVisibility(View.VISIBLE);
            activeNone.setText(spHelper.getIsStatus());
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_profile;
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