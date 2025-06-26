package nemosofts.streambox.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nemosofts.streambox.R;
import nemosofts.streambox.adapter.AdapterDownloadVideos;
import nemosofts.streambox.item.ItemVideoDownload;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.AsyncTaskExecutor;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.DBHelper;

public class DownloadActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private RecyclerView rv;
    private ArrayList<ItemVideoDownload> arrayList;
    private FrameLayout frameLayout;
    private ProgressDialog progressDialog;
    private ProgressBar pb;
    private TextView usedData;
    private TextView totalData;
    private final Handler handlerSeries = new Handler(Looper.getMainLooper());
    private int progressStatusOld = 0;

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

        if(Boolean.TRUE.equals(checkPer())) {
            new LoadDownloadVideo().execute();
        }
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        progressDialog = new ProgressDialog(DownloadActivity.this);
        dbHelper = new DBHelper(this);
        arrayList = new ArrayList<>();

        pb = findViewById(R.id.pb_data);
        pb.setMax(100);
        usedData = findViewById(R.id.tv_used_data);
        totalData = findViewById(R.id.tv_total_data);
    }

    private void setupRecyclerView() {
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        GridLayoutManager grid = new GridLayoutManager(this, 6);
        grid.setSpanCount(6);
        rv.setLayoutManager(grid);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (Boolean.TRUE.equals(isGranted)){
            new LoadDownloadVideo().execute();
            Toast.makeText(DownloadActivity.this, "Permission granted" , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DownloadActivity.this, getResources().getString(R.string.err_cannot_use_features), Toast.LENGTH_SHORT).show();
        }
    });

    class LoadDownloadVideo extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            try {
                List<ItemVideoDownload> tempArray = dbHelper.loadDataDownload(DBHelper.TABLE_DOWNLOAD_MOVIES);
                File fileRoot = new File(Objects.requireNonNull(getExternalFilesDir("")).getAbsolutePath() + File.separator + "temp");
                File[] files = fileRoot.listFiles();
                if (files != null) {
                    for (File file : files) {
                        for (int j = 0; j < tempArray.size(); j++) {
                            if (new File(file.getAbsolutePath()).getName().contains(tempArray.get(j).getTempName())) {
                                ItemVideoDownload itemVideo = tempArray.get(j);
                                itemVideo.setVideoURL(file.getAbsolutePath());
                                arrayList.add(itemVideo);
                                break;
                            }
                        }
                    }
                }
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
            if (!arrayList.isEmpty()){
                setAdapterToListview();
            } else {
                setEmpty();
            }
        }
    }

    public void setAdapterToListview() {
        AdapterDownloadVideos adapter = new AdapterDownloadVideos(this, arrayList, new AdapterDownloadVideos.RecyclerItemClickListener() {

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClickListener(ItemVideoDownload itemVideo, int position) {
                Intent intent = new Intent(DownloadActivity.this, ExoPlayerActivity.class);
                intent.putExtra("player_type", ExoPlayerActivity.TAG_TYPE_DOWNLOAD);
                intent.putExtra("video_name",  arrayList.get(position).getName());
                intent.putExtra("video_url", arrayList.get(position).getVideoURL());
                startActivity(intent);
            }

            @Override
            public void onDelete() {
                updateStorageInfo(true);
            }

        });
        rv.setAdapter(adapter);
        if (DeviceUtils.isTvBox(this)){
            rv.requestFocus();
        }
        setEmpty();
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateStorageInfo(Boolean isDelete) {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long availableBytes = stat.getAvailableBytes();
            long totalBytes = stat.getTotalBytes();

            int progress = (int) ((availableBytes * 100) / totalBytes);

            if (Boolean.FALSE.equals(isDelete)){
                progressStatusOld = 0;
                handlerSeries.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressStatusOld < 100 - progress) {
                            progressStatusOld++;
                            pb.setProgress(progressStatusOld);
                            handlerSeries.postDelayed(this, 10);
                        }
                    }
                }, 10);
            } else {
                pb.setProgress(100 - progress);
            }

            double availableGB = availableBytes / (1024.0 * 1024.0 * 1024.0);
            double totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0);

            usedData.setText(String.format("%.2f GB", availableGB) + " Available Storage");
            totalData.setText(String.format("%.2f GB", totalGB) + " total . Internal Storage");

            findViewById(R.id.ll_storage).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            findViewById(R.id.ll_storage).setVisibility(View.GONE);
        }
    }

    private void setEmpty() {
        updateStorageInfo(false);
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.row_empty, null);

            myView.findViewById(R.id.tv_empty_msg_sub).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @NonNull
    private Boolean checkPer() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_VIDEO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(DownloadActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);  // Request permission using the new API
            return false;
        }
        return true;
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_download;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.cancel();
        }
        try {
            dbHelper.close();
        } catch (Exception e) {
            ApplicationUtil.log("DownloadActivity", "Error db close",e);
        }
        super.onDestroy();
    }
}