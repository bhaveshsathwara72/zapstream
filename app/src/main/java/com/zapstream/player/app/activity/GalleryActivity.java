package com.zapstream.player.app.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

import com.zapstream.player.app.R;
import com.zapstream.player.app.adapter.AdapterWallpaper;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.executor.LoadWallpaper;
import com.zapstream.player.app.interfaces.WallpaperListener;
import com.zapstream.player.app.item.ItemWallpaper;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;

public class GalleryActivity extends AppCompatActivity {

    private ProgressBar pb;
    private RecyclerView rv;
    private FrameLayout frameLayout;
    private ArrayList<ItemWallpaper> arrayList;
    private String errorMsg;

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

        String tmdbID = getIntent().getStringExtra("tmdb_id");
        getData(tmdbID);
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        pb = findViewById(R.id.pb);
    }

    private void setupRecyclerView() {
        rv = findViewById(R.id.rv);
        StaggeredGridLayoutManager grid = new StaggeredGridLayoutManager(DeviceUtils.isTvBox(this) ? 6 : 5, StaggeredGridLayoutManager.VERTICAL);
        grid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setLayoutManager(grid);
    }

    private void getData(String tmdbID) {
        if (!NetworkUtils.isConnected(this)){
            errorMsg = getString(R.string.err_internet_not_connected);
            setEmpty();
            return;
        }
        new LoadWallpaper(this, tmdbID, new WallpaperListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()) {
                    frameLayout.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onEnd(String success, ArrayList<ItemWallpaper> arrayListWallpaper) {
                if (success.equals("1")) {
                    if (!arrayListWallpaper.isEmpty()) {
                        arrayList.addAll(arrayListWallpaper);
                        setAdapter();
                    } else {
                        errorMsg = getString(R.string.err_no_data_found);
                        setEmpty();
                    }
                } else {
                    errorMsg = getString(R.string.err_server_not_connected);
                    setEmpty();
                }
            }
        }).execute();
    }

    private void setAdapter() {
        AdapterWallpaper adapter = new AdapterWallpaper(arrayList, position ->
                DialogUtil.wallpaperDialog(GalleryActivity.this, arrayList.get(position).getFilePath())
        );
        rv.setAdapter(adapter);
        rv.scheduleLayoutAnimation();
        setEmpty();
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.row_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg_title);
            textView.setText(errorMsg);

            frameLayout.addView(myView);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_gallery;
    }
}