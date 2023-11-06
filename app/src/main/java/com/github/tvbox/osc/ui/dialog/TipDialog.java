package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import com.github.tvbox.osc.R;

public class TipDialog extends BaseDialog {

    public interface OnListener {
        void cancel();

        void left();

        void right();
    }

    public TipDialog(Context context, String str, String str2, String str3, final OnListener onListener) {
        super(context);
        setContentView(R.layout.dialog_tip);
        setCanceledOnTouchOutside(false);
        TextView textView = (TextView) findViewById(R.id.leftBtn);
        TextView textView2 = (TextView) findViewById(R.id.rightBtn);
        ((TextView) findViewById(R.id.tipInfo)).setText(str);
        textView.setText(str2);
        textView2.setText(str3);
        textView.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.TipDialog.AnonymousClass1 */

            public void onClick(View view) {
                onListener.left();
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.TipDialog.AnonymousClass2 */

            public void onClick(View view) {
                onListener.right();
            }
        });
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            /* class com.github.tvbox.osc.ui.dialog.TipDialog.AnonymousClass3 */

            public void onCancel(DialogInterface dialogInterface) {
                onListener.cancel();
            }
        });
    }
}
