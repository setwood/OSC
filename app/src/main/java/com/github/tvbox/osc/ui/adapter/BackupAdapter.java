package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import java.util.ArrayList;

public class BackupAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public BackupAdapter() {
        super(R.layout.item_dialog_backup, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, String str) {
        baseViewHolder.setText(R.id.tvName, str);
        baseViewHolder.addOnClickListener(2131296992);
        baseViewHolder.addOnClickListener(2131296967);
    }
}
