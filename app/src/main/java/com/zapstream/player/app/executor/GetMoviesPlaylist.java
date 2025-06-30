package com.zapstream.player.app.executor;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;

import com.zapstream.player.app.interfaces.GetMovieListener;
import com.zapstream.player.app.item.ItemMovies;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.AsyncTaskExecutor;
import com.zapstream.player.app.utils.helper.JSHelper;

public class GetMoviesPlaylist extends AsyncTaskExecutor<String, String, String> {

    private final JSHelper jsHelper;
    private final GetMovieListener listener;
    private final ArrayList<ItemMovies> itemMovies = new ArrayList<>();
    private final String catName;
    private final int page;
    private static final int ITEMS_PER_PAGE = 10;

    public GetMoviesPlaylist(Context ctx, int page, String catName, GetMovieListener listener) {
        this.listener = listener;
        this.catName = catName;
        this.page = page;
        jsHelper = new JSHelper(ctx);
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            ArrayList<ItemMovies> allItems = new ArrayList<>(jsHelper.getMoviesPlaylist());
            if (allItems.isEmpty()){
                return "0";
            }

            // Filter the items by the specified category
            ArrayList<ItemMovies> filteredItems = new ArrayList<>();
            for (ItemMovies item : allItems) {
                if (item.getCatName().equalsIgnoreCase(catName)) {
                    filteredItems.add(item);
                }
            }

            // Reverse the order if required
            if (Boolean.TRUE.equals(jsHelper.getIsMovieOrder())) {
                Collections.reverse(filteredItems);
            }

            // Apply pagination
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());
            for (int i = startIndex; i < endIndex; i++) {
                itemMovies.add(filteredItems.get(i));
            }

            return "1";
        } catch (Exception e) {
            ApplicationUtil.log("GetMoviesPlaylist", "Error fetching movies playlist", e);
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s,itemMovies);
    }
}