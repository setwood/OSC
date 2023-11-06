package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.XWalkUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import java.io.File;

public class XWalkInitDialog extends BaseDialog {
    private OnListener listener;

    public interface OnListener {
        void onchange();
    }

    public XWalkInitDialog(final Context context) {
        super(context);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setContentView(R.layout.dialog_xwalk);
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.github.tvbox.osc.ui.dialog.XWalkInitDialog.AnonymousClass1 */

            public void onDismiss(DialogInterface dialogInterface) {
                OkGo.getInstance().cancelTag("down_xwalk");
            }
        });
        final TextView textView = (TextView) findViewById(R.id.downXWalk);
        ((TextView) findViewById(R.id.downXWalkArch)).setText("下载XWalkView运行组件\nArch:" + XWalkUtils.getRuntimeAbi());
        if (XWalkUtils.xWalkLibExist(context)) {
            textView.setText("重新下载");
        }
        textView.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.XWalkInitDialog.AnonymousClass2 */

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private void setTextEnable(boolean z) {
                textView.setEnabled(z);
                textView.setTextColor(z ? ViewCompat.MEASURED_STATE_MASK : -7829368);
            }

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                setTextEnable(false);
                ((GetRequest) OkGo.get(XWalkUtils.downUrl()).tag("down_xwalk")).execute(new FileCallback(context.getCacheDir().getAbsolutePath(), XWalkUtils.saveZipFile()) {
                    /* class com.github.tvbox.osc.ui.dialog.XWalkInitDialog.AnonymousClass2.AnonymousClass1 */

                    @Override // com.lzy.okgo.callback.Callback
                    public void onSuccess(Response<File> response) {
                        try {
                            XWalkUtils.unzipXWalkZip(context, response.body().getAbsolutePath());
                            XWalkUtils.extractXWalkLib(context);
                            textView.setText("重新下载");
                            if (XWalkInitDialog.this.listener != null) {
                                XWalkInitDialog.this.listener.onchange();
                            }
                            XWalkInitDialog.this.dismiss();
                        } catch (Throwable th) {
                            th.printStackTrace();
                            Toast.makeText(context, th.getMessage(), 1).show();
                            AnonymousClass2.this.setTextEnable(true);
                        }
                    }

                    @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                    public void onError(Response<File> response) {
                        super.onError(response);
                        Toast.makeText(context, response.getException().getMessage(), 1).show();
                        AnonymousClass2.this.setTextEnable(true);
                    }

                    @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        textView.setText(String.format("%.2f%%", Float.valueOf(progress.fraction * 100.0f)));
                    }
                });
            }
        });
    }

    public XWalkInitDialog setOnListener(OnListener onListener) {
        this.listener = onListener;
        return this;
    }
}
