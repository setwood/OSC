package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.Movie;
import java.util.ArrayList;

public class QuickSearchAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    public QuickSearchAdapter() {
        super(R.layout.item_quick_search_lite, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, Movie.Video video) {
        Object[] objArr = new Object[4];
        objArr[0] = ApiConfig.get().getSource(video.sourceKey).getName();
        objArr[1] = video.name;
        String str = "";
        objArr[2] = video.type == null ? str : video.type;
        if (video.note != null) {
            str = video.note;
        }
        objArr[3] = str;
        baseViewHolder.setText(R.id.tvName, String.format("%s  %s %s %s", objArr));
    }
}
