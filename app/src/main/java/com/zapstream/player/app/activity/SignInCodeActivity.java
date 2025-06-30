package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;

import java.util.ArrayList;

import com.zapstream.player.app.R;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.callback.Method;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.executor.LoadLogin;
import com.zapstream.player.app.executor.LoadUsers;
import com.zapstream.player.app.interfaces.LoginListener;
import com.zapstream.player.app.interfaces.UsersListener;
import com.zapstream.player.app.item.ItemLoginServer;
import com.zapstream.player.app.item.ItemLoginUser;
import com.zapstream.player.app.item.ItemUsers;
import com.zapstream.player.app.item.ItemUsersDB;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.Helper;
import com.zapstream.player.app.utils.helper.SPHelper;

public class SignInCodeActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SPHelper spHelper;
    private Helper helper;
    private EditText etActivationCode;
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
        initializeViews();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        progressDialog = new ProgressDialog(SignInCodeActivity.this);

        helper = new Helper(this);
        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);
    }

    private void initializeViews() {
        etActivationCode  = findViewById(R.id.et_activation_code);
        findViewById(R.id.ll_btn_add).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.rl_list_users).setOnClickListener(view -> openSelectPlayerActivity());
        findViewById(R.id.rl_vpn).setOnClickListener(view -> openOVPNActivity());
        findViewById(R.id.rl_vpn).setVisibility(Boolean.TRUE.equals(spHelper.isOVEN()) ? View.VISIBLE : View.GONE);
        if (DeviceUtils.isTvBox(this)){
            etActivationCode.requestFocus();
        }
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(SignInCodeActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openSelectPlayerActivity() {
        Intent intent = new Intent(SignInCodeActivity.this, SelectPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    private void openOVPNActivity() {
        Intent intent = new Intent(SignInCodeActivity.this, OpenVPNActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    private void attemptLogin() {
        etActivationCode.setError(null);

        String code = etActivationCode.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(code)) {
            etActivationCode.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            focusView = etActivationCode;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            loadActivationCode();
        }
    }

    private void loadActivationCode() {
        if (!NetworkUtils.isConnected(this)){
            Toast.makeText(this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        LoadUsers loadUsers = new LoadUsers(new UsersListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemUsers> arrayListUsers) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    if (arrayListUsers.isEmpty()) {
                        Toasty.makeText(SignInCodeActivity.this, getString(R.string.err_activation_code_incorrect), Toasty.ERROR);
                    } else {
                        loadLogin(arrayListUsers.get(0));
                    }
                } else {
                    Toasty.makeText(SignInCodeActivity.this, getString(R.string.err_server_not_connected), Toasty.ERROR);
                }
            }
        }, helper.getAPIRequestNSofts(Method.METHOD_GET_ACTIVATION_CODE, "",
                "", "", etActivationCode.getText().toString()));
        loadUsers.execute();
    }

    private void loadLogin(ItemUsers itemUsers) {
        if (!NetworkUtils.isConnected(this)){
            Toast.makeText(this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        LoadLogin login = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, ItemLoginUser itemLoginUser,
                              ItemLoginServer itemLoginServer, String allowedOutputFormats) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (!success.equals("1")) {
                    Toast.makeText(SignInCodeActivity.this, getString(R.string.err_login_not_incorrect), Toast.LENGTH_SHORT).show();
                    return;
                }

                setLoginData(itemUsers, itemLoginUser, itemLoginServer, allowedOutputFormats);
            }
        },itemUsers.getDnsBase(), ApplicationUtil.getAPIRequestLogin(itemUsers.getUserName(),
                itemUsers.getUserPassword()));
        login.execute();
    }

    private void setLoginData(ItemUsers itemUsers, ItemLoginUser itemLoginUser,
                              ItemLoginServer itemLoginServer, String allowedOutputFormats) {
        String userId = dbHelper.addToUserDB(new ItemUsersDB("",itemUsers.getUserName(),
                itemUsers.getUserName(), itemUsers.getUserPassword(),
                itemUsers.getDnsBase().replace(" ",""),
                Boolean.TRUE.equals(itemUsers.getUserType().equals("xui")) ? "xui" : "stream")
        );
        spHelper.setUserId(userId);

        spHelper.setLoginDetails(itemLoginUser, itemLoginServer);
        if (Boolean.TRUE.equals(itemUsers.getUserType().equals("xui"))){
            spHelper.setLoginType(Callback.TAG_LOGIN_ONE_UI);
        } else {
            spHelper.setLoginType(Callback.TAG_LOGIN_STREAM);
        }

        if (!allowedOutputFormats.isEmpty()){
            if (allowedOutputFormats.contains("m3u8")){
                spHelper.setLiveFormat(2);
            } else {
                spHelper.setLiveFormat(1);
            }
        } else {
            spHelper.setLiveFormat(0);
        }

        spHelper.setAnyName(itemUsers.getUserName());
        spHelper.setIsFirst(false);
        spHelper.setIsLogged(true);
        spHelper.setIsAutoLogin(true);

        Toast.makeText(SignInCodeActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();

        ApplicationUtil.openThemeActivity(SignInCodeActivity.this);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_sign_in_code;
    }
}