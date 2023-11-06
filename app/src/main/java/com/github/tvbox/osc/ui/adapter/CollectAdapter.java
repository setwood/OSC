package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.cache.VodCollect;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.MD5;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import java.util.ArrayList;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class CollectAdapter extends BaseQuickAdapter<VodCollect, BaseViewHolder> {
    public CollectAdapter() {
        super(R.layout.item_grid, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, VodCollect vodCollect) {
        baseViewHolder.setVisible(R.id.tvLang, false);
        baseViewHolder.setVisible(R.id.tvArea, false);
        baseViewHolder.setVisible(R.id.tvNote, false);
        baseViewHolder.setText(R.id.tvName, vodCollect.name);
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvYear);
        SourceBean source = ApiConfig.get().getSource(vodCollect.sourceKey);
        textView.setText(source != null ? source.getName() : "");
        ImageView imageView = (ImageView) baseViewHolder.getView(R.id.ivThumb);
        if (!TextUtils.isEmpty(vodCollect.pic)) {
            RequestCreator load = Picasso.get().load(DefaultConfig.checkReplaceProxy(vodCollect.pic));
            load.transform(new RoundTransformation(MD5.string2MD5(vodCollect.pic + vodCollect.name)).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView);
            return;
        }
        imageView.setImageResource(R.drawable.img_loading_placeholder);
    }
}
