package nemosofts.streambox.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;

import nemosofts.streambox.R;
import nemosofts.streambox.dialog.DialogUtil;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.SPHelper;

public class SettingMultiScreenActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private ImageView ivScreen;

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

        Boolean isTvBox  = DeviceUtils.isTvBox(this);

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (Boolean.TRUE.equals(isTvBox)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        spHelper = new SPHelper(this);

        ivScreen = findViewById(R.id.iv_screen);

        CheckBox screen = findViewById(R.id.cbox_screen);
        screen.setChecked(spHelper.getIsScreen());

        findViewById(R.id.btn_select).setOnClickListener(v ->
                DialogUtil.screenDialog(SettingMultiScreenActivity.this, new DialogUtil.ScreenDialogListener() {
                            @Override
                            public void onSubmit(int screen) {
                                spHelper.setScreen(screen);
                                findViewById(R.id.tv_select).setVisibility(View.GONE);
                                findViewById(R.id.pb_select).setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> {
                                    findViewById(R.id.tv_select).setVisibility(View.VISIBLE);
                                    findViewById(R.id.pb_select).setVisibility(View.GONE);
                                    Toasty.makeText(SettingMultiScreenActivity.this, "Changed screen", Toasty.SUCCESS);
                                    setScreen();
                                }, 500);
                            }

                            @Override
                            public void onCancel() {
                                findViewById(R.id.tv_select).setVisibility(View.VISIBLE);
                                findViewById(R.id.pb_select).setVisibility(View.GONE);
                                setScreen();
                            }
                        }
                ));

        findViewById(R.id.ll_btn_save).setOnClickListener(v -> {
            spHelper.setIsScreen(screen.isChecked());
            findViewById(R.id.tv_save).setVisibility(View.GONE);
            findViewById(R.id.pb_save).setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                findViewById(R.id.tv_save).setVisibility(View.VISIBLE);
                findViewById(R.id.pb_save).setVisibility(View.GONE);
                Toasty.makeText(SettingMultiScreenActivity.this, "Save Data", Toasty.SUCCESS);
            }, 500);
        });

        setScreen();

        if (Boolean.TRUE.equals(isTvBox)){
            screen.requestFocus();
        }
    }

    private void setScreen() {
        int getScreenData = spHelper.getScreen();
        if (getScreenData == 1){
            ivScreen.setImageResource(R.drawable.screen_one);
        } else if (getScreenData == 2){
            ivScreen.setImageResource(R.drawable.screen_two);
        } else if (getScreenData == 3){
            ivScreen.setImageResource(R.drawable.screen_three);
        } else if (getScreenData == 4){
            ivScreen.setImageResource(R.drawable.screen_four);
        } else if (getScreenData == 5){
            ivScreen.setImageResource(R.drawable.screen_five);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_multi_screen;
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