package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import java.util.List;

public class SelectDialog<T> extends BaseDialog {
    private boolean muteCheck = false;

    public SelectDialog(Context context) {
        super(context);
        setContentView((int) R.layout.dialog_select);
    }

    public SelectDialog(Context context, int i) {
        super(context);
        setContentView(i);
    }

    public void setItemCheckDisplay(boolean z) {
        this.muteCheck = !z;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.ui.dialog.BaseDialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public void setTip(String str) {
        ((TextView) findViewById(2131296930)).setText(str);
    }

    public void setAdapter(SelectDialogAdapter.SelectDialogInterface<T> selectDialogInterface, DiffUtil.ItemCallback<T> itemCallback, List<T> list, final int i) {
        SelectDialogAdapter selectDialogAdapter = new SelectDialogAdapter(selectDialogInterface, itemCallback, this.muteCheck);
        selectDialogAdapter.setData(list, i);
        final TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.list);
        tvRecyclerView.setAdapter(selectDialogAdapter);
        tvRecyclerView.setSelectedPosition(i);
        tvRecyclerView.post(new Runnable() {
            /* class com.github.tvbox.osc.ui.dialog.SelectDialog.AnonymousClass1 */

            public void run() {
                tvRecyclerView.scrollToPosition(i);
            }
        });
    }
}
