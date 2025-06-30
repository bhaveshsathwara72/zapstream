package com.zapstream.player.app.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import com.zapstream.player.app.R;
import com.zapstream.player.app.adapter.AdapterCategory;
import com.zapstream.player.app.adapter.AdapterSeries;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.executor.GetCategory;
import com.zapstream.player.app.executor.GetSeries;
import com.zapstream.player.app.interfaces.GetCategoryListener;
import com.zapstream.player.app.interfaces.GetSeriesListener;
import com.zapstream.player.app.item.ItemCat;
import com.zapstream.player.app.item.ItemSeries;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.Helper;
import com.zapstream.player.app.utils.recycler.EndlessRecyclerViewScrollListener;

public class SeriesActivity extends AppCompatActivity {

    Helper helper;
    private FrameLayout frameLayout;
    private ProgressDialog progressDialog;
    // Category
    private AdapterCategory adapterCategory;
    private RecyclerView rvCat;
    private ArrayList<ItemCat> arrayListCat;
    // Series
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private Boolean isLoading = false;
    private int page = 1;
    private String catID = "0";
    private AdapterSeries adapter;
    private ArrayList<ItemSeries> arrayList;
    private RecyclerView rv;
    private ProgressBar pb;
    private int isPage = 0;
    private GetSeries loadSeries;
    private int pos = 1;

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
        initRecyclerViews();
        initListeners();

        new Handler(Looper.getMainLooper()).postDelayed(this::getDataCat, 0);
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        TextView title = findViewById(R.id.tv_page_title);
        title.setText(getString(R.string.series_home));

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        pb = findViewById(R.id.pb);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        rvCat = findViewById(R.id.rv_cat);

        progressDialog = new ProgressDialog(SeriesActivity.this);
        helper = new Helper(this, (position, type) -> openDetailsSeriesActivity(position));

        arrayList = new ArrayList<>();
        arrayListCat = new ArrayList<>();
    }

    private void openDetailsSeriesActivity(int position) {
        Intent intent = new Intent(this, DetailsSeriesActivity.class);
        intent.putExtra("series_id", arrayList.get(position).getSeriesID());
        intent.putExtra("series_name", arrayList.get(position).getName());
        intent.putExtra("series_rating", arrayList.get(position).getRating());
        intent.putExtra("series_cover", arrayList.get(position).getCover());
        startActivity(intent);
    }

    private void initRecyclerViews() {
        GridLayoutManager grid = new GridLayoutManager(this, 1);
        grid.setSpanCount(DeviceUtils.isTvBox(this) ? 6 : 5);
        rv.setLayoutManager(grid);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.TRUE.equals(isPage == 0) && (Boolean.FALSE.equals(isOver) && (Boolean.FALSE.equals(isLoading)))) {
                    isLoading = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        getData();
                    }, 0);
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvCat.setLayoutManager(manager);
        rvCat.setItemAnimator(new DefaultItemAnimator());
    }

    private void initListeners() {
        findViewById(R.id.iv_filter).setOnClickListener(v -> DialogUtil.filterDialog(this, 3, () -> recreateData(pos)));
        findViewById(R.id.iv_search).setOnClickListener(view -> {
            Intent intent = new Intent(SeriesActivity.this, SearchActivity.class);
            intent.putExtra("page", "Series");
            startActivity(intent);
        });
        findViewById(R.id.iv_cat_filter).setOnClickListener(v -> {
            Intent result = new Intent(SeriesActivity.this, FilterCategoriesActivity.class);
            result.putExtra("cat_type", "series");
            filterLauncher.launch(result);
        });
    }

    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    recreate();
                }
            }
    );

    private void getDataCat() {
        new GetCategory(this, GetCategory.PAGE_TYPE_SERIES, new GetCategoryListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, ArrayList<ItemCat> itemCat) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1") && !itemCat.isEmpty()) {
                    if (!arrayListCat.isEmpty()){
                        arrayListCat.clear();
                    }
                    arrayListCat.add(new ItemCat("01",getString(R.string.favourite),""));
                    arrayListCat.add(new ItemCat("02",getString(R.string.recently),""));
                    arrayListCat.add(new ItemCat("03",getString(R.string.recently_add),""));
                    arrayListCat.addAll(itemCat);
                    catID = itemCat.get(0).getId();
                    setAdapterToCatListview();
                } else {
                    setEmpty();
                }
            }
        }).execute();
    }

    private void getData() {
        loadSeries = new GetSeries(this, page,  catID, isPage, new GetSeriesListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()){
                    pb.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onEnd(String success, ArrayList<ItemSeries> arrayListSeries) {
                if (isFinishing()){
                    return;
                }
                pb.setVisibility(View.GONE);
                if (Boolean.FALSE.equals(isOver)){
                    if (success.equals("1")) {
                        if (arrayListSeries.isEmpty()) {
                            isOver = true;
                            setEmpty();
                        } else {
                            arrayList.addAll(arrayListSeries);
                            page = page + 1;
                            setAdapterToListview();
                        }
                    } else {
                        setEmpty();
                    }
                    isLoading = false;
                }
            }
        });
        loadSeries.execute();
    }

    public void setAdapterToCatListview() {
        adapterCategory = new AdapterCategory(this, arrayListCat, position -> {
            if (pos != position){
                recreateData(position);
            }
        });
        rvCat.setAdapter(adapterCategory);
        adapterCategory.select(3);
        pos = 3;

        handleAdultContentVerification();
        setupSearchFunctionality();
    }

    private void setupSearchFunctionality() {
        EditText edtSearch = findViewById(R.id.edt_search);
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return true;
        });
        edtSearch.addTextChangedListener(createSearchWatcher());
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @NonNull
    @Contract(" -> new")
    private TextWatcher createSearchWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // this method is empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapterCategory != null) {
                    adapterCategory.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // this method is empty
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void recreateData(int position) {
        if (position >= 0 && position < arrayListCat.size()) {
            pos = position;
            catID = arrayListCat.get(position).getId();
            adapterCategory.select(position);

            // Cancel any ongoing task
            if (loadSeries != null) {
                loadSeries.shutDown();
            }
            isOver = true;

            // Clear the list
            if (!arrayList.isEmpty()){
                arrayList.clear();
            }

            // Notify adapter of data change
            if (adapter != null){
                adapter.notifyDataSetChanged();
            }

            determinePageType(position);
            new Handler(Looper.getMainLooper()).postDelayed(this::resetPaginationAndFetchData, 0);
        }
    }

    private void determinePageType(int position) {
        switch (arrayListCat.get(position).getId()) {
            case "01":
                isPage = 1; // Favorites
                break;
            case "02":
                isPage = 2; // Recently watched
                break;
            case "03":
                isPage = 3; // Recently added
                break;
            default:
                isPage = 0; // Default category
                break;
        }
    }

    private void resetPaginationAndFetchData() {
        isOver = false;
        isScroll = false;
        isLoading = false;
        page = 1;

        handleAdultContentVerification();
    }

    private void handleAdultContentVerification() {
        if (ApplicationUtil.isAdultsCount(arrayListCat.get(pos).getName())) {
            DialogUtil.childCountDialog(this, pos, position -> getData());
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::getData, 0);
        }
    }

    public void setAdapterToListview() {
        if(Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterSeries(this, arrayList, (itemCat, position) -> helper.showInterAd(position,""));
            rv.setAdapter(adapter);
            rv.scheduleLayoutAnimation();
            setEmpty();
        } else {
            adapter.notifyItemInserted(arrayList.size()-1);
        }
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.row_empty, null);

            frameLayout.addView(myView);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_channel;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK){
                finish();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_HOME){
                ApplicationUtil.openHomeActivity(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.cancel();
        }
        super.onDestroy();
    }
}