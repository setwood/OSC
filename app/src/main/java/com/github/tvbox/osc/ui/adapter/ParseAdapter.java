package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.ParseBean;
import java.util.ArrayList;

public class ParseAdapter extends BaseQuickAdapter<ParseBean, BaseViewHolder> {
    public ParseAdapter() {
        super(R.layout.item_play_parse, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, ParseBean parseBean) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvParse);
        textView.setVisibility(0);
        if (parseBean.isDefault()) {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        } else {
            textView.setTextColor(-1);
        }
        textView.setText(parseBean.getName());
    }
}
