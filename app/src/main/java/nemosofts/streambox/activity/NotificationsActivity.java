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
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nemosofts.streambox.R;
import nemosofts.streambox.adapter.AdapterNotification;
import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.IfSupported;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private FrameLayout frameLayout;

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
        setupRecyclerView();

        if (Callback.getArrayListNotify().isEmpty()){
            setEmpty();
        } else {
            AdapterNotification adapter = new AdapterNotification(Callback.getArrayListNotify(), (notification, position) -> {
                Callback.setPosNotify(position);
                startActivity(new Intent(NotificationsActivity.this, NotificationsWebActivity.class));
            });
            rv.setAdapter(adapter);
            setEmpty();
        }
    }

    private void initializeUI() {
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        findViewById(R.id.theme_bg).setBackgroundResource(ApplicationUtil.openThemeBg(this));
        findViewById(R.id.iv_back_page).setOnClickListener(view -> finish());
        if (DeviceUtils.isTvBox(this)){
            findViewById(R.id.iv_back_page).setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    private void setEmpty() {
        if (!Callback.getArrayListNotify().isEmpty()) {
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
    public int setContentViewID() {
        return R.layout.activity_notifications;
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
}