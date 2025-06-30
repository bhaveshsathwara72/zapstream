package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;

import com.zapstream.player.app.R;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.callback.Method;
import com.zapstream.player.app.dialog.LoginDialog;
import com.zapstream.player.app.executor.LoadLogin;
import com.zapstream.player.app.executor.LoadStatus;
import com.zapstream.player.app.executor.LoadTrial;
import com.zapstream.player.app.interfaces.LoginListener;
import com.zapstream.player.app.interfaces.StatusListener;
import com.zapstream.player.app.item.ItemLoginServer;
import com.zapstream.player.app.item.ItemLoginUser;
import com.zapstream.player.app.item.ItemUsersDB;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.Helper;
import com.zapstream.player.app.utils.helper.SPHelper;

public class TrialAccountActivity extends AppCompatActivity {

    private Helper helper;
    private DBHelper dbHelper;
    private SPHelper spHelper;
    private ProgressDialog progressDialog;
    private String deviceID = "N/A";
    private TextView msg;

    private LinearLayout btnLogin;
    private TextView btnText;
    private TextView msgNote;

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

        deviceID = "WIXOS"+ DeviceUtils.getDeviceID(this).toUpperCase();

        TextView device = findViewById(R.id.tv_user_name);
        device.setText(deviceID);

        msg = findViewById(R.id.tv_msg);
        btnText = findViewById(R.id.tv_login_text);
        btnLogin = findViewById(R.id.ll_btn_add);
        msgNote = findViewById(R.id.tv_msg_note);

        getData();
    }

    private void getData() {
        if (!NetworkUtils.isConnected(this)){
            Toasty.makeText(TrialAccountActivity.this, getString(R.string.err_internet_not_connected),Toasty.ERROR);
            return;
        }
        LoadStatus loadStatus = new LoadStatus(new StatusListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String reportSuccess, String message) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    switch (reportSuccess) {
                        case "1" -> {
                            setLogin();
                            msg.setTextColor(ContextCompat.getColor(TrialAccountActivity.this, R.color.color_setting_5));
                        }
                        case "2" -> {
                            setCreate();
                            msg.setTextColor(ContextCompat.getColor(TrialAccountActivity.this, R.color.color_setting_5));
                        }
                        default -> {
                            setInvalid();
                            msg.setTextColor(ContextCompat.getColor(TrialAccountActivity.this, R.color.color_setting_1));
                        }
                    }
                    msg.setText(message);
                } else {
                    Toasty.makeText(TrialAccountActivity.this, getString(R.string.err_server_not_connected),Toasty.ERROR);
                }
            }
        }, helper.getAPIRequestNSofts(Method.METHOD_GET_TRIAL, deviceID, "", "", ""));
        loadStatus.execute();
    }

    private void setInvalid() {
        btnText.setText("");
        btnLogin.setVisibility(View.GONE);
        msgNote.setVisibility(View.VISIBLE);
        msgNote.setText(spHelper.getTrialNote());
    }

    private void setCreate() {
        btnText.setText(getString(R.string.create_trial_account));
        btnLogin.setVisibility(View.VISIBLE);
        btnLogin.setOnClickListener(v -> getLoginData());
    }

    private void setLogin() {
        btnText.setText(getString(R.string.login_trial_account));
        btnLogin.setVisibility(View.VISIBLE);
        btnLogin.setOnClickListener(v -> getLoginData());
    }

    private void getLoginData() {
        if (!NetworkUtils.isConnected(this)){
            Toast.makeText(this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        LoadTrial loadTrial = new LoadTrial(new LoadTrial.Listener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String apiSuccess, String message, String type, String name, String password, String dns) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    loadLogin(type, name, password, dns);
                } else {
                    Toasty.makeText(TrialAccountActivity.this, getString(R.string.err_server_not_connected),Toasty.ERROR);
                }
            }
        }, helper.getAPIRequestNSofts(Method.METHOD_ADD_TRIAL, deviceID, "", "", ""));
        loadTrial.execute();
    }

    private void loadLogin(String type, String name, String password, String dns) {
        if (!NetworkUtils.isConnected(this)){
            Toast.makeText(this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        LoginDialog dialog = new LoginDialog(this, name);
        LoadLogin login = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onEnd(String success, ItemLoginUser itemLoginUser,
                              ItemLoginServer itemLoginServer, String allowedOutputFormats) {
                if (isFinishing()){
                    return;
                }
                if (!success.equals("1")) {
                    Toast.makeText(TrialAccountActivity.this, getString(R.string.err_login_not_incorrect), Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = dbHelper.addToUserDB(new ItemUsersDB("",
                        name, name, password,
                        dns.replace(" ",""), type)
                );
                spHelper.setUserId(userId);

                spHelper.setLoginDetails(itemLoginUser, itemLoginServer);
                spHelper.setLoginType(type.equals("xui") ? Callback.TAG_LOGIN_ONE_UI : Callback.TAG_LOGIN_STREAM);

                if (!allowedOutputFormats.isEmpty()){
                    if (allowedOutputFormats.contains("m3u8")){
                        spHelper.setLiveFormat(2);
                    } else {
                        spHelper.setLiveFormat(1);
                    }
                } else {
                    spHelper.setLiveFormat(0);
                }

                spHelper.setAnyName("Trial Account");
                spHelper.setIsFirst(false);
                spHelper.setIsLogged(true);
                spHelper.setIsAutoLogin(true);

                Toast.makeText(TrialAccountActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();
                ApplicationUtil.openThemeActivity(TrialAccountActivity.this);
            }
        },dns, ApplicationUtil.getAPIRequestLogin(name, password));
        login.execute();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        progressDialog = new ProgressDialog(TrialAccountActivity.this);

        helper = new Helper(this);
        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);

        findViewById(R.id.rl_list_users).setOnClickListener(view -> openUsersListActivity());
    }

    private void openUsersListActivity() {
        Intent intent = new Intent(TrialAccountActivity.this, UsersListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_trial_account;
    }
}