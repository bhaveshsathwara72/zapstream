package nemosofts.streambox.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.FormatUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import nemosofts.streambox.R;
import nemosofts.streambox.adapter.AdapterClear;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.item.ItemSetting;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.AsyncTaskExecutor;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.DBHelper;
import nemosofts.streambox.utils.helper.SPHelper;

public class SettingClearDataActivity extends AppCompatActivity {

    SPHelper spHelper;
    private DBHelper dbHelper;
    private RecyclerView rv;
    private Boolean isTvBox;
    private String cacheSize;
    private AdapterClear adapter;
    private ArrayList<ItemSetting> arrayList;
    private ProgressDialog progressDialog;
    private Boolean isClearChannels = false;
    private Boolean isClearMovies = false;
    private Boolean isClearSeries = false;
    private Boolean isPlaybackSeries = false;
    private Boolean isPlaybackEpisodes = false;

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
        initializeCache();
        setAdapterToListview();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        isTvBox  = DeviceUtils.isTvBox(this);

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (Boolean.TRUE.equals(isTvBox)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);

        progressDialog = new ProgressDialog(SettingClearDataActivity.this);

        arrayList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        rv = findViewById(R.id.rv);
        GridLayoutManager grid = new GridLayoutManager(this, 2);
        grid.setSpanCount(2);
        rv.setLayoutManager(grid);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapterToListview() {
        if (!arrayList.isEmpty()){
            arrayList.clear();
        }
        arrayList.add(new ItemSetting(getResources().getString(R.string.clear_cache)+" "+cacheSize, R.drawable.ic_clean_code));

        if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_ONE_UI) || spHelper.getLoginType().equals(Callback.TAG_LOGIN_STREAM)){
            arrayList.add(new ItemSetting(getResources().getString(R.string.clear_channels), R.drawable.ic_trash));
            arrayList.add(new ItemSetting(getResources().getString(R.string.clear_movies), R.drawable.ic_trash));
            arrayList.add(new ItemSetting(getResources().getString(R.string.clear_series), R.drawable.ic_trash));

            arrayList.add(new ItemSetting(getResources().getString(R.string.clear_playback_movies), R.drawable.ic_trash));
            arrayList.add(new ItemSetting(getResources().getString(R.string.clear_playback_episodes), R.drawable.ic_trash));
        }

        if (adapter == null){
            adapter = new AdapterClear(arrayList, position -> setOnClick(arrayList.get(position).getName()));
            rv.setAdapter(adapter);
            if (Boolean.TRUE.equals(isTvBox)){
                rv.requestFocus();
            }
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void setOnClick(String name) {
        if (name == null){
            return;
        }

        if (name.equals(getResources().getString(R.string.clear_channels))){
            clearChannels();
        } else if (name.equals(getResources().getString(R.string.clear_movies))){
            if (Boolean.TRUE.equals(isClearMovies)){
                Toasty.makeText(this, getResources().getString(R.string.already_cleared), Toasty.WARNING);
                return;
            }
            isClearMovies = true;
            executeClearAction(DBHelper.TABLE_RECENT_MOVIE, getResources().getString(R.string.clear_movies));
        } else if (name.equals(getResources().getString(R.string.clear_series))){
            if (Boolean.TRUE.equals(isClearSeries)){
                Toasty.makeText(this,getResources().getString(R.string.already_cleared), Toasty.WARNING);
                return;
            }
            isClearSeries = true;
            executeClearAction(DBHelper.TABLE_RECENT_SERIES, getResources().getString(R.string.clear_series));
        } else if (name.equals(getResources().getString(R.string.clear_playback_movies))){
            if (Boolean.TRUE.equals(isPlaybackSeries)){
                Toasty.makeText(this,getResources().getString(R.string.already_cleared), Toasty.WARNING);
                return;
            }
            isPlaybackSeries = true;
            executeClearAction(DBHelper.TABLE_SEEK_MOVIE, getResources().getString(R.string.clear_playback_movies));
        } else if (name.equals(getResources().getString(R.string.clear_playback_episodes))){
            if (Boolean.TRUE.equals(isPlaybackEpisodes)){
                Toasty.makeText(this,getResources().getString(R.string.already_cleared), Toasty.WARNING);
                return;
            }
            isPlaybackEpisodes = true;
            executeClearAction(DBHelper.TABLE_SEEK_EPISODES, getResources().getString(R.string.clear_playback_episodes));
        } else {
            clearCache();
        }
    }

    private void clearChannels() {
        if (Boolean.TRUE.equals(isClearChannels)){
            Toasty.makeText(this,getResources().getString(R.string.already_cleared), Toasty.WARNING);
            return;
        }
        isClearChannels = true;
        executeClearAction(DBHelper.TABLE_RECENT_LIVE, getResources().getString(R.string.clear_channels));
    }

    private void executeClearAction(String table, String message) {
        if (table == null || table.isEmpty()){
            return;
        }
        progressDialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dbHelper.clearData(table);
            progressDialog.dismiss();
            Toasty.makeText(this,message + " Success", Toasty.SUCCESS);
        }, 500);
    }

    private void initializeCache() {
        try {
            long size = 0;
            size += getDirSize(this.getCacheDir());
            size += getDirSize(this.getExternalCacheDir());
            cacheSize = FormatUtils.formatFileSize(size);
        } catch (Exception e) {
            cacheSize ="0 MB";
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        try {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file != null && file.isDirectory()) {
                    size += getDirSize(file);
                } else if (file != null && file.isFile()) {
                    size += file.length();
                }
            }
        } catch (Exception e) {
            return size;
        }
        return size;
    }

    private void clearCache() {
        if (cacheSize.equals("0 MB")){
            Toasty.makeText(this, getResources().getString(R.string.already_cleared), Toasty.WARNING);
            return;
        }
        new AsyncTaskExecutor<String, String, String>() {
            @Override
            protected void onPreExecute() {
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String strings) {
                try {
                    FileUtils.deleteQuietly(getCacheDir());
                    FileUtils.deleteQuietly(getExternalCacheDir());
                    return "1";
                } catch (Exception e) {
                    return "0";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                cacheSize = "0 MB";
                setAdapterToListview();
                Toasty.makeText(SettingClearDataActivity.this, getResources().getString(R.string.clear_cache)+" Success", Toasty.SUCCESS);
            }
        }.execute();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_clear_data;
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            ApplicationUtil.log("SettingClearDataActivity", "Error dbHelper close",e);
        }
        super.onDestroy();
    }
}