package com.zapstream.player.app.executor;

import android.content.Context;

import java.util.ArrayList;

import com.zapstream.player.app.interfaces.GetMovieListener;
import com.zapstream.player.app.item.ItemMovies;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.AsyncTaskExecutor;
import com.zapstream.player.app.utils.helper.JSHelper;

public class GetMovieSearch extends AsyncTaskExecutor<String, String, String> {

    private final JSHelper jsHelper;
    private final GetMovieListener listener;
    private final ArrayList<ItemMovies> itemMovies = new ArrayList<>();
    private final String searchText;
    private final Boolean isPlaylist;
    private static final int MAX_RESULTS = 20;

    public GetMovieSearch(Context ctx, Boolean isPlaylist, String searchText, GetMovieListener listener) {
        this.listener = listener;
        this.isPlaylist = isPlaylist;
        this.searchText = searchText;
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
            ArrayList<ItemMovies> moviesList = new ArrayList<>();
            if (Boolean.TRUE.equals(isPlaylist)) {
                moviesList.addAll(jsHelper.getMoviesPlaylist());
            } else {
                moviesList.addAll(jsHelper.getMoviesSearch(searchText));
            }

            if (moviesList.isEmpty()){
                return "0";
            }

            int limit = Math.min(MAX_RESULTS, moviesList.size());
            for (int i = 0; i < limit; i++) {
                ItemMovies movie = moviesList.get(i);
                if (movie.getName().toLowerCase().contains(searchText)) {
                    itemMovies.add(movie);
                }
            }
            return "1";
        } catch (Exception e) {
            ApplicationUtil.log("GetMovieSearch", "Error fetching movies search", e);
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s,itemMovies);
    }
}