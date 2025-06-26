package nemosofts.streambox.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import nemosofts.streambox.utils.helper.SPHelper;

public class IfSupported {

    private static final String TAG = "IfSupported";

    private IfSupported() {
        throw new IllegalStateException("Utility class");
    }

    public static void isRTL(Activity activity) {
        if (activity == null) {
            ApplicationUtil.log(TAG, "Activity context is null in isRTL");
            return;
        }
        try {
            if (Boolean.TRUE.equals(new SPHelper(activity).getIsRTL())) {
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController insetsController = window.getInsetsController();
                    if (insetsController != null) {
                        insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        insetsController.hide(WindowInsets.Type.systemBars());
                    }
                }
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to apply RTL layout direction", e);
        }
    }

    public static void isScreenshot(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null isScreenshot");
            return;
        }
        try {
            if (Boolean.TRUE.equals(new SPHelper(mContext).getIsScreenshot())) {
                Window window = mContext.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to isScreenshot", e);
        }
    }

    public static void keepScreenOn(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null keepScreenOn");
            return;
        }
        try {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to keep screen on", e);
        }
    }

    public static void hideStatusBar(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null hideStatusBar");
            return;
        }
        try {
            Window window = mContext.getWindow();
            hideStatusBarDialog(window);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to hide status bar", e);
        }
    }

    public static void hideStatusBarDialog(Window window) {
        if (window == null) {
            Log.e(TAG, "Window is null");
            return;
        }
        try {
            View decorView = window.getDecorView();

            // Allow layout to extend behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Use AndroidX controller to manage insets across all versions
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, decorView);

            // Hide both status and navigation bars
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());

            // Enable immersive sticky (swipe to show system bars temporarily)
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                // Optional: Set navigation bar color
                window.setNavigationBarColor(Color.BLACK);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to hide status bar dialog", e);
        }
    }

    public static void statusAndNavigationBars(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null barColor");
            return;
        }
        try {
            Window window = mContext.getWindow();
            View decorView = window.getDecorView();

            // Allow content to draw behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                // Make status bar fully transparent
                window.setStatusBarColor(Color.TRANSPARENT);

                // Set the navigation bar color (customize as needed)
                window.setNavigationBarColor(Color.BLACK);
            }

            // Use AndroidX controller to manage insets across all versions
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, decorView);

            // Set icon appearance (light == dark icons disabled)
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to hide status bar dialog", e);
        }
    }

    public static void playerHideStatusBar(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null player Hide StatusBar");
            return;
        }
        try {
            Window window = mContext.getWindow();
            View decorView = window.getDecorView();

            // Allow content to extend behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Use compatible insets controller
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decorView);

            // Hide status and navigation bars
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());

            // Enable immersive sticky behavior (swipe to temporarily show bars)
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to hide player status bar", e);
        }
    }

    /**
     * Check if a Developer Mode is enabled.
     */
    public static boolean isDeveloperModeEnabled(Activity activity) {
        if (activity == null) {
            ApplicationUtil.log(TAG, "Activity context is null isDeveloperModeEnabled");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return Settings.Global.getInt(activity.getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
            } catch (SecurityException e) {
                ApplicationUtil.log(TAG, "No permission to access developer settings");
                return false;
            }
        }
        ApplicationUtil.log(TAG, "No permission to access developer settings");
        return false;
    }

    /**
     * Check if a VPN is active.
     */
    public static boolean isVPNActive(Activity activity) {
        if (activity == null) {
            ApplicationUtil.log(TAG, "Activity context is null isVPNActive");
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }
}