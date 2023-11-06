package com.github.tvbox.osc.ui.adapter;

import android.view.View;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import java.util.ArrayList;

public class SeriesFlagAdapter extends BaseQuickAdapter<VodInfo.VodSeriesFlag, BaseViewHolder> {
    public SeriesFlagAdapter() {
        super(R.layout.item_series_flag, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, VodInfo.VodSeriesFlag vodSeriesFlag) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvSeriesFlag);
        View view = baseViewHolder.getView(R.id.tvSeriesFlagSelect);
        if (vodSeriesFlag.selected) {
            view.setVisibility(0);
        } else {
            view.setVisibility(8);
        }
        baseViewHolder.setText(R.id.tvSeriesFlag, vodSeriesFlag.name);
    }
}
