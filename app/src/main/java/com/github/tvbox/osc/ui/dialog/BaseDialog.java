package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import com.github.tvbox.osc.R;
import xyz.doikki.videoplayer.util.CutoutUtil;

public class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context, R.style.CustomDialogStyle);
    }

    public BaseDialog(Context context, int i) {
        super(context, i);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        CutoutUtil.adaptCutoutAboveAndroidP((Dialog) this, true);
        super.onCreate(bundle);
    }

    public void show() {
        getWindow().setFlags(8, 8);
        super.show();
        hideSysBar();
        getWindow().clearFlags(8);
    }

    private void hideSysBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | 256 | 1024 | 4 | 4096);
        }
    }
}
