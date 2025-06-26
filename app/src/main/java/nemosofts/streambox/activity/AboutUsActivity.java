package nemosofts.streambox.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.DeviceUtils;

import nemosofts.streambox.BuildConfig;
import nemosofts.streambox.R;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;
import nemosofts.streambox.utils.helper.SPHelper;

public class AboutUsActivity extends AppCompatActivity {

    private SPHelper spHelper;
    private TextView author;
    private TextView email;
    private TextView website;
    private TextView contact;
    private TextView description;
    private TextView version;

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

        initializeUI();
        initializeViews();
        setAboutUs();
        setupBackPressHandler();
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.statusAndNavigationBars(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));

        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)) {
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_about_us;
    }

    private void initializeViews() {
        spHelper = new SPHelper(this);

        author = findViewById(R.id.tv_company);
        email = findViewById(R.id.tv_email);
        website = findViewById(R.id.tv_website);
        contact = findViewById(R.id.tv_contact);
        description = findViewById(R.id.tv_app_des);
        version = findViewById(R.id.tv_version);
    }

    private void setAboutUs() {
        author.setText(getValidText(spHelper.getAppAuthor()));
        email.setText(getValidText(spHelper.getAppEmail()));
        website.setText(getValidText(spHelper.getAppWebsite()));
        contact.setText(getValidText(spHelper.getAppContact()));
        description.setText(getValidText(spHelper.getAppDescription()));
        version.setText(BuildConfig.VERSION_NAME);
    }

    private String getValidText(String value) {
        return TextUtils.isEmpty(value) ? "N/A" : value;
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_HOME) {
                ApplicationUtil.openHomeActivity(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}