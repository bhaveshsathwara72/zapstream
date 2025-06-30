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
import androidx.annotation.OptIn;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
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
import com.zapstream.player.app.adapter.AdapterMovie;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.executor.GetCategory;
import com.zapstream.player.app.executor.GetMovies;
import com.zapstream.player.app.executor.GetMoviesPlaylist;
import com.zapstream.player.app.interfaces.GetCategoryListener;
import com.zapstream.player.app.interfaces.GetMovieListener;
import com.zapstream.player.app.item.ItemCat;
import com.zapstream.player.app.item.ItemMovies;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.IfSupported;
import com.zapstream.player.app.utils.helper.Helper;
import com.zapstream.player.app.utils.recycler.EndlessRecyclerViewScrollListener;

public class MovieActivity extends AppCompatActivity {

    public static final String TAG_TYPE_PLAYLIST = "playlist";
    public static final String TAG_TYPE_ONLINE = "online";
    private String pageType = TAG_TYPE_ONLINE;

    Helper helper;
    private ProgressDialog progressDialog;
    // Category
    private AdapterCategory adapterCategory;
    private RecyclerView rvCat;
    private ArrayList<ItemCat> arrayListCat;
    // Movies
    private FrameLayout frameLayout;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private Boolean isLoading = false;
    private int page = 1;
    private String catID = "0";
    private AdapterMovie adapter;
    private ArrayList<ItemMovies> arrayList;
    private RecyclerView rv;
    private ProgressBar pb;
    private int isPage = 0;
    private GetMovies loadMovies;
    private GetMoviesPlaylist loadMoviesPlaylist;
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

        pageType = getIntent().getStringExtra("pageType");

        initializeUI();
        setupRecyclerView();
        setupListeners();

        new Handler(Looper.getMainLooper()).postDelayed(this::getDataCat, 0);
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        TextView title = findViewById(R.id.tv_page_title);
        title.setText(getString(R.string.movies_home));

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }

        pb = findViewById(R.id.pb);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        rvCat = findViewById(R.id.rv_cat);

        progressDialog = new ProgressDialog(MovieActivity.this);
        helper = new Helper(this, (position, type) -> openDetailsMovieActivity(position));
        arrayList = new ArrayList<>();
        arrayListCat = new ArrayList<>();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openDetailsMovieActivity(int position) {
        Intent intent;
        if (pageType.equals(TAG_TYPE_PLAYLIST)){
            intent = new Intent(MovieActivity.this, ExoPlayerActivity.class);
            intent.putExtra("player_type", ExoPlayerActivity.TAG_TYPE_SINGLE_URL);
            intent.putExtra("video_name", arrayList.get(position).getName());
            intent.putExtra("video_url", arrayList.get(position).getStreamID());
        } else {
            intent = new Intent(MovieActivity.this, DetailsMovieActivity.class);
            intent.putExtra("stream_id", arrayList.get(position).getStreamID());
            intent.putExtra("stream_name", arrayList.get(position).getName());
            intent.putExtra("stream_icon", arrayList.get(position).getStreamIcon());
            intent.putExtra("stream_rating", arrayList.get(position).getRating());
        }
        startActivity(intent);
    }

    private void setupRecyclerView() {
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

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvCat.setLayoutManager(llm);
        rvCat.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupListeners() {
        findViewById(R.id.iv_filter).setOnClickListener(v -> DialogUtil.filterDialog(this, 2, () -> recreateData(pos)));
        findViewById(R.id.iv_search).setOnClickListener(view -> {
            Intent intent = new Intent(MovieActivity.this, SearchActivity.class);
            if (pageType.equals(TAG_TYPE_PLAYLIST)){
                intent.putExtra("page", "MoviePlaylist");
            } else {
                intent.putExtra("page", "Movie");
            }
            startActivity(intent);
        });

        if (pageType.equals(TAG_TYPE_PLAYLIST)){
            findViewById(R.id.iv_cat_filter).setVisibility(View.GONE);
        } else {
            findViewById(R.id.iv_cat_filter).setOnClickListener(v -> {
                Intent result = new Intent(MovieActivity.this, FilterCategoriesActivity.class);
                result.putExtra("cat_type", "movie");
                filterLauncher.launch(result);
            });
        }
    }

    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    recreate();
                }
            }
    );

    private void getDataCat() {
        new GetCategory(this, pageType.equals(TAG_TYPE_PLAYLIST)
                ? GetCategory.PAGE_TYPE_PLAYLIST_5
                : GetCategory.PAGE_TYPE_MOVIE, new GetCategoryListener() {
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
                    if (pageType.equals(TAG_TYPE_PLAYLIST)){
                        catID = itemCat.get(0).getName();
                    } else {
                        catID = itemCat.get(0).getId();
                        arrayListCat.add(new ItemCat("01",getString(R.string.favourite),""));
                        arrayListCat.add(new ItemCat("02",getString(R.string.recently),""));
                        arrayListCat.add(new ItemCat("03",getString(R.string.recently_add),""));
                    }
                    arrayListCat.addAll(itemCat);
                    setAdapterToCatListview();
                } else {
                    setEmpty();
                }
            }
        }).execute();
    }

    private void getData() {
        if (pageType.equals(TAG_TYPE_PLAYLIST)){
            getDataPlaylist();
            return;
        }
        loadMovies = new GetMovies(this, page, catID, isPage, new GetMovieListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()){
                    pb.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onEnd(String success, ArrayList<ItemMovies> arrayListMovies) {
                if (isFinishing()){
                    return;
                }
                pb.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(isOver)){
                    return;
                }
                if (success.equals("1")) {
                    if (arrayListMovies.isEmpty()) {
                        isOver = true;
                        setEmpty();
                    } else {
                        page = page + 1;
                        arrayList.addAll(arrayListMovies);
                        setAdapterToListview();
                    }
                } else {
                    setEmpty();
                }
                isLoading = false;
            }
        });
        loadMovies.execute();
    }

    private void getDataPlaylist() {
        loadMoviesPlaylist = new GetMoviesPlaylist(this, page, catID, new GetMovieListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()){
                    pb.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onEnd(String success, ArrayList<ItemMovies> arrayListMovies) {
                if (isFinishing()){
                    return;
                }
                pb.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(isOver)){
                    return;
                }
                if (success.equals("1")) {
                    if (arrayListMovies.isEmpty()) {
                        isOver = true;
                        setEmpty();
                    } else {
                        page = page + 1;
                        arrayList.addAll(arrayListMovies);
                        setAdapterToListview();
                    }
                } else {
                    setEmpty();
                }
                isLoading = false;
            }
        });
        loadMoviesPlaylist.execute();
    }

    public void setAdapterToCatListview() {
        adapterCategory = new AdapterCategory(this, arrayListCat, position -> {
            if (pos != position){
                recreateData(position);
            }
        });
        rvCat.setAdapter(adapterCategory);
        adapterCategory.select(pageType.equals(TAG_TYPE_PLAYLIST) ? 0 : 3);
        pos = pageType.equals(TAG_TYPE_PLAYLIST) ? 0 : 3;

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
            if (pageType.equals(TAG_TYPE_PLAYLIST)){
                catID = arrayListCat.get(position).getName();
            } else {
                catID = arrayListCat.get(position).getId();
            }
            adapterCategory.select(position);

            // Cancel any ongoing task
            if (loadMoviesPlaylist != null) {
                loadMoviesPlaylist.shutDown();
            }
            if (loadMovies != null) {
                loadMovies.shutDown();
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

            if (!pageType.equals(TAG_TYPE_PLAYLIST)){
                determinePageType(position);
            }

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
            adapter = new AdapterMovie(this, arrayList, (itemCat, position) -> helper.showInterAd(position,""));
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