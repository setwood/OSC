package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.MD5;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import java.util.ArrayList;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class FastSearchAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    public FastSearchAdapter() {
        super(R.layout.item_search, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, Movie.Video video) {
        baseViewHolder.setText(R.id.tvName, video.name);
        baseViewHolder.setText(R.id.tvSite, ApiConfig.get().getSource(video.sourceKey).getName());
        baseViewHolder.setVisible(R.id.tvNote, video.note != null && !video.note.isEmpty());
        if (video.note != null && !video.note.isEmpty()) {
            baseViewHolder.setText(R.id.tvNote, video.note);
        }
        ImageView imageView = (ImageView) baseViewHolder.getView(R.id.ivThumb);
        if (!TextUtils.isEmpty(video.pic)) {
            RequestCreator load = Picasso.get().load(video.pic);
            load.transform(new RoundTransformation(MD5.string2MD5(video.pic + "position=" + baseViewHolder.getLayoutPosition())).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView);
            return;
        }
        imageView.setImageResource(R.drawable.img_loading_placeholder);
    }
}
