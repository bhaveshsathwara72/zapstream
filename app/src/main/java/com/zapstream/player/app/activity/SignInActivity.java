package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.zapstream.player.app.R;
import com.zapstream.player.app.adapter.AdapterSignInDns;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.executor.LoadLogin;
import com.zapstream.player.app.interfaces.LoginListener;
import com.zapstream.player.app.item.ItemDns;
import com.zapstream.player.app.item.ItemLoginServer;
import com.zapstream.player.app.item.ItemLoginUser;
import com.zapstream.player.app.item.ItemUsersDB;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.SPHelper;

public class SignInActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SPHelper spHelper;
    private EditText etAnyName;
    private EditText etUserName;
    private EditText etLoginPassword;
    private EditText etUrl;
    private Boolean isVisibility = false;
    private LinearLayout llUrl;
    private AdapterSignInDns adapter;
    private Boolean isXui = true;
    private ProgressDialog progressDialog;
    private RecyclerView rvDns;

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
        setupRecyclerView();
        setDNSData();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        String from = getIntent().getStringExtra("from");
        if (from != null && from.equals("stream")) {
            isXui = false;
        }

        progressDialog = new ProgressDialog(SignInActivity.this);

        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);
    }

    private void initializeViews() {
        llUrl  = findViewById(R.id.ll_url);
        etAnyName = findViewById(R.id.et_any_name);
        etUserName = findViewById(R.id.et_user_name);
        etLoginPassword = findViewById(R.id.et_login_password);
        etUrl = findViewById(R.id.et_url);

        ImageView visibility = findViewById(R.id.iv_visibility);
        visibility.setImageResource(Boolean.TRUE.equals(isVisibility) ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
        visibility.setOnClickListener(v -> {
            isVisibility = !isVisibility;
            visibility.setImageResource(Boolean.TRUE.equals(isVisibility) ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
            etLoginPassword.setTransformationMethod(Boolean.TRUE.equals(isVisibility)
                    ? HideReturnsTransformationMethod.getInstance()  : PasswordTransformationMethod.getInstance());
        });

        findViewById(R.id.ll_btn_add).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.rl_list_users).setOnClickListener(view -> openUsersListActivity());
        findViewById(R.id.rl_vpn).setOnClickListener(view -> openOVPNActivity());
        findViewById(R.id.rl_vpn).setVisibility(Boolean.TRUE.equals(spHelper.isOVEN()) ? View.VISIBLE : View.GONE);

        if (Boolean.FALSE.equals(isXui) || Boolean.FALSE.equals(spHelper.getIsSelect(SPHelper.TAG_IS_TRIAL))){
            findViewById(R.id.rl_lines).setVisibility(View.GONE);
        }
        findViewById(R.id.rl_lines).setOnClickListener(view -> openTrialAccountActivity());

        if (DeviceUtils.isTvBox(this)){
            etAnyName.requestFocus();
        }
    }

    private void setupRecyclerView() {
        rvDns = findViewById(R.id.rv_dns);
        rvDns.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDns.setLayoutManager(llm);
        rvDns.setNestedScrollingEnabled(false);
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(SignInActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void openOVPNActivity() {
        Intent intent = new Intent(SignInActivity.this, OpenVPNActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openTrialAccountActivity() {
        Intent intent = new Intent(SignInActivity.this, TrialAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openUsersListActivity() {
        Intent intent = new Intent(SignInActivity.this, UsersListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setDNSData() {
        ArrayList<ItemDns> dnsList = new ArrayList<>();
        dnsList.add(new ItemDns("External", ""));

        try {
            boolean isDnsEnabled;
            String tableName;

            if (Boolean.TRUE.equals(isXui)) {
                isDnsEnabled = Boolean.TRUE.equals(spHelper.getIsXuiDNS());
                tableName = DBHelper.TABLE_DNS_XUI;
            } else {
                isDnsEnabled = Boolean.TRUE.equals(spHelper.getIssStreamDNS());
                tableName = DBHelper.TABLE_DNS_STREAM;
            }

            if (isDnsEnabled) {
                ArrayList<ItemDns> loadedDns = new ArrayList<>(dbHelper.loadDNS(tableName));
                if (!loadedDns.isEmpty()) {
                    dnsList.addAll(loadedDns);
                }
            } else {
                rvDns.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ApplicationUtil.log("SignInActivity", "Error loading DNS data", e);
        }

        setDnsAdapter(dnsList);
    }

    private void setDnsAdapter(ArrayList<ItemDns> arrayList) {
        adapter = new AdapterSignInDns(this, arrayList, (itemDns, position) -> {
            if (llUrl.getVisibility() == View.GONE && position == 0){
                etUrl.setText("");
            }
            llUrl.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            adapter.setSelectedFocus(position);
        });
        rvDns.setAdapter(adapter);
        adapter.setSelected(0);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_sign_in;
    }

    private void attemptLogin() {
        etUserName.setError(null);
        etLoginPassword.setError(null);
        etAnyName.setError(null);

        if (llUrl.getVisibility() == View.GONE) {
            String selectedBase = adapter.getSelectedBase();
            etUrl.setText(selectedBase.isEmpty() ? "https://nemosofts.com" : selectedBase);
        }

        // Store values at the time of the login attempt.
        String anyName = etAnyName.getText().toString();
        String userName = etUserName.getText().toString();
        String password = etLoginPassword.getText().toString();
        String urlData = etUrl.getText().toString().replace(" ", "");

        boolean cancel = false;
        View focusView = null;

        // Validate inputs
        if (isInputInvalid(password, userName, anyName, urlData)) {
            cancel = true;
            focusView = getFocusView();
        }

        // Check if the URL is blacklisted
        if (isUrlBlacklisted(urlData)) {
            cancel = true;
            Toasty.makeText(SignInActivity.this, "Blacklist", Toasty.ERROR);
            focusView = etUrl;
        }

        // Handle login or focus correction
        if (cancel && focusView != null) {
            focusView.requestFocus();
        } else {
            loadLogin();
        }
    }

    private boolean isInputInvalid(String password, String userName, String anyName, String urlData) {
        boolean isInvalid = false;
        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            isInvalid = true;
        } else if (password.endsWith(" ")) {
            etLoginPassword.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_pass_end_space)));
            isInvalid = true;
        }

        if (TextUtils.isEmpty(userName)) {
            etUserName.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            isInvalid = true;
        } else if (TextUtils.isEmpty(anyName)) {
            etAnyName.setText(userName);
        }

        if (TextUtils.isEmpty(urlData)) {
            etUrl.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            isInvalid = true;
        }

        return isInvalid;
    }

    private View getFocusView() {
        if (etLoginPassword.getError() != null) {
            return etLoginPassword;
        } else if (etUserName.getError() != null) {
            return etUserName;
        } else if (etUrl.getError() != null) {
            return etUrl;
        }
        return null;
    }

    private boolean isUrlBlacklisted(String url) {
        boolean isBlacklisted = false;
        if (!Callback.getArrayBlacklist().isEmpty()) {
            for (int i = 0; i < Callback.getArrayBlacklist().size(); i++) {
                if (url.toLowerCase().contains(Callback.getArrayBlacklist().get(i).getBase().toLowerCase())) {
                    isBlacklisted = true;
                    break;
                }
            }
        }
        return isBlacklisted;
    }

    private void loadLogin() {
        if (!NetworkUtils.isConnected(this)){
            Toasty.makeText(SignInActivity.this, getString(R.string.err_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoadLogin login = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                setEnabled(false);
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, ItemLoginUser itemLoginUser,
                              ItemLoginServer itemLoginServer, String allowedOutputFormats) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                handlerLoginEnd(success, itemLoginUser, itemLoginServer, allowedOutputFormats);
            }
        },etUrl.getText().toString().replace(" ",""), ApplicationUtil.getAPIRequestLogin(etUserName.getText().toString(),
                etLoginPassword.getText().toString()));
        login.execute();
    }

    private void handlerLoginEnd(String success, ItemLoginUser itemLoginUser, ItemLoginServer itemLoginServer, String allowedOutputFormats) {
        if (!success.equals("1")) {
            setEnabled(true);
            Toasty.makeText(SignInActivity.this, getString(R.string.err_login_not_incorrect), Toasty.ERROR);
            return;
        }
        String userId = dbHelper.addToUserDB(new ItemUsersDB("",etAnyName.getText().toString(),
                etUserName.getText().toString(), etLoginPassword.getText().toString(),
                etUrl.getText().toString().replace(" ",""),Boolean.TRUE.equals(isXui) ? "xui" : "stream")
        );
        spHelper.setUserId(userId);

        spHelper.setLoginDetails(itemLoginUser, itemLoginServer);
        spHelper.setLoginType(Boolean.TRUE.equals(isXui) ? Callback.TAG_LOGIN_ONE_UI : Callback.TAG_LOGIN_STREAM);

        if (!allowedOutputFormats.isEmpty()){
            if (allowedOutputFormats.contains("m3u8")){
                spHelper.setLiveFormat(2);
            } else {
                spHelper.setLiveFormat(1);
            }
        } else {
            spHelper.setLiveFormat(0);
        }

        spHelper.setAnyName(etAnyName.getText().toString());
        spHelper.setIsFirst(false);
        spHelper.setIsLogged(true);
        spHelper.setIsAutoLogin(true);

        Toast.makeText(SignInActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();
        ApplicationUtil.openThemeActivity(SignInActivity.this);
    }

    private void setEnabled(boolean isEnabled) {
        if (isEnabled){
            findViewById(R.id.iv_add).setVisibility(View.VISIBLE);
            findViewById(R.id.pb_add).setVisibility(View.GONE);
            if (DeviceUtils.isTvBox(this)){
                findViewById(R.id.ll_btn_add).requestFocus();
            }
        } else {
            findViewById(R.id.iv_add).setVisibility(View.GONE);
            findViewById(R.id.pb_add).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)){
            DialogUtil.exitDialog(SignInActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}