package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.MD5;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import java.util.ArrayList;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class HistoryAdapter extends BaseQuickAdapter<VodInfo, BaseViewHolder> {
    public HistoryAdapter() {
        super(R.layout.item_grid, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, VodInfo vodInfo) {
        ((TextView) baseViewHolder.getView(R.id.tvYear)).setText(ApiConfig.get().getSource(vodInfo.sourceKey).getName());
        baseViewHolder.setVisible(R.id.tvLang, false);
        baseViewHolder.setVisible(R.id.tvArea, false);
        if (vodInfo.note == null || vodInfo.note.isEmpty()) {
            baseViewHolder.setVisible(R.id.tvNote, false);
        } else {
            baseViewHolder.setText(R.id.tvNote, vodInfo.note);
        }
        baseViewHolder.setText(R.id.tvName, vodInfo.name);
        ImageView imageView = (ImageView) baseViewHolder.getView(R.id.ivThumb);
        if (!TextUtils.isEmpty(vodInfo.pic)) {
            RequestCreator load = Picasso.get().load(DefaultConfig.checkReplaceProxy(vodInfo.pic));
            load.transform(new RoundTransformation(MD5.string2MD5(vodInfo.pic + vodInfo.name)).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView);
            return;
        }
        imageView.setImageResource(R.drawable.img_loading_placeholder);
    }
}
