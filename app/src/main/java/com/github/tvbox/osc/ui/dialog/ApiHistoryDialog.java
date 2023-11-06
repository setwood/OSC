package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import java.util.List;

public class ApiHistoryDialog extends BaseDialog {
    public ApiHistoryDialog(Context context) {
        super(context, R.style.CustomDialogStyleDim);
        setContentView(R.layout.dialog_api_history);
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.ui.dialog.BaseDialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public void setTip(String str) {
        ((TextView) findViewById(2131296930)).setText(str);
    }

    public void setAdapter(ApiHistoryDialogAdapter.SelectDialogInterface selectDialogInterface, List<String> list, final int i) {
        ApiHistoryDialogAdapter apiHistoryDialogAdapter = new ApiHistoryDialogAdapter(selectDialogInterface);
        apiHistoryDialogAdapter.setData(list, i);
        final TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.list);
        tvRecyclerView.setAdapter(apiHistoryDialogAdapter);
        tvRecyclerView.setSelectedPosition(i);
        tvRecyclerView.post(new Runnable() {
            /* class com.github.tvbox.osc.ui.dialog.ApiHistoryDialog.AnonymousClass1 */

            public void run() {
                tvRecyclerView.scrollToPosition(i);
            }
        });
    }
}
