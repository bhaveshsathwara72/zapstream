package nemosofts.streambox.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;

import java.util.ArrayList;

import nemosofts.streambox.R;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.dialog.DialogUtil;
import nemosofts.streambox.executor.LoadPlaylist;
import nemosofts.streambox.interfaces.LoadPlaylistListener;
import nemosofts.streambox.item.ItemPlaylist;
import nemosofts.streambox.item.ItemUsersDB;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.MediaPath;
import nemosofts.streambox.utils.helper.DBHelper;
import nemosofts.streambox.utils.helper.JSHelper;
import nemosofts.streambox.utils.helper.SPHelper;

public class AddPlaylistActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private DBHelper dbHelper;
    private JSHelper jsHelper;
    private EditText etAnyName;
    private EditText etUrl;
    private Boolean isFile = true;
    private String filePath = "";
    private TextView btnBrowse;
    private TextView tvBrowse;
    private ProgressDialog progressDialog;

    private static final String[] SUPPORTED_EXTENSIONS_PLAYLIST = {
            "audio/mpegurl", "audio/x-mpegurl", "application/x-mpegurl", "application/vnd.apple.mpegurl"
    };

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> pickPlaylistLauncher;

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

        initializeUI();
        initializeViews();
        setupActivityResultLaunchers();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);
        IfSupported.statusAndNavigationBars(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
    }

    private void initializeViews() {
        progressDialog = new ProgressDialog(this);
        jsHelper = new JSHelper(this);
        dbHelper = new DBHelper(this);
        spHelper = new SPHelper(this);

        etAnyName = findViewById(R.id.et_any_name);
        etUrl = findViewById(R.id.et_url);
        btnBrowse = findViewById(R.id.btn_browse);
        tvBrowse = findViewById(R.id.tv_browse);

        setupRadioGroup();
        setClickListener();
    }

    private void setupRadioGroup() {
        RadioGroup rg =  findViewById(R.id.rg);
        if (DeviceUtils.isTvBox(this)){
            etAnyName.requestFocus();
            rg.check(R.id.rd_2);
            setIsFile(false);
        } else {
            rg.check(R.id.rd_1);
            setIsFile(true);
        }
    }

    private void setupActivityResultLaunchers() {
        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
                Toast.makeText(AddPlaylistActivity.this, Boolean.TRUE.equals(isGranted)
                        ? "Permission granted"
                        : getResources().getString(R.string.err_cannot_use_features), Toast.LENGTH_SHORT).show());

        // Initialize the ActivityResultLauncher to pick a playlist
        pickPlaylistLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handlePickPlaylistResult);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(AddPlaylistActivity.this);
            }
        });
    }

    private void setClickListener() {
        findViewById(R.id.rd_1).setOnClickListener(view -> setIsFile(true));
        findViewById(R.id.rd_2).setOnClickListener(view -> setIsFile(false));
        findViewById(R.id.ll_btn_add).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.rl_list_users).setOnClickListener(view -> openUsersListActivity());

        btnBrowse.setOnClickListener(view -> {
            if (checkPermission()) {
                btnBrowse.setBackgroundResource(R.drawable.focused_btn_primary);
                pickPlaylist();
            }
        });
    }

    private void setIsFile(boolean file) {
        isFile = file;
        findViewById(R.id.ll_browse).setVisibility(file ? View.VISIBLE : View.GONE);
        findViewById(R.id.ll_url).setVisibility(file ? View.GONE : View.VISIBLE);
    }

    private void handlePickPlaylistResult(ActivityResult result) {
        if (result != null && result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri == null) {
                return;
            }

            try {
                String pathAudio = MediaPath.getPathAudio(this, uri);
                if (pathAudio != null && pathAudio.contains(".m3u")) {
                    filePath = uri.toString();
                    tvBrowse.setText(pathAudio);
                    btnBrowse.setBackgroundResource(R.drawable.focused_btn_success);
                    Toasty.makeText(this,getString(R.string.added_success), Toasty.SUCCESS);
                } else {
                    errorData();
                }
            } catch (Exception e) {
                errorData();
            }
        }
    }

    private void attemptLogin() {
        etAnyName.setError(null);
        String anyName = etAnyName.getText().toString();

        if (TextUtils.isEmpty(anyName)) {
            etAnyName.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            etAnyName.requestFocus();
            return;
        }

        if (Boolean.TRUE.equals(isFile) && filePath.isEmpty()) {
            Toasty.makeText(this, getString(R.string.err_file_invalid), Toasty.ERROR);
        } else if (Boolean.FALSE.equals(isFile) && TextUtils.isEmpty(etUrl.getText().toString())) {
            etUrl.setError(ApplicationUtil.setErrorMsg(getString(R.string.err_cannot_empty)));
            etUrl.requestFocus();
        } else if (Boolean.FALSE.equals(isFile) && !NetworkUtils.isConnected(this)) {
            Toasty.makeText(this, getString(R.string.err_internet_not_connected), Toasty.ERROR);
        } else {
            loadPlaylistData();
        }
    }

    private void loadPlaylistData() {
        String finalUrl = Boolean.TRUE.equals(isFile) ? filePath : etUrl.getText().toString().trim();
        new LoadPlaylist(this, isFile, finalUrl, new LoadPlaylistListener() {
            @Override
            public void onStart() {
                setEnabled(false);
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String msg, ArrayList<ItemPlaylist> arrayListPlaylist) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                handleLoadResult(success, msg, arrayListPlaylist);
            }
        }).execute();
    }

    private void handleLoadResult(String success, String msg, ArrayList<ItemPlaylist> arrayListPlaylist) {
        if ("1".equals(success)) {
            if (arrayListPlaylist.isEmpty()) {
                setEnabled(true);
                Toasty.makeText(this, getString(R.string.err_no_data_found), Toasty.ERROR);
            } else {
                jsHelper.addToPlaylistData(arrayListPlaylist);
                if (Boolean.FALSE.equals(isFile)){
                    String userId = dbHelper.addToUserDB(new ItemUsersDB("",
                            etAnyName.getText().toString(), etAnyName.getText().toString(),
                            etAnyName.getText().toString(), etUrl.getText().toString(),
                            "playlist"));
                    spHelper.setUserId(userId);
                }
                openPlaylistActivity();
            }
        } else {
            setEnabled(true);
            Toasty.makeText(this, msg, Toasty.ERROR);
        }
    }

    private void openPlaylistActivity() {
        spHelper.setLoginType(Callback.TAG_LOGIN_PLAYLIST);
        spHelper.setAnyName(etAnyName.getText().toString());
        Toast.makeText(this, "Login successfully.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PlaylistActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void openUsersListActivity() {
        Intent intent = new Intent(AddPlaylistActivity.this, UsersListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    private void pickPlaylist() {
        tvBrowse.setText("");
        // Create an intent with the specified MIME types
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // General audio type, can filter within the file chooser
        intent.putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_EXTENSIONS_PLAYLIST);
        // Launch the intent using ActivityResultLauncher
        pickPlaylistLauncher.launch(intent);
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
    public int setContentViewID() {
        return R.layout.activity_add_playlist;
    }

    @NonNull
    private Boolean checkPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(AddPlaylistActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);  // Request permission using the new API
            return false;
        }
        return true;
    }

    private void errorData() {
        filePath = "";
        tvBrowse.setText("");
        btnBrowse.setBackgroundResource(R.drawable.focused_btn_danger);
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                Toasty.makeText(AddPlaylistActivity.this,getString(R.string.err_file_invalid), Toasty.ERROR), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)){
            DialogUtil.exitDialog(AddPlaylistActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}