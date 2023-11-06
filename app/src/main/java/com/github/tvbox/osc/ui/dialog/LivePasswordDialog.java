package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import com.github.tvbox.osc.R;

public class LivePasswordDialog extends BaseDialog {
    private EditText inputPassword;
    OnListener listener = null;

    public interface OnListener {
        void onCancel();

        void onChange(String str);
    }

    public LivePasswordDialog(Context context) {
        super(context);
        setOwnerActivity((Activity) context);
        setContentView(R.layout.dialog_live_password);
        this.inputPassword = (EditText) findViewById(R.id.input);
        findViewById(R.id.inputSubmit).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.LivePasswordDialog.AnonymousClass1 */

            public void onClick(View view) {
                String trim = LivePasswordDialog.this.inputPassword.getText().toString().trim();
                if (!trim.isEmpty()) {
                    LivePasswordDialog.this.listener.onChange(trim);
                    LivePasswordDialog.this.dismiss();
                }
            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.listener.onCancel();
        dismiss();
    }

    public void setOnListener(OnListener onListener) {
        this.listener = onListener;
    }
}
