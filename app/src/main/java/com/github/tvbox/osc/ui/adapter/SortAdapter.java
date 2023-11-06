package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MovieSort;
import java.util.ArrayList;

public class SortAdapter extends BaseQuickAdapter<MovieSort.SortData, BaseViewHolder> {
    public SortAdapter() {
        super(R.layout.item_home_sort, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, MovieSort.SortData sortData) {
        baseViewHolder.setText(R.id.tvTitle, sortData.name);
    }
}
