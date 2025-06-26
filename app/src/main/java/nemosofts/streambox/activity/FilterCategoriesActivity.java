package nemosofts.streambox.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import nemosofts.streambox.R;
import nemosofts.streambox.adapter.AdapterCategoriesFilter;
import nemosofts.streambox.item.ItemCat;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.AsyncTaskExecutor;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.DBHelper;
import nemosofts.streambox.utils.helper.JSHelper;

public class FilterCategoriesActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private JSHelper jsHelper;
    private RecyclerView rv;
    private ArrayList<ItemCat> arrayList;
    private FrameLayout frameLayout;
    private ProgressDialog progressDialog;
    private String type = "live";
    String filterIDs;
    private boolean addData = false;

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

        type = getIntent().getStringExtra("cat_type");

        initializeUI();
        setupRecyclerView();
        setupBackPressHandler();

        getData();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
        findViewById(R.id.iv_back_page).setOnClickListener(view -> backPressed());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        progressDialog = new ProgressDialog(FilterCategoriesActivity.this);
        jsHelper = new JSHelper(this);
        dbHelper = new DBHelper(this);
        arrayList = new ArrayList<>();
        frameLayout = findViewById(R.id.fl_empty);
    }

    private void setupRecyclerView() {
        rv = findViewById(R.id.rv);
        GridLayoutManager grid = new GridLayoutManager(this, 3);
        grid.setSpanCount(3);
        rv.setLayoutManager(grid);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public int setContentViewID() {
        return (R.layout.activity_filter_categories);
    }

    private void getData() {
        new AsyncTaskExecutor<String, String, String>() {

            @Override
            protected void onPreExecute() {
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String strings) {
                try {
                    switch (type) {
                        case "movie" -> {
                            arrayList.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_MOVIE_CAT,""));
                            filterIDs = dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_MOVIE);
                        }
                        case "series" -> {
                            arrayList.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_SERIES_CAT,""));
                            filterIDs = dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_SERIES);
                        }
                        default -> {
                            arrayList.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_LIVE_CAT,""));
                            filterIDs = dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_LIVE);
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
        }.execute();
    }

    public void setAdapterToListview() {
        AdapterCategoriesFilter adapter = new AdapterCategoriesFilter(arrayList, filterIDs, this::addFilter);
        rv.setAdapter(adapter);
        setEmpty();
    }

    private void addFilter(boolean isChecked, String catID) {
        addData = true;
        switch (type) {
            case "movie" -> {
                if (isChecked){
                    dbHelper.addToFilter(DBHelper.TABLE_FILTER_MOVIE, catID);
                } else {
                    dbHelper.removeFilter(DBHelper.TABLE_FILTER_MOVIE, catID);
                }
            }
            case "series" -> {
                if (isChecked){
                    dbHelper.addToFilter(DBHelper.TABLE_FILTER_SERIES, catID);
                } else {
                    dbHelper.removeFilter(DBHelper.TABLE_FILTER_SERIES, catID);
                }
            }
            default -> {
                if (isChecked){
                    dbHelper.addToFilter(DBHelper.TABLE_FILTER_LIVE, catID);
                } else {
                    dbHelper.removeFilter(DBHelper.TABLE_FILTER_LIVE, catID);
                }
            }
        }
        String msg = isChecked ? getString(R.string.list_success) : getString(R.string.list_remove_success);
        Toasty.makeText(FilterCategoriesActivity.this, msg, Toasty.SUCCESS);
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            if (DeviceUtils.isTvBox(this)){
                rv.requestFocus();
            }
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

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK){
                backPressed();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_HOME){
                ApplicationUtil.openHomeActivity(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });
    }

    private void backPressed() {
        if (addData){
            Intent intent = new Intent();
            intent.putExtra("page_type", type);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.cancel();
        }
        super.onDestroy();
    }
}