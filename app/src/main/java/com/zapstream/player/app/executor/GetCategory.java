package com.zapstream.player.app.executor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import com.zapstream.player.app.interfaces.GetCategoryListener;
import com.zapstream.player.app.item.ItemCat;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.AsyncTaskExecutor;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.JSHelper;

public class GetCategory extends AsyncTaskExecutor<String, String, String> {

    public static final int PAGE_TYPE_LIVE = 1;
    public static final int PAGE_TYPE_MOVIE = 2;
    public static final int PAGE_TYPE_SERIES = 3;
    public static final int PAGE_TYPE_PLAYLIST_4 = 4;
    public static final int PAGE_TYPE_PLAYLIST_5 = 5;

    private final DBHelper dbHelper;
    private final JSHelper jsHelper;
    private final GetCategoryListener listener;
    private final ArrayList<ItemCat> itemCat = new ArrayList<>();
    private final int pageType;

    public GetCategory(Context ctx, int pageType, GetCategoryListener listener) {
        this.listener = listener;
        this.pageType = pageType;
        jsHelper = new JSHelper(ctx);
        dbHelper = new DBHelper(ctx);
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String s) {
        try {
            switch (pageType) {
                case PAGE_TYPE_LIVE -> itemCat.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_LIVE_CAT, dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_LIVE)));
                case PAGE_TYPE_MOVIE -> itemCat.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_MOVIE_CAT, dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_MOVIE)));
                case PAGE_TYPE_SERIES -> itemCat.addAll(jsHelper.getCategory(JSHelper.TAG_JSON_SERIES_CAT, dbHelper.getFilterIDs(DBHelper.TABLE_FILTER_SERIES)));
                case PAGE_TYPE_PLAYLIST_4, PAGE_TYPE_PLAYLIST_5 -> {
                    ArrayList<ItemCat> arrayList = new ArrayList<>(jsHelper.getCategoryPlaylist(pageType));
                    for (int i = 0; i < arrayList.size(); i++) {
                        addOrUpdateItem(itemCat, String.valueOf(i), arrayList.get(i).getName());
                    }
                }
                default -> {
                    return "0";
                }
            }
            if (!itemCat.isEmpty() && Boolean.TRUE.equals(jsHelper.getIsCategoriesOrder())) {
                Collections.reverse(itemCat);
            }
            return "1";
        } catch (Exception e) {
            ApplicationUtil.log("GetCategory", "Error fetching categories", e);
            return "0";
        }
    }

    protected void addOrUpdateItem(@NonNull ArrayList<ItemCat> itemList, String id, String title) {
        boolean idExists = false;
        for (ItemCat item : itemList) {
            if (item.getName().equals(title)) {
                idExists = true;
                break;
            }
        }
        if (!idExists) {
            itemList.add(new ItemCat(id,title,""));
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, itemCat);
    }
}