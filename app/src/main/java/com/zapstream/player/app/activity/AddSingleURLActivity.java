package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;

import com.zapstream.player.app.R;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.item.ItemSingleURL;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.AsyncTaskExecutor;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.SPHelper;

public class AddSingleURLActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SPHelper spHelper;
    private EditText etAnyName;
    private EditText etUrl;
    private ProgressDialog progressDialog;

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
        setupCallbacks();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);
        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        progressDialog = new ProgressDialog(this);
        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);

        etAnyName = findViewById(R.id.et_any_name);
        etUrl = findViewById(R.id.et_url);

        findViewById(R.id.ll_btn_add).setOnClickListener(v -> addURL());
        findViewById(R.id.rl_list_single).setOnClickListener(view -> openSingleStreamActivity());

        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.rl_list_single).setFocusableInTouchMode(false);
            etAnyName.requestFocus();
        }
    }

    private void setupCallbacks() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(AddSingleURLActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_add_single_url;
    }

    private void addURL() {
        etAnyName.setError(null);
        etUrl.setError(null);

        String anyName = etAnyName.getText().toString().trim();
        String videoUrl = etUrl.getText().toString().trim();

        if (validateInputs(anyName, videoUrl)) {
            playVideo(anyName, videoUrl);
        }
    }

    private boolean validateInputs(String anyName, String videoUrl) {
        if (TextUtils.isEmpty(anyName)) {
            etAnyName.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            etAnyName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(videoUrl)) {
            etUrl.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            etUrl.requestFocus();
            return false;
        }
        return true;
    }

    private void playVideo(String anyName, String videoUrl) {
        new AsyncTaskExecutor<String, String, String>() {

            @Override
            protected void onPreExecute() {
                setEnabled(false);
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String strings) {
                try {
                    dbHelper.addToSingleURL(new ItemSingleURL("", anyName, videoUrl));
                    return "1";
                } catch (Exception e) {
                    return "0";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (s.equals("1")){
                    navigateToSingleStream();
                } else {
                    setEnabled(true);
                    Toasty.makeText(AddSingleURLActivity.this, getString(R.string.err_file_invalid), Toasty.ERROR);
                }
            }
        }.execute();
    }

    private void navigateToSingleStream() {
        spHelper.setLoginType(Callback.TAG_LOGIN_SINGLE_STREAM);
        new Handler(Looper.getMainLooper()).postDelayed(this::openSingleStreamActivity, 500);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openSingleStreamActivity() {
        spHelper.setLoginType(Callback.TAG_LOGIN_SINGLE_STREAM);
        startActivity(new Intent(this, SingleStreamActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void setEnabled(boolean isEnabled) {
        if (!isEnabled){
            findViewById(R.id.iv_add).setVisibility(View.GONE);
            findViewById(R.id.pb_add).setVisibility(View.VISIBLE);
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()){
                return;
            }
            findViewById(R.id.iv_add).setVisibility(View.VISIBLE);
            findViewById(R.id.pb_add).setVisibility(View.GONE);
            if (DeviceUtils.isTvBox(this)){
                findViewById(R.id.ll_btn_add).requestFocus();
            }
        }, 400);
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            ApplicationUtil.log("AddSingleURLActivity", "Error closing database",e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)){
            DialogUtil.exitDialog(AddSingleURLActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}