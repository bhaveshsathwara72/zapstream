package com.zapstream.player.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.zapstream.player.app.R;
import com.zapstream.player.app.adapter.AdapterUsers;
import com.zapstream.player.app.callback.Callback;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.dialog.LoginDialog;
import com.zapstream.player.app.executor.LoadLogin;
import com.zapstream.player.app.executor.LoadPlaylist;
import com.zapstream.player.app.interfaces.LoadPlaylistListener;
import com.zapstream.player.app.interfaces.LoginListener;
import com.zapstream.player.app.item.ItemLoginServer;
import com.zapstream.player.app.item.ItemLoginUser;
import com.zapstream.player.app.item.ItemPlaylist;
import com.zapstream.player.app.item.ItemUsersDB;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.AsyncTaskExecutor;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.JSHelper;
import com.zapstream.player.app.utils.helper.SPHelper;

public class UsersListActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private DBHelper dbHelper;
    private RecyclerView rv;
    private ArrayList<ItemUsersDB> arrayList;

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
        getUserData();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);
        arrayList = new ArrayList<>();

        findViewById(R.id.ll_user_add).setOnClickListener(v -> openSelectPlayerActivity());
    }

    private void setupRecyclerView() {
        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtil.exitDialog(UsersListActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openSelectPlayerActivity() {
        Intent intent = new Intent(UsersListActivity.this, SelectPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_users_list;
    }

    private void getUserData() {
        new AsyncTaskExecutor<String, String, String>() {

            @Override
            protected String doInBackground(String strings) {
                try {
                    arrayList.addAll(dbHelper.loadUsersDB());
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
                if (arrayList != null && !arrayList.isEmpty()){
                    setAdapter();
                } else {
                    setEmpty();
                }
            }
        }.execute();
    }

    public void setAdapter() {
        AdapterUsers adapter = new AdapterUsers(this, arrayList, position -> {
            if (position >= 0 && position < arrayList.size()) {
                if (arrayList.get(position).getUserType().equals("xui") || arrayList.get(position).getUserType().equals("stream")) {
                    loadLogin(arrayList.get(position));
                } else if (arrayList.get(position).getUserType().equals("playlist")) {
                    loadLoginPlaylist(arrayList.get(position).getAnyName(), arrayList.get(position).getUserURL());
                }
            } else {
                Toasty.makeText(UsersListActivity.this, "Position out of bounds: " + position, Toasty.ERROR);
            }
        });
        rv.setAdapter(adapter);
        setEmpty();
    }

    private void loadLoginPlaylist(String anyName, String userURL) {
        if (!NetworkUtils.isConnected(this)){
            Toasty.makeText(UsersListActivity.this, getString(R.string.err_internet_not_connected), Toasty.ERROR);
            return;
        }

        LoginDialog dialog = new LoginDialog(this, anyName);
        LoadPlaylist playlist = new LoadPlaylist(this,false, userURL, new LoadPlaylistListener() {
            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onEnd(String success, String msg, ArrayList<ItemPlaylist> arrayListPlaylist) {
                if (isFinishing()){
                    return;
                }

                if (success.equals("1")) {
                    if (arrayListPlaylist.isEmpty()){
                        dialog.dismiss();
                        Toast.makeText(UsersListActivity.this, getString(R.string.err_no_data_found), Toast.LENGTH_SHORT).show();
                    } else {

                        new JSHelper(UsersListActivity.this).addToPlaylistData(arrayListPlaylist);

                        Toast.makeText(UsersListActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();

                        spHelper.setLoginType(Callback.TAG_LOGIN_PLAYLIST);
                        spHelper.setAnyName(anyName);
                        Intent intent = new Intent(UsersListActivity.this, PlaylistActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }  else {
                    dialog.dismiss();
                    Toasty.makeText(UsersListActivity.this, msg, Toasty.ERROR);
                }
            }
        });
        playlist.execute();
    }

    private void loadLogin(ItemUsersDB itemUsersDB) {
        if (!NetworkUtils.isConnected(this)){
            Toasty.makeText(UsersListActivity.this, getString(R.string.err_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoginDialog dialog = new LoginDialog(this, itemUsersDB.getAnyName());
        LoadLogin login = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onEnd(String success, ItemLoginUser itemLoginUser,
                              ItemLoginServer itemLoginServer , String allowedOutputFormats) {
                if (isFinishing()){
                    return;
                }

                if (!success.equals("1")) {
                    dialog.dismiss();
                    Toasty.makeText(UsersListActivity.this, getString(R.string.err_server_not_connected), Toasty.ERROR);
                    return;
                }

                spHelper.setLoginDetails(itemLoginUser, itemLoginServer);
                if (Boolean.TRUE.equals(itemUsersDB.getUserType().equals("xui"))){
                    spHelper.setLoginType(Callback.TAG_LOGIN_ONE_UI);
                } else {
                    spHelper.setLoginType(Callback.TAG_LOGIN_STREAM);
                }
                spHelper.setUserId(itemUsersDB.getId());

                if (!allowedOutputFormats.isEmpty()){
                    if (allowedOutputFormats.contains("m3u8")){
                        spHelper.setLiveFormat(2);
                    } else {
                        spHelper.setLiveFormat(1);
                    }
                } else {
                    spHelper.setLiveFormat(0);
                }

                spHelper.setAnyName(itemUsersDB.getAnyName());
                spHelper.setIsFirst(false);
                spHelper.setIsLogged(true);
                spHelper.setIsAutoLogin(true);

                Toast.makeText(UsersListActivity.this, "Login successfully.", Toast.LENGTH_SHORT).show();
                ApplicationUtil.openThemeActivity(UsersListActivity.this);
            }
        },itemUsersDB.getUserURL(), ApplicationUtil.getAPIRequestLogin(itemUsersDB.getUseName(),itemUsersDB.getUserPass()));
        login.execute();
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            if (DeviceUtils.isTvBox(this)){
                rv.requestFocus();
            }
        } else {
            rv.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)) {
            DialogUtil.exitDialog(UsersListActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}