package nemosofts.streambox.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;

import nemosofts.streambox.R;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.SPHelper;

public class SettingPlayerActivity extends AppCompatActivity {

    boolean isHardware = true;

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
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        SPHelper spHelper = new SPHelper(this);

        RadioGroup rg = findViewById(R.id.rg);
        isHardware = spHelper.isHardwareDecoding();
        if (isHardware){
            rg.check(R.id.rd_1);
        } else {
            rg.check(R.id.rd_2);
        }

        findViewById(R.id.rd_1).setOnClickListener(view -> isHardware = true);
        findViewById(R.id.rd_2).setOnClickListener(view -> isHardware = false);
        findViewById(R.id.ll_btn_save).setOnClickListener(v -> {
            spHelper.setHardwareDecoding(isHardware);
            findViewById(R.id.tv_save).setVisibility(View.GONE);
            findViewById(R.id.pb_save).setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                findViewById(R.id.tv_save).setVisibility(View.VISIBLE);
                findViewById(R.id.pb_save).setVisibility(View.GONE);
                Toasty.makeText(SettingPlayerActivity.this, "Save Data", Toasty.SUCCESS);
            }, 500);
        });
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_player;
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