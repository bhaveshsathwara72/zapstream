package nemosofts.streambox.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import nemosofts.streambox.R;
import nemosofts.streambox.utils.IfSupported;

public class LoginDialog extends Dialog {

    private final String userName;

    public LoginDialog(Context context, String anyName) {
        super(context, R.style.FullScreenDialogTheme);
        this.userName = anyName;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_login);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        TextView dialogWelcome = findViewById(R.id.dialog_welcome);
        if (dialogWelcome != null) {
            dialogWelcome.setText("Welcome " + userName);
        }

        Window window = getWindow();
        if (window != null) {
            IfSupported.hideStatusBarDialog(window);
            window.setLayout(MATCH_PARENT, MATCH_PARENT);
        }
    }
}