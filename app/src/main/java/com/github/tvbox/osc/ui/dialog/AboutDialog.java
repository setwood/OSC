package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import com.github.tvbox.osc.R;

public class AboutDialog extends BaseDialog {
    public AboutDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_about);
    }
}
