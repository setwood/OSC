package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import java.util.ArrayList;

public class SeriesAdapter extends BaseQuickAdapter<VodInfo.VodSeries, BaseViewHolder> {
    public SeriesAdapter() {
        super(R.layout.item_series, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, VodInfo.VodSeries vodSeries) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvSeries);
        if (vodSeries.selected) {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        } else {
            textView.setTextColor(-1);
        }
        baseViewHolder.setText(R.id.tvSeries, vodSeries.name);
    }
}
