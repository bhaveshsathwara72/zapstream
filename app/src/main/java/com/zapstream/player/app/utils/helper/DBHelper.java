package com.zapstream.player.app.utils.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.nemosofts.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zapstream.player.app.item.ItemChannel;
import com.zapstream.player.app.item.ItemDns;
import com.zapstream.player.app.item.ItemMovies;
import com.zapstream.player.app.item.ItemSeries;
import com.zapstream.player.app.item.ItemSingleURL;
import com.zapstream.player.app.item.ItemUsersDB;
import com.zapstream.player.app.item.ItemVideoDownload;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.encrypter.EncryptData;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = BuildConfig.APPLICATION_ID + "_" + "db_streambox.db";
    private final SQLiteDatabase db;
    private final EncryptData encryptData;
    final Context context;
    private final SPHelper spHelper;

    private static final String TAG_ID = "id";
    private static final String TAG_USER_ID = "user_id";

    private static final String TAG_AND = " AND ";
    private static final String TAG_AND_Q = "=? AND ";

    // Table ---------------------------------------------------------------------------------------
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SINGLE = "single";
    public static final String TABLE_DNS_XUI = "tbl_dns_xui";
    public static final String TABLE_DNS_STREAM = "tbl_dns_stream";
    public static final String TABLE_SUBSCRIPTION = "tbl_subscription";
    public static final String TABLE_FAV_LIVE = "fav_live";
    public static final String TABLE_FAV_MOVIE = "fav_movie";
    public static final String TABLE_FAV_SERIES = "fav_series";
    public static final String TABLE_RECENT_LIVE = "recent_live";
    public static final String TABLE_RECENT_MOVIE = "recent_movie";
    public static final String TABLE_RECENT_SERIES = "recent_series";
    public static final String TABLE_DOWNLOAD_MOVIES = "download_movie";
    public static final String TABLE_SEEK_MOVIE = "movie_seek";
    public static final String TABLE_SEEK_EPISODES = "epi_seek";
    public static final String TABLE_FILTER_LIVE = "filter_live";
    public static final String TABLE_FILTER_MOVIE = "filter_movie";
    public static final String TABLE_FILTER_SERIES = "filter_series";

    // TAG -----------------------------------------------------------------------------------------
    private static final String TAG_DNS_TITLE = "dns_title";
    private static final String TAG_DNS_BASE = "dns_base";


    private static final String TAG_SINGLE_ANY_NAME = "any_name";
    private static final String TAG_SINGLE_URL = "single_url";

    private static final String TAG_USERS_ANY_NAME = "any_name";
    private static final String TAG_USERS_NAME = "user_name";
    private static final String TAG_USERS_PASSWORD = "user_pass";
    private static final String TAG_USERS_URL = "user_url";
    private static final String TAG_USERS_TYPE = "user_type";

    private static final String TAG_MOVIE_STREAM_ID = "stream_id";
    private static final String TAG_MOVIE_TITLE = "title";
    private static final String TAG_MOVIE_SEEK = "seek";
    private static final String TAG_MOVIE_SEEK_FULL = "seek_full";

    // FAV AND RECENT ------------------------------------------------------------------------------
    private static final String TAG_LIVE_NAME = "name";
    private static final String TAG_LIVE_ID = "stream_id";
    private static final String TAG_LIVE_ICON = "stream_icon";

    private static final String TAG_MOVIE_NAME = "name";
    private static final String TAG_MOVIE_ID = "stream_id";
    private static final String TAG_MOVIE_ICON = "stream_icon";
    private static final String TAG_MOVIE_RATING = "rating";

    private static final String TAG_SERIES_NAME = "name";
    private static final String TAG_SERIES_ID = "series_id";
    private static final String TAG_SERIES_COVER = "cover";
    private static final String TAG_SERIES_RATING = "rating";
    // DOWNLOAD ------------------------------------------------------------------------------------
    private static final String TAG_DOWNLOAD_NAME = "name";
    private static final String TAG_DOWNLOAD_ID = "stream_id";
    private static final String TAG_DOWNLOAD_ICON = "stream_icon";
    private static final String TAG_DOWNLOAD_URL = "video_url";
    private static final String TAG_DOWNLOAD_CONTAINER = "container";
    private static final String TAG_DOWNLOAD_TEMP_NAME = "temp_name";

    // FILTER --------------------------------------------------------------------------------------
    private static final String TAG_FILTER_ID = "filter_id";

    // Columns -------------------------------------------------------------------------------------
    private final String[] columnsFilter = new String[]{
            TAG_ID, TAG_USER_ID, TAG_FILTER_ID
    };
    private final String[] columnsLive = new String[]{
            TAG_ID, TAG_USER_ID, TAG_LIVE_NAME, TAG_LIVE_ID, TAG_LIVE_ICON
    };
    private final String[] columnsMovie = new String[]{
            TAG_ID, TAG_USER_ID, TAG_MOVIE_NAME, TAG_MOVIE_ID, TAG_MOVIE_ICON, TAG_MOVIE_RATING
    };
    private final String[] columnsSeries = new String[]{
            TAG_ID, TAG_USER_ID, TAG_SERIES_NAME, TAG_SERIES_ID, TAG_SERIES_COVER, TAG_SERIES_RATING
    };
    private final String[] columnsSingle = new String[]{
            TAG_ID, TAG_SINGLE_ANY_NAME, TAG_SINGLE_URL
    };
    private final String[] columnsSeek = new String[]{
            TAG_ID, TAG_USER_ID, TAG_MOVIE_STREAM_ID, TAG_MOVIE_TITLE, TAG_MOVIE_SEEK, TAG_MOVIE_SEEK_FULL
    };
    private final String[] columnsDns = new String[]{
            TAG_ID, TAG_DNS_TITLE, TAG_DNS_BASE
    };
    private final String[] columnsDownload = new String[]{
            TAG_ID, TAG_USER_ID, TAG_DOWNLOAD_NAME, TAG_DOWNLOAD_ID, TAG_DOWNLOAD_ICON,
            TAG_DOWNLOAD_URL, TAG_DOWNLOAD_CONTAINER, TAG_DOWNLOAD_TEMP_NAME
    };
    private final String[] columnsUsers = new String[]{
            TAG_USER_ID, TAG_USERS_ANY_NAME, TAG_USERS_NAME,
            TAG_USERS_PASSWORD, TAG_USERS_URL, TAG_USERS_TYPE
    };

    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String TEXT_ONE = " TEXT,";
    private static final String TEXT_END = " TEXT";

    // Creating Table Query FILTER ---------------------------------------------------------------
    private static final String CREATE_TABLE_FILTER_MOVIES = CREATE_TABLE + TABLE_FILTER_MOVIE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_FILTER_ID + TEXT_END
            + ")";
    private static final String CREATE_TABLE_FILTER_SERIES = CREATE_TABLE + TABLE_FILTER_SERIES + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_FILTER_ID + TEXT_END
            + ")";
    private static final String CREATE_TABLE_FILTER_LIVE = CREATE_TABLE + TABLE_FILTER_LIVE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_FILTER_ID + TEXT_END
            + ")";

    // Creating Table Query DOWNLOAD ---------------------------------------------------------------
    private static final String CREATE_TABLE_DOWNLOAD_MOVIES = CREATE_TABLE + TABLE_DOWNLOAD_MOVIES + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_DOWNLOAD_NAME + TEXT_ONE
            + TAG_DOWNLOAD_ID + TEXT_ONE
            + TAG_DOWNLOAD_ICON + TEXT_ONE
            + TAG_DOWNLOAD_URL + TEXT_ONE
            + TAG_DOWNLOAD_CONTAINER + TEXT_ONE
            + TAG_DOWNLOAD_TEMP_NAME + TEXT_END
            + ")";

    // Creating Table Query FAV --------------------------------------------------------------------
    private static final String CREATE_TABLE_FAV_SERIES = CREATE_TABLE + TABLE_FAV_SERIES + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_SERIES_NAME + TEXT_ONE
            + TAG_SERIES_ID + TEXT_ONE
            + TAG_SERIES_COVER + TEXT_ONE
            + TAG_SERIES_RATING + TEXT_END
            + ")";
    private static final String CREATE_TABLE_FAV_MOVIE = CREATE_TABLE + TABLE_FAV_MOVIE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_MOVIE_NAME + TEXT_ONE
            + TAG_MOVIE_ID + TEXT_ONE
            + TAG_MOVIE_ICON + TEXT_ONE
            + TAG_MOVIE_RATING + TEXT_END
            + ")";
    private static final String CREATE_TABLE_FAV_LIVE = CREATE_TABLE + TABLE_FAV_LIVE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_LIVE_NAME + TEXT_ONE
            + TAG_LIVE_ID + TEXT_ONE
            + TAG_LIVE_ICON + TEXT_END
            + ")";

    // Creating Table Query RECENT -----------------------------------------------------------------
    private static final String CREATE_TABLE_RECENT_SERIES = CREATE_TABLE + TABLE_RECENT_SERIES + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_SERIES_NAME + TEXT_ONE
            + TAG_SERIES_ID + TEXT_ONE
            + TAG_SERIES_COVER + TEXT_ONE
            + TAG_SERIES_RATING + TEXT_END
            + ")";
    private static final String CREATE_TABLE_RECENT_MOVIE = CREATE_TABLE + TABLE_RECENT_MOVIE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_MOVIE_NAME + TEXT_ONE
            + TAG_MOVIE_ID + TEXT_ONE
            + TAG_MOVIE_ICON + TEXT_ONE
            + TAG_MOVIE_RATING + TEXT_END
            + ")";
    private static final String CREATE_TABLE_RECENT_LIVE = CREATE_TABLE + TABLE_RECENT_LIVE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_LIVE_NAME + TEXT_ONE
            + TAG_LIVE_ID + TEXT_ONE
            + TAG_LIVE_ICON + TEXT_END
            + ")";

    // Creating Table Query ------------------------------------------------------------------------
    private static final String CREATE_TABLE_DNS_XUI = CREATE_TABLE + TABLE_DNS_XUI + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_DNS_TITLE + TEXT_ONE
            + TAG_DNS_BASE + TEXT_END
            + ")";
    private static final String CREATE_TABLE_DNS_STREAM = CREATE_TABLE + TABLE_DNS_STREAM + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_DNS_TITLE + TEXT_ONE
            + TAG_DNS_BASE + TEXT_END
            + ")";

    // Creating table query ------------------------------------------------------------------------
    private static final String CREATE_TABLE_SINGLE = CREATE_TABLE + TABLE_SINGLE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_SINGLE_ANY_NAME + TEXT_ONE
            + TAG_SINGLE_URL + TEXT_END
            + ")";

    // Creating table query ------------------------------------------------------------------------
    private static final String CREATE_TABLE_USERS = CREATE_TABLE + TABLE_USERS + " ("
            + TAG_USER_ID + AUTOINCREMENT
            + TAG_USERS_ANY_NAME + TEXT_ONE
            + TAG_USERS_NAME + TEXT_ONE
            + TAG_USERS_PASSWORD + TEXT_ONE
            + TAG_USERS_URL + TEXT_ONE
            + TAG_USERS_TYPE + TEXT_END
            + ")";

    // Creating table query ------------------------------------------------------------------------
    private static final String CREATE_TABLE_MOVIE_SEEK = CREATE_TABLE + TABLE_SEEK_MOVIE + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_MOVIE_STREAM_ID + TEXT_ONE
            + TAG_MOVIE_TITLE + TEXT_ONE
            + TAG_MOVIE_SEEK + TEXT_ONE
            + TAG_MOVIE_SEEK_FULL + TEXT_END
            + ")";

    // Creating table query ------------------------------------------------------------------------
    private static final String CREATE_TABLE_EPISODES_SEEK = CREATE_TABLE + TABLE_SEEK_EPISODES + " ("
            + TAG_ID + AUTOINCREMENT
            + TAG_USER_ID + TEXT_ONE
            + TAG_MOVIE_STREAM_ID + TEXT_ONE
            + TAG_MOVIE_TITLE + TEXT_ONE
            + TAG_MOVIE_SEEK + TEXT_ONE
            + TAG_MOVIE_SEEK_FULL + TEXT_END
            + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryptData = new EncryptData(context);
        this.context = context;
        spHelper = new SPHelper(context);
        db = getWritableDatabase();
    }

    // Create --------------------------------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_SINGLE);
            db.execSQL(CREATE_TABLE_FAV_LIVE);
            db.execSQL(CREATE_TABLE_RECENT_LIVE);
            db.execSQL(CREATE_TABLE_MOVIE_SEEK);
            db.execSQL(CREATE_TABLE_FAV_MOVIE);
            db.execSQL(CREATE_TABLE_RECENT_MOVIE);
            db.execSQL(CREATE_TABLE_FAV_SERIES);
            db.execSQL(CREATE_TABLE_RECENT_SERIES);
            db.execSQL(CREATE_TABLE_EPISODES_SEEK);
            db.execSQL(CREATE_TABLE_DNS_XUI);
            db.execSQL(CREATE_TABLE_DNS_STREAM);
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_DOWNLOAD_MOVIES);

            db.execSQL(CREATE_TABLE_FILTER_LIVE);
            db.execSQL(CREATE_TABLE_FILTER_MOVIES);
            db.execSQL(CREATE_TABLE_FILTER_SERIES);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error creating table", e);
        }
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        final String dropTable = "DROP TABLE IF EXISTS ";
        db.execSQL(dropTable + TABLE_DOWNLOAD_MOVIES);
        db.execSQL(dropTable + TABLE_FAV_SERIES);
        db.execSQL(dropTable + TABLE_FAV_MOVIE);
        db.execSQL(dropTable + TABLE_FAV_LIVE);
        db.execSQL(dropTable + TABLE_RECENT_SERIES);
        db.execSQL(dropTable + TABLE_RECENT_MOVIE);
        db.execSQL(dropTable + TABLE_RECENT_LIVE);
        db.execSQL(dropTable + TABLE_DNS_XUI);
        db.execSQL(dropTable + TABLE_DNS_STREAM);
        db.execSQL(dropTable + TABLE_SINGLE);
        db.execSQL(dropTable + TABLE_USERS);
        db.execSQL(dropTable + TABLE_SEEK_MOVIE);
        db.execSQL(dropTable + TABLE_SEEK_EPISODES);
        db.execSQL(dropTable + TABLE_FILTER_LIVE);
        db.execSQL(dropTable + TABLE_FILTER_MOVIE);
        db.execSQL(dropTable + TABLE_FILTER_SERIES);
        onCreate(db);
    }
    // FILTER --------------------------------------------------------------------------------------
    public void addToFilter(String table, String id) {
        if (table == null || id == null){
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_FILTER_ID, id);
        contentValues.put(TAG_USER_ID, spHelper.getUserId());
        db.insert(table, null, contentValues);
    }

    @NonNull
    private Boolean checkFilter(String table, String id) {
        if (table == null || id == null){
            return false;
        }
        try (Cursor cursor = db.query(table, columnsFilter, TAG_FILTER_ID + "=?" + TAG_AND
                + TAG_USER_ID
                + "=" + spHelper.getUserId(), new String[]{id}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public void removeFilter(String table, String id) {
        if (table == null || id == null){
            return;
        }
        try {
            if (Boolean.TRUE.equals(checkFilter(table, id))){
                db.delete(table, TAG_FILTER_ID + "=" + id + TAG_AND + TAG_USER_ID
                        + "=" + spHelper.getUserId(), null
                );
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove live", e);
        }
    }

    @SuppressLint("Range")
    public String getFilterIDs(String table) {
        StringBuilder filterIDs = new StringBuilder();
        if (table == null){
            return filterIDs.toString();
        }
        try (Cursor cursor = db.query(table, columnsFilter, TAG_USER_ID + "=" + spHelper.getUserId(),
                null, null, null, TAG_ID + " DESC", null)) {
            if (cursor.moveToFirst()) {
                do {
                    if (filterIDs.length() > 0) {
                        filterIDs.append(",");
                    }
                    filterIDs.append(cursor.getString(cursor.getColumnIndex(TAG_FILTER_ID)));
                } while (cursor.moveToNext());
            }
        }
        return filterIDs.toString();
    }

    // FAV AND RECENT ------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemChannel> getLive(String table, boolean isOrder) {
        List<ItemChannel> arrayList = new ArrayList<>();
        String orderClause = isOrder ? " ASC" : "";
        try (Cursor cursor = db.query(table, columnsLive, TAG_USER_ID + "=" + spHelper.getUserId(),
                null, null, null, TAG_ID + orderClause)) {
            if (cursor.moveToFirst()) {
                do {
                    String streamIcon = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_LIVE_ICON)));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_LIVE_NAME));
                    String streamID = cursor.getString(cursor.getColumnIndex(TAG_LIVE_ID));

                    ItemChannel objItem = new ItemChannel(name, streamID, streamIcon, "");
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading live", e);
        }
        return arrayList;
    }

    // Live TV -------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public void addToLive(String table, ItemChannel itemChannel, int limit) {
        if (table == null){
            return;
        }
        try {
            if (TABLE_RECENT_LIVE.equals(table)) {
                // Delete excess records if needed
                try (Cursor cursorDelete = db.query(TABLE_RECENT_LIVE, columnsLive,
                        TAG_USER_ID + "=" + spHelper.getUserId(), null,
                        null, null, null)) {
                    if (cursorDelete.getCount() > limit) {
                        cursorDelete.moveToFirst();
                        String deleteId = cursorDelete.getString(cursorDelete.getColumnIndex(TAG_ID));
                        db.delete(TABLE_RECENT_LIVE, TAG_ID + "=?", new String[]{deleteId});
                    }
                }

                // Remove existing entry if the stream ID is already present
                if (Boolean.TRUE.equals(checkLive(TABLE_RECENT_LIVE, itemChannel.getStreamID()))) {
                    db.delete(TABLE_RECENT_LIVE, TAG_USER_ID + "=" + spHelper.getUserId()
                            + TAG_AND + TAG_LIVE_ID + "=?", new String[]{itemChannel.getStreamID()}
                    );
                }
            }

            // Prepare ContentValues and insert the new item
            String image = encryptData.encrypt(itemChannel.getStreamIcon().replace(" ", "%20"));
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USER_ID, spHelper.getUserId());
            contentValues.put(TAG_LIVE_NAME, itemChannel.getName());
            contentValues.put(TAG_LIVE_ID, itemChannel.getStreamID());
            contentValues.put(TAG_LIVE_ICON, image);
            db.insert(table, null, contentValues);

        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to live", e);
        }
    }

    public void removeLive(String table, String streamID) {
        if (table == null || streamID == null){
            return;
        }
        try {
            if (Boolean.TRUE.equals(checkLive(table, streamID))){
                db.delete(table, TAG_LIVE_ID + "=" + streamID + TAG_AND + TAG_USER_ID
                        + "=" + spHelper.getUserId(), null
                );
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove live", e);
        }
    }

    public boolean checkLive(String table, String streamID) {
        if (table == null || streamID == null){
            return false;
        }
        String where = TAG_USER_ID + "=" + spHelper.getUserId() + TAG_AND + TAG_LIVE_ID + "=?";
        String[] args = {streamID};
        try (Cursor cursor = db.query(table, columnsLive, where, args, null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    // Movies --------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemMovies> getMovies(String table, boolean isOrder) {
        List<ItemMovies> arrayList = new ArrayList<>();
        if (table == null){
            return arrayList;
        }
        String orderClause = isOrder ? " ASC" : "";
        try (Cursor cursor = db.query(table, columnsMovie, TAG_USER_ID + "=" + spHelper.getUserId(),
                null, null, null, TAG_ID + orderClause)) {
            if (cursor.moveToFirst()) {
                do {
                    String streamIcon = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_MOVIE_ICON)));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_MOVIE_NAME));
                    String streamID = cursor.getString(cursor.getColumnIndex(TAG_MOVIE_ID));
                    String rating = cursor.getString(cursor.getColumnIndex(TAG_MOVIE_RATING));

                    ItemMovies objItem = new ItemMovies(name, streamID, streamIcon, rating, "");
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading movies", e);
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public void addToMovie(String table, ItemMovies itemMovies, int limit) {
        if (itemMovies == null || table == null){
            return;
        }

        try {
            if (TABLE_RECENT_MOVIE.equals(table)) {
                // Delete excess records if needed
                try (Cursor cursorDelete = db.query(TABLE_RECENT_MOVIE, columnsMovie,
                        TAG_USER_ID + "=" + spHelper.getUserId(), null,
                        null, null, null)) {
                    if (cursorDelete.getCount() > limit) {
                        cursorDelete.moveToFirst();
                        String deleteId = cursorDelete.getString(cursorDelete.getColumnIndex(TAG_ID));
                        db.delete(TABLE_RECENT_MOVIE, TAG_ID + "=?", new String[]{deleteId});
                    }
                }

                // Remove existing entry if the movie is already present
                if (Boolean.TRUE.equals(checkMovie(TABLE_RECENT_MOVIE, itemMovies.getStreamID()))) {
                    db.delete(TABLE_RECENT_MOVIE, TAG_USER_ID + "=" + spHelper.getUserId()
                            + TAG_AND + TAG_MOVIE_ID + "=?", new String[]{itemMovies.getStreamID()}
                    );
                }
            }

            // Prepare ContentValues and insert the new movie item
            String image = encryptData.encrypt(itemMovies.getStreamIcon().replace(" ", "%20"));
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USER_ID, spHelper.getUserId());
            contentValues.put(TAG_MOVIE_NAME, itemMovies.getName());
            contentValues.put(TAG_MOVIE_ID, itemMovies.getStreamID());
            contentValues.put(TAG_MOVIE_ICON, image);
            contentValues.put(TAG_MOVIE_RATING, itemMovies.getRating());

            db.insert(table, null, contentValues);

        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to movie", e);
        }
    }

    public void removeMovie(String table, String streamID) {
        if (table == null || streamID == null){
            return;
        }
        try {
            if (Boolean.TRUE.equals(checkMovie(table, streamID))){
                db.delete(table, TAG_USER_ID + "=" + spHelper.getUserId()
                        + TAG_AND + TAG_MOVIE_ID + "=" + streamID, null
                );
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove movie", e);
        }
    }

    public boolean checkMovie(String table, String streamID) {
        if (table == null || streamID == null){
            return false;
        }
        try (Cursor cursor = db.query(table, columnsMovie, TAG_USER_ID + "=" + spHelper.getUserId()
                + TAG_AND + TAG_MOVIE_ID + "=?", new String[]{streamID}, null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Series --------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemSeries> getSeries(String table, boolean isOrder) {
        List<ItemSeries> arrayList = new ArrayList<>();
        if (table == null) {
            return arrayList;
        }
        String orderClause = isOrder ? " ASC" : "";
        try (Cursor cursor = db.query(table, columnsSeries, TAG_USER_ID + "=" + spHelper.getUserId(),
                null, null, null, TAG_ID + orderClause)) {
            if (cursor.moveToFirst()) {
                do {
                    String cover = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_SERIES_COVER)));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_SERIES_NAME));
                    String seriesID = cursor.getString(cursor.getColumnIndex(TAG_SERIES_ID));
                    String rating = cursor.getString(cursor.getColumnIndex(TAG_SERIES_RATING));

                    ItemSeries objItem = new ItemSeries(name, seriesID, cover, rating);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading series", e);
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public void addToSeries(String table, ItemSeries itemSeries, int limit) {
        if (itemSeries == null || table == null){
            return;
        }

        try {
            if (TABLE_RECENT_SERIES.equals(table)) {
                // Delete excess records if needed
                try (Cursor cursorDelete = db.query(TABLE_RECENT_SERIES, columnsSeries,
                        TAG_USER_ID + "=" + spHelper.getUserId(), null,
                        null, null, null)) {
                    if (cursorDelete.getCount() > limit) {
                        cursorDelete.moveToFirst();
                        String deleteId = cursorDelete.getString(cursorDelete.getColumnIndex(TAG_ID));
                        db.delete(TABLE_RECENT_SERIES, TAG_ID + "=?", new String[]{deleteId});
                    }
                }

                // Remove existing entry if the series is already present
                if (Boolean.TRUE.equals(checkSeries(TABLE_RECENT_SERIES, itemSeries.getSeriesID()))) {
                    db.delete(TABLE_RECENT_SERIES,
                            TAG_SERIES_ID + "=?", new String[]{itemSeries.getSeriesID()}
                    );
                }
            }

            // Prepare ContentValues and insert the new series item
            String cover = encryptData.encrypt(itemSeries.getCover().replace(" ", "%20"));
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USER_ID, spHelper.getUserId());
            contentValues.put(TAG_SERIES_NAME, itemSeries.getName());
            contentValues.put(TAG_SERIES_ID, itemSeries.getSeriesID());
            contentValues.put(TAG_SERIES_COVER, cover);
            contentValues.put(TAG_SERIES_RATING, itemSeries.getRating());

            db.insert(table, null, contentValues);

        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to series", e);
        }
    }

    public void removeFavSeries(String table, String seriesID) {
        if (table == null || seriesID == null){
            return;
        }
        try {
            if (Boolean.TRUE.equals(checkSeries(table,seriesID))){
                db.delete(table, TAG_USER_ID + "=" + spHelper.getUserId()
                        + TAG_AND + TAG_SERIES_ID + "=" + seriesID, null
                );
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove series", e);
        }
    }

    public boolean checkSeries(String table, String seriesID) {
        if (table == null || seriesID == null){
            return false;
        }
        try (Cursor cursor = db.query(table, columnsSeries, TAG_USER_ID + "=" + spHelper.getUserId()
                + TAG_AND + TAG_SERIES_ID + "=?", new String[]{seriesID}, null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // DNS -----------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemDns> loadDNS(String table) {
        List<ItemDns> arrayList = new ArrayList<>();
        if (table == null){
            return arrayList;
        }
        try (Cursor cursor = db.query(table, columnsDns, null, null,
                null, null, TAG_ID + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    String name = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_DNS_TITLE)));
                    String url = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_DNS_BASE)));
                    ItemDns objItem = new ItemDns(name, url);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading Dns", e);
        }
        return arrayList;
    }

    public void addToDNS(String table, ItemDns itemDns) {
        if (itemDns == null) {
            return;  // Early return if itemDns is null, simplifying the logic
        }

        try {
            String name = encryptData.encrypt(itemDns.getTitle());
            String url = encryptData.encrypt(itemDns.getBase().replace(" ", "%20"));

            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_DNS_TITLE, name);
            contentValues.put(TAG_DNS_BASE, url);

            db.insert(table, null, contentValues);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to DNS", e);
        }
    }

    public void removeAllDNS(String table) {
        if (table == null){
            return;
        }
        try {
            db.delete(table, null, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove Dns", e);
        }
    }

    // Single --------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemSingleURL> loadSingleURL() {
        List<ItemSingleURL> arrayList = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_SINGLE, columnsSingle, null, null,
                null, null, TAG_ID + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_ID));
                    String anyName = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_SINGLE_ANY_NAME)));
                    String url = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_SINGLE_URL)));

                    arrayList.add(new ItemSingleURL(id, anyName, url));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading Single URLs", e);
        }
        return arrayList;
    }

    public void addToSingleURL(ItemSingleURL itemSingle) {
        if (itemSingle == null){
            return;
        }

        try {
            String anyName = encryptData.encrypt(itemSingle.getAnyName());
            String url = encryptData.encrypt(itemSingle.getSingleURL().replace(" ", "%20"));

            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_SINGLE_ANY_NAME, anyName);
            contentValues.put(TAG_SINGLE_URL, url);

            db.insert(TABLE_SINGLE, null, contentValues);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to Single URL", e);
        }
    }

    public void removeFromSingleURL(String singleID) {
        if (singleID == null){
            return;
        }
        try {
            db.delete(TABLE_SINGLE, TAG_ID + "=" + singleID, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove Single URL", e);
        }
    }

    // Users ---------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemUsersDB> loadUsersDB() {
        List<ItemUsersDB> arrayList = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_USERS, columnsUsers, null, null,
                null, null, TAG_USER_ID + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_USER_ID));
                    String anyName = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_USERS_ANY_NAME)));
                    String userName = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_USERS_NAME)));
                    String userPass = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_USERS_PASSWORD)));
                    String userUrl = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_USERS_URL)));
                    String userType = cursor.getString(cursor.getColumnIndex(TAG_USERS_TYPE));

                    ItemUsersDB objItem = new ItemUsersDB(id, anyName, userName, userPass, userUrl, userType);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading users db", e);
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public String addToUserDB(ItemUsersDB itemUsersDB) {
        if (itemUsersDB == null) {
            return "0";  // Return 0 if the item is null
        }
        try {
            String anyName = encryptData.encrypt(itemUsersDB.getAnyName());
            String userName = encryptData.encrypt(itemUsersDB.getUseName());
            String userPass = encryptData.encrypt(itemUsersDB.getUserPass());
            String userUrl = encryptData.encrypt(itemUsersDB.getUserURL().replace(" ", "%20"));

            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USERS_ANY_NAME, anyName);
            contentValues.put(TAG_USERS_NAME, userName);
            contentValues.put(TAG_USERS_PASSWORD, userPass);
            contentValues.put(TAG_USERS_URL, userUrl);
            contentValues.put(TAG_USERS_TYPE, itemUsersDB.getUserType());

            // Insert the data into the database
            db.insert(TABLE_USERS, null, contentValues);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to user db", e);
        }

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{TAG_USER_ID}, null,
                null, null, null, TAG_USER_ID + " DESC", "1")) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(TAG_USER_ID));
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error retrieving last inserted ID", e);
        }
        return "0"; // Return the row ID or 0 if an error occurred
    }

    public void removeFromUser(String userID) {
        if (userID == null){
            return;
        }
        try {
            db.delete(TABLE_RECENT_LIVE, TAG_USER_ID + "=" + userID, null);
            db.delete(TABLE_FAV_LIVE, TAG_USER_ID + "=" + userID, null);

            db.delete(TABLE_RECENT_MOVIE, TAG_USER_ID + "=" + userID, null);
            db.delete(TABLE_FAV_MOVIE, TAG_USER_ID + "=" + userID, null);

            db.delete(TABLE_RECENT_SERIES, TAG_USER_ID + "=" + userID, null);
            db.delete(TABLE_FAV_SERIES, TAG_USER_ID + "=" + userID, null);

            db.delete(TABLE_SEEK_EPISODES, TAG_USER_ID + "=" + userID, null);

            db.delete(TABLE_SEEK_MOVIE, TAG_USER_ID + "=" + userID, null);

            db.delete(TABLE_USERS, TAG_USER_ID + "=" + userID, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove user and remove all data for this user", e);
        }
    }

    // Seek Movie ----------------------------------------------------------------------------------
    public int getSeek(String table, String streamID, String streamName) {
        if (table == null || streamID == null || streamName == null){
            return 0;
        }
        String seekTo = "0";

        String whereClause = TAG_USER_ID + TAG_AND_Q + TAG_MOVIE_STREAM_ID + TAG_AND_Q + TAG_MOVIE_TITLE + "=?";
        String[] args = {
                spHelper.getUserId(),
                streamID,
                streamName.replace("'", "%27")
        };

        try (Cursor cursor = db.query(table, columnsSeek, whereClause, args, null, null, null)) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(TAG_MOVIE_SEEK);
                if (columnIndex != -1) {
                    String seekValue = cursor.getString(columnIndex);
                    if (seekValue != null && !seekValue.isEmpty()) {
                        seekTo = seekValue;
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }

        try {
            return Integer.parseInt(seekTo);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getSeekFull(String table, String streamID, String streamName) {
        if (table == null || streamID == null || streamName == null){
            return 0;
        }
        String seekTo = "0";

        String whereClause = TAG_USER_ID + TAG_AND_Q + TAG_MOVIE_STREAM_ID + TAG_AND_Q + TAG_MOVIE_TITLE + "=?";
        String[] args = {
                spHelper.getUserId(),
                streamID,
                streamName.replace("'", "%27")
        };
        try (Cursor cursor = db.query(table, columnsSeek, whereClause, args, null, null, null)) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(TAG_MOVIE_SEEK_FULL);
                if (columnIndex != -1) {
                    String seekValue = cursor.getString(columnIndex);
                    if (seekValue != null && !seekValue.isEmpty()) {
                        seekTo = seekValue;
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }

        try {
            return Integer.parseInt(seekTo);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void addToSeek(String table, String currentPosition, String positionFull,
                          String streamID, String streamName) {
        if (table == null || streamID == null || streamName == null){
            return;
        }
        String whereClause = TAG_USER_ID + TAG_AND_Q + TAG_MOVIE_STREAM_ID + TAG_AND_Q + TAG_MOVIE_TITLE + "=?";
        String[] args = {
                spHelper.getUserId(),
                streamID,
                streamName.replace("'", "%27")
        };

        try {
            // Check if the seek position already exists for the stream and delete it if it does
            if (Boolean.TRUE.equals(checkSeek(table, streamID, streamName))) {
                db.delete(table, whereClause, args);
            }

            // Prepare content values for the new seek entry
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USER_ID, spHelper.getUserId());
            contentValues.put(TAG_MOVIE_STREAM_ID, streamID);
            contentValues.put(TAG_MOVIE_TITLE, streamName);
            contentValues.put(TAG_MOVIE_SEEK, currentPosition);
            contentValues.put(TAG_MOVIE_SEEK_FULL, positionFull);

            // Insert the new entry into the table
            db.insert(table, null, contentValues);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to seek", e);
        }
    }

    public boolean checkSeek(String table, String streamID, String streamName) {
        if (table == null || streamID == null || streamName == null){
            return false;
        }

        String whereClause = TAG_USER_ID + TAG_AND_Q + TAG_MOVIE_STREAM_ID + TAG_AND_Q + TAG_MOVIE_TITLE + "=?";
        String[] args = {
                spHelper.getUserId(),
                streamID,
                streamName.replace("'", "%27")
        };
        try (Cursor cursor = db.query(table, columnsSeek, whereClause, args, null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    public void removeSeekID(String table, String streamID, String streamName) {
        if (table == null || streamID == null || streamName == null){
            return;
        }
        try {
            if (Boolean.TRUE.equals(checkSeek(table,streamID, streamName))) {
                String whereClause = TAG_USER_ID + TAG_AND_Q + TAG_MOVIE_STREAM_ID + TAG_AND_Q + TAG_MOVIE_TITLE + "=?";
                String[] args = {
                        spHelper.getUserId(),
                        streamID,
                        streamName.replace("'", "%27")
                };
                db.delete(table, whereClause, args);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove seek id", e);
        }
    }

    // Download ------------------------------------------------------------------------------------
    @SuppressLint("Range")
    public List<ItemVideoDownload> loadDataDownload(String table) {
        ArrayList<ItemVideoDownload> arrayList = new ArrayList<>();
        if (table == null){
            return arrayList;
        }
        try (Cursor cursor = db.query(table, columnsDownload, null, null,
                null, null, "")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_ID));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_NAME)).replace("%27", "'");
                    String imageBig = Uri.fromFile(new File(encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_ICON))))).toString();
                    String container = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_CONTAINER));
                    String tempName = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_TEMP_NAME));
                    String url = Objects.requireNonNull(context.getExternalFilesDir("")).getAbsolutePath() + File.separator + "temp/" + tempName;

                    ItemVideoDownload objItem = new ItemVideoDownload(name, id, imageBig, url, container);
                    objItem.setTempName(tempName);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading download", e);
        }
        return arrayList;
    }

    public void addToDownloads(String table, ItemVideoDownload itemDownload) {
        if (itemDownload == null || table == null) {
            return; // Exit early if itemDownload is null.
        }

        try {
            // Perform necessary manipulations and encryption.
            String name = itemDownload.getName().replace("'", "%27"); // Replace single quotes with %27.
            String imageBig = encryptData.encrypt(itemDownload.getStreamIcon());
            String url = encryptData.encrypt(itemDownload.getVideoURL());

            // Create ContentValues to insert data into the database.
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_USER_ID, spHelper.getUserId());
            contentValues.put(TAG_DOWNLOAD_ID, itemDownload.getStreamID());
            contentValues.put(TAG_DOWNLOAD_NAME, name);
            contentValues.put(TAG_DOWNLOAD_ICON, imageBig);
            contentValues.put(TAG_DOWNLOAD_URL, url);
            contentValues.put(TAG_DOWNLOAD_CONTAINER, itemDownload.getContainerExtension());
            contentValues.put(TAG_DOWNLOAD_TEMP_NAME, itemDownload.getTempName());

            // Insert the record into the database.
            db.insert(table, null, contentValues);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error adding to downloads", e);
        }
    }

    public Boolean checkDownload(String table, String id, String container) {
        if (table == null || id == null || container == null){
            return false;
        }
        boolean isDownloaded;
        try {
            File root = new File(Objects.requireNonNull(context.getExternalFilesDir("")).getAbsolutePath() + "/temp");
            Cursor cursor = db.query(table, columnsDownload, TAG_USER_ID + "=" + spHelper.getUserId() + TAG_AND + TAG_DOWNLOAD_ID + "=" + id,
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOAD_TEMP_NAME));
                File file = new File(root, filename + container);
                isDownloaded = file.exists();
                cursor.close();
            } else {
                isDownloaded = false;
            }
        } catch (Exception e) {
            isDownloaded = false;
        }
        return isDownloaded;
    }

    public void removeFromDownload(String table, String streamID) {
        if (table == null || streamID == null){
            return;
        }
        try {
            db.delete(table, TAG_DOWNLOAD_ID + "=" + streamID, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove download", e);
        }
    }

    // clear Data ----------------------------------------------------------------------------------
    public void clearData(String table) {
        if (table == null){
            return;
        }
        try {
            db.delete(table, TAG_USER_ID + "=" + spHelper.getUserId(), null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error clearPlayback", e);
        }
    }

    @Override
    public synchronized void close () {
        if (db != null && db.isOpen()) {
            db.close();
            super.close();
        }
    }
}