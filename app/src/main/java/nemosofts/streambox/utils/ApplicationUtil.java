package nemosofts.streambox.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Rational;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.utils.FormatUtils;
import androidx.nemosofts.utils.ThemoviedbAPI;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nemosofts.streambox.BuildConfig;
import nemosofts.streambox.R;
import nemosofts.streambox.activity.PlaylistActivity;
import nemosofts.streambox.activity.SelectPlayerActivity;
import nemosofts.streambox.activity.SingleStreamActivity;
import nemosofts.streambox.activity.ui.BlackPantherActivity;
import nemosofts.streambox.activity.ui.ChristmasActivity;
import nemosofts.streambox.activity.ui.GlossyActivity;
import nemosofts.streambox.activity.ui.HalloweenActivity;
import nemosofts.streambox.activity.ui.MovieUiActivity;
import nemosofts.streambox.activity.ui.OneUIActivity;
import nemosofts.streambox.activity.ui.RamadanActivity;
import nemosofts.streambox.activity.ui.SportsActivity;
import nemosofts.streambox.activity.ui.VUIActivity;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.utils.helper.SPHelper;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;

public class ApplicationUtil {

    private ApplicationUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String TAG = "ApplicationUtil";

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String ACTION_KEY = "action";

    private static final Random RANDOM = new Random();
    public static int getRandomValue(int bound) {
        return RANDOM.nextInt(bound);
    }

    @NonNull
    public static String responsePost(String url, RequestBody requestBody) {
        // Set up logging for HTTP requests and responses
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cache(null)
                .build();

        // Build the POST request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                return executeGetRequestFromPostBody(url, requestBody, client);
            }
        } catch (IOException e) {
            log(TAG, "Error during POST request, attempting GET fallback.", e);
            return executeGetRequestFromPostBody(url, requestBody, client);
        }
    }

    @NonNull
    private static String executeGetRequestFromPostBody(String url, RequestBody requestBody, OkHttpClient client) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (requestBody instanceof FormBody formBody) {
            for (int i = 0; i < formBody.size(); i++) {
                urlBuilder.addQueryParameter(formBody.name(i), formBody.value(i));
            }
        } else if (requestBody instanceof MultipartBody multipartBody) {
            extractMultipartParams(multipartBody, urlBuilder);
        }

        Request getRequest = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = client.newCall(getRequest).execute()) {
            return response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            log(TAG, "Error during GET request retry.", e);
            return "";
        }
    }

    private static void extractMultipartParams(MultipartBody multipartBody, HttpUrl.Builder urlBuilder) {
        for (MultipartBody.Part part : multipartBody.parts()) {
            RequestBody body = part.body();
            Headers headers = part.headers();
            if (headers != null) {
                String contentDisposition = headers.get("Content-Disposition");
                if (contentDisposition != null) {
                    Matcher matcher = Pattern.compile("name=\"([^\"]*)\"").matcher(contentDisposition);
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        try (Buffer buffer = new Buffer()) {
                            body.writeTo(buffer);
                            urlBuilder.addQueryParameter(name, buffer.readUtf8());
                        } catch (IOException e) {
                            log(TAG, "Error reading MultipartBody part for GET parameters.", e);
                        }
                    }
                }
            }
        }
    }

    @NonNull
    public static String getMovieCredits(String movieID, String token) {
        if (movieID == null || token == null) {
            return "";
        }
        return executeRequest(ThemoviedbAPI.getMovieCredits(movieID), token);
    }

    @NonNull
    public static String getMovieImages(String movieID, String token) {
        if (movieID == null || token == null) {
            return "";
        }
        return executeRequest(ThemoviedbAPI.getMovieImages(movieID), token);
    }

    private static String executeRequest(String url, String token) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            return "";
        }
    }

    @NonNull
    public static RequestBody getAPIRequestLogin(String username, String password) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(USERNAME_KEY, username)
                .addFormDataPart(PASSWORD_KEY, password)
                .build();
    }

    @NonNull
    public static RequestBody getAPIRequest(String action, String username, String password) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(USERNAME_KEY, username)
                .addFormDataPart(PASSWORD_KEY, password)
                .addFormDataPart(ACTION_KEY, action)
                .build();
    }

    @NonNull
    public static RequestBody getAPIRequestID(String action, String type, String seriesID,
                                               String username, String password) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(USERNAME_KEY, username)
                .addFormDataPart(PASSWORD_KEY, password)
                .addFormDataPart(ACTION_KEY, action)
                .addFormDataPart(type, seriesID)
                .build();
    }

    public static Boolean calculateUpdateHours(@NonNull String inputDateStr, int updateHours){
        boolean isUpdate = false;
        try {
            if (!inputDateStr.isEmpty()){
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date inputDate = dateFormat.parse(inputDateStr);
                Date currentDate = new Date();
                assert inputDate != null;
                long timeDifferenceInMillis = currentDate.getTime() - inputDate.getTime();
                long seconds = timeDifferenceInMillis / 1000;
                int hours = (int) (seconds / 3600);
                isUpdate = hours > updateHours;
            }
        } catch (Exception e) {
            return isUpdate;
        }
        return isUpdate;
    }

    @NonNull
    public static String averageRating(String rating) {
        if (rating == null || rating.isEmpty()) {
            return "0";
        }

        try {
            float floatRating = Float.parseFloat(rating);
            int roundedRating = Math.max(0, Math.min(5, (int) Math.floor(floatRating)));
            return String.valueOf(roundedRating);
        } catch (NumberFormatException e) {
            return "0";  // Return "0" for invalid numeric formats
        }
    }

    @NonNull
    @Contract("_ -> new")
    public static Rational getRational(final Format format) {
        if (format == null || format.width <= 0 || format.height <= 0) {
            // Default to 16:9 if format is null or has invalid dimensions
            return new Rational(16, 9);
        }
        if (isRotated(format)){
            return new Rational(format.height, format.width);
        } else {
            return new Rational(format.width, format.height);
        }
    }

    @Contract(pure = true)
    @OptIn(markerClass = UnstableApi.class)
    public static boolean isRotated(@NonNull final Format format) {
        return format.rotationDegrees == 90 || format.rotationDegrees == 270;
    }

    public static void openThemeActivity(Activity activity) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        int theme = new SPHelper(activity).getIsTheme();
        Intent intent;
        if (theme == 2){
            intent = new Intent(activity, GlossyActivity.class);
        } else  if (theme == 3){
            intent = new Intent(activity, BlackPantherActivity.class);
        } else  if (theme == 4){
            intent = new Intent(activity, MovieUiActivity.class);
        } else if (theme == 5){
            intent = new Intent(activity, VUIActivity.class);
        } else if (theme == 6){
            intent = new Intent(activity, ChristmasActivity.class);
        } else if (theme == 7){
            intent = new Intent(activity, HalloweenActivity.class);
        } else if (theme == 8){
            intent = new Intent(activity, RamadanActivity.class);
        } else if (theme == 9){
            intent = new Intent(activity, SportsActivity.class);
        } else {
            intent = new Intent(activity, OneUIActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void openHomeActivity(Activity activity) {
        SPHelper spHelper = new SPHelper(activity);
        Intent intent;
        if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_SINGLE_STREAM)){
            intent = new Intent(activity, SingleStreamActivity.class);
        } else if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_PLAYLIST)){
            intent = new Intent(activity, PlaylistActivity.class);
        } else if (spHelper.getLoginType().equals(Callback.TAG_LOGIN_ONE_UI) || spHelper.getLoginType().equals(Callback.TAG_LOGIN_STREAM)){
            int theme = spHelper.getIsTheme();
            if (theme == 2){
                intent = new Intent(activity, GlossyActivity.class);
            }  else if (theme == 3){
                intent = new Intent(activity, BlackPantherActivity.class);
            } else if (theme == 4){
                intent = new Intent(activity, MovieUiActivity.class);
            } else if (theme == 5){
                intent = new Intent(activity, VUIActivity.class);
            } else if (theme == 6){
                intent = new Intent(activity, ChristmasActivity.class);
            } else if (theme == 7){
                intent = new Intent(activity, HalloweenActivity.class);
            }  else if (theme == 8){
                intent = new Intent(activity, RamadanActivity.class);
            } else if (theme == 9){
                intent = new Intent(activity, SportsActivity.class);
            } else {
                intent = new Intent(activity, OneUIActivity.class);
            }
        } else {
            intent = new Intent(activity, SelectPlayerActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        activity.startActivity(intent);
        activity.finish();
    }

    public static int openThemeBg(Activity activity) {
        int theme = new SPHelper(activity).getIsTheme();
        int themePage = new ThemeEngine(activity).getThemePage();
        return switch (theme) {
            case 2 -> R.drawable.bg_ui_glossy;
            case 3 -> R.drawable.bg_dark_panther;
            default -> switch (themePage) {
                case 1, 2, 3 -> R.drawable.bg_classic;
                default -> R.drawable.bg_dark;
            };
        };
    }

    @NonNull
    @Contract(pure = true)
    public static String containerExtension(@Nullable String container) {
        if (container == null || container.isEmpty()) {
            return ".mp4";
        }
        return container.contains(".") ? container : "." + container;
    }

    @NonNull
    @Contract(pure = true)
    public static Boolean isAdultsCount(@NonNull String count) {
        String normalizedCount = count.toLowerCase();
        // List of keywords related to adult content
        String[] adultKeywords = {"18+", "+18", "[18+]", "adults", "adult", "xxx", "pron", "sex"};
        // Check if any of the keywords are present in the normalized string
        for (String keyword : adultKeywords) {
            if (normalizedCount.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static int getBatteryDrawable(int status, int level, int scale) {
        float batteryLevel = (level / (float) scale) * 100;
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);
        if (isCharging){
            return R.drawable.ic_battery_charging;
        }

        if (batteryLevel < 10){
            return R.drawable.ic_battery_disable;
        } else if (batteryLevel < 20){
            return R.drawable.ic_battery_empty;
        } else if (batteryLevel < 30){
            return R.drawable.ic_battery_one;
        } else if (batteryLevel < 50){
            return R.drawable.ic_battery_two;
        } else {
            return R.drawable.ic_battery_full;
        }
    }

    public static void setRating(String rating, ImageView star1, ImageView star2, ImageView star3,
                                 ImageView star4, ImageView star5) {
        if (rating == null) {
            return;
        }

        try {
            String avg = averageRating(rating);
            int starCount = Integer.parseInt(avg);
            ImageView[] stars = {star1, star2, star3, star4, star5};
            for (int i = 0; i < stars.length; i++) {
                if (stars[i] != null) {
                    stars[i].setImageResource(i < starCount ? R.drawable.ic_star : R.drawable.ic_star_border);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "setRating", e);
        }
    }

    @NonNull
    public static CharSequence setErrorMsg(String errorMsg) {
        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(errorMsg);
            builder. setSpan(new ForegroundColorSpan(Color.RED), 12, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return builder;
        } catch (Exception e) {
            return errorMsg;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    public static String getInfoAudio( ExoPlayer exoPlayer) {
        String infoAudio = """
        Audio Sample Rate: N/A
    
        Audio Channels: N/A
    
        Audio Type: N/A
    
        Audio MIME Type: N/A
        """;

        if (exoPlayer == null){
            return infoAudio;
        }
        if (exoPlayer.getAudioFormat() != null){
            int audioSampleRate = exoPlayer.getAudioFormat().sampleRate;
            int audioChannels = exoPlayer.getAudioFormat().channelCount;
            String audioMimeType = exoPlayer.getAudioFormat().sampleMimeType;

            infoAudio = "Audio Sample Rate: " + audioSampleRate + "\n\n"
                    + "Audio Channels: " + audioChannels + "\n\n"
                    + "Audio Type: "+ formatAudioFromMime(audioMimeType) +"\n\n"
                    + "Audio MIME Type: " + audioMimeType +"\n";

        }
        return infoAudio;
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    public static String getInfoVideo(ExoPlayer exoPlayer, boolean isLive) {
        String infoVideo = """
        Video Quality : Unknown resolution
    
        Video Width: N/A
    
        Video Height: N/A
        """;

        if (exoPlayer == null){
            return infoVideo;
        }
        if (exoPlayer.getVideoFormat() != null){
            int videoWidth = exoPlayer.getVideoFormat().width;
            int videoHeight = exoPlayer.getVideoFormat().height;
            int videoBitrate = exoPlayer.getVideoFormat().bitrate;
            float frameRate = exoPlayer.getVideoFormat().frameRate;
            String finalRate = FormatUtils.formatFrameRate(frameRate);

            if (isLive){
                infoVideo = "Video Quality: " + FormatUtils.formatVideoResolution(videoHeight)+ "\n\n"
                        + "Video Width: " + videoWidth + "\n\n"
                        + "Video Height: " + videoHeight + "\n";
            } else {
                infoVideo = "Video Quality: " + FormatUtils.formatVideoResolution(videoHeight)+ "\n\n"
                        + "Video Width: " + videoWidth + "\n\n"
                        + "Video Height: " + videoHeight + "\n\n"
                        + "Video Bitrate: " + videoBitrate + "\n\n"
                        + "Video Frame Rate: " + finalRate + "\n";
            }
        }
        return infoVideo;
    }

    @OptIn(markerClass = UnstableApi.class)
    public static String formatAudioFromMime(final String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return "N/A";
        }
        return switch (mimeType) {
            case MimeTypes.AUDIO_DTS -> "DTS";
            case MimeTypes.AUDIO_DTS_HD -> "DTS-HD";
            case MimeTypes.AUDIO_DTS_EXPRESS -> "DTS Express";
            case MimeTypes.AUDIO_TRUEHD -> "TrueHD";
            case MimeTypes.AUDIO_AC3 -> "AC-3";
            case MimeTypes.AUDIO_E_AC3 -> "E-AC-3";
            case MimeTypes.AUDIO_E_AC3_JOC -> "E-AC-3-JOC";
            case MimeTypes.AUDIO_AC4 -> "AC-4";
            case MimeTypes.AUDIO_AAC -> "AAC";
            case MimeTypes.AUDIO_MPEG -> "MP3";
            case MimeTypes.AUDIO_MPEG_L2 -> "MP2";
            case MimeTypes.AUDIO_VORBIS -> "Vorbis";
            case MimeTypes.AUDIO_OPUS -> "Opus";
            case MimeTypes.AUDIO_FLAC -> "FLAC";
            case MimeTypes.AUDIO_ALAC -> "ALAC";
            case MimeTypes.AUDIO_WAV -> "WAV";
            case MimeTypes.AUDIO_AMR -> "AMR";
            case MimeTypes.AUDIO_AMR_NB -> "AMR-NB";
            case MimeTypes.AUDIO_AMR_WB -> "AMR-WB";
            case MimeTypes.AUDIO_IAMF -> "IAMF";
            case MimeTypes.AUDIO_MPEGH_MHA1, MimeTypes.AUDIO_MPEGH_MHM1 -> "MPEG-H";
            case MimeTypes.APPLICATION_PGS -> "PGS";
            case MimeTypes.APPLICATION_SUBRIP -> "SRT";
            case MimeTypes.TEXT_SSA -> "SSA";
            case MimeTypes.TEXT_VTT -> "VTT";
            case MimeTypes.APPLICATION_TTML -> "TTML";
            case MimeTypes.APPLICATION_TX3G -> "TX3G";
            case MimeTypes.APPLICATION_DVBSUBS -> "DVB";
            default -> mimeType;
        };
    }

    // Helper method to get the volume -------------------------------------------------------------
    public static int getVolume(final Context context, final boolean max, final AudioManager audioManager) {
        // Handle Samsung-specific volume retrieval for Android SDK 30 and above
        if (isSamsungDeviceWithSDK30OrAbove()) {
            Integer samsungVolume = getSamsungVolume(context, max, audioManager);
            if (samsungVolume != null) {
                return samsungVolume;
            }
        }

        // Fallback to standard volume retrieval
        return max ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    // Checks if the device is Samsung and running SDK 30 or above
    private static boolean isSamsungDeviceWithSDK30OrAbove() {
        return Build.VERSION.SDK_INT >= 30 && Build.MANUFACTURER.equalsIgnoreCase("samsung");
    }

    // Retrieves the Samsung-specific volume if possible, otherwise returns null
    @Nullable
    private static Integer getSamsungVolume(Context context, boolean max, AudioManager audioManager) {
        try {
            int mediaVolumeInterval = getSamsungMediaVolumeInterval(context);
            if (mediaVolumeInterval < 10) {
                return retrieveFineVolume(max, audioManager, mediaVolumeInterval);
            }
        }  catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                  IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "getSamsungVolume", e);
        }
        return null;
    }

    // Gets the media volume interval from Samsung's SemSoundAssistantManager
    private static int getSamsungMediaVolumeInterval(Context context)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName("com.samsung.android.media.SemSoundAssistantManager");
        Constructor<?> constructor = clazz.getConstructor(Context.class);
        Method getMediaVolumeInterval = clazz.getDeclaredMethod("getMediaVolumeInterval");
        Object result = getMediaVolumeInterval.invoke(constructor.newInstance(context));
        return result instanceof Integer ? (int) result : 10; // Default interval if not found
    }

    // Retrieves the fine volume using Samsung's semGetFineVolume method
    @Nullable
    private static Integer retrieveFineVolume(boolean max, AudioManager audioManager,
                                              int mediaVolumeInterval) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method = AudioManager.class.getDeclaredMethod("semGetFineVolume", int.class);
        Object result = method.invoke(audioManager, AudioManager.STREAM_MUSIC);
        if (result instanceof Integer) {
            int fineVolume = (int) result;
            return max ? 150 / mediaVolumeInterval : fineVolume / mediaVolumeInterval;
        }
        return null;
    }

    @Nullable
    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        try {
            @SuppressLint("Recycle") Cursor cursor = ctx.getContentResolver().query(contentUri,
                    null, null, null, null);
            if (cursor == null) {
                return contentUri.getPath();
            }
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            return cursor.getString(idx);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public static Boolean isVolumeMin(@NonNull AudioManager audioManager) {
        int min = Build.VERSION.SDK_INT >= 28 ? audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) : 0;
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == min;
    }

    @NonNull
    public static String getTimeOfTheDay(Context ctx) {
        if (ctx == null){
            return "";
        }
        String message;
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (timeOfDay < 6) {
            message = ctx.getString(R.string.title_good_night);
        } else if (timeOfDay < 12) {
            message = ctx.getString(R.string.title_good_morning);
        } else if (timeOfDay < 16) {
            message = ctx.getString(R.string.title_good_afternoon);
        } else if (timeOfDay < 20) {
            message = ctx.getString(R.string.title_good_evening);
        } else {
            message = ctx.getString(R.string.title_good_night);
        }
        return message;
    }

    public static void log(String tag, String msg) {
        if (BuildConfig.DEBUG){
            Log.e(tag, msg);
        }
    }

    public static void log(String tag, String msg, Exception e) {
        if (BuildConfig.DEBUG){
            Log.e(tag, msg, e);
        }
    }
}