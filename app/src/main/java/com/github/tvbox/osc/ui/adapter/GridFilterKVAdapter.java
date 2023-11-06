package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import java.util.ArrayList;

public class GridFilterKVAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public GridFilterKVAdapter() {
        super(R.layout.item_grid_filter_value, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, String str) {
        baseViewHolder.setText(R.id.filterValue, str);
    }
}
