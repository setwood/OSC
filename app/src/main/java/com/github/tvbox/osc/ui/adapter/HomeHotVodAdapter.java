package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.MD5;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import java.util.ArrayList;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class HomeHotVodAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    public HomeHotVodAdapter() {
        super(R.layout.item_user_hot_vod, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, Movie.Video video) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvNote);
        if (video.note == null || video.note.isEmpty()) {
            textView.setVisibility(8);
        } else {
            textView.setText(video.note);
            textView.setVisibility(0);
        }
        baseViewHolder.setText(R.id.tvName, video.name);
        ImageView imageView = (ImageView) baseViewHolder.getView(R.id.ivThumb);
        if (!TextUtils.isEmpty(video.pic)) {
            RequestCreator load = Picasso.get().load(DefaultConfig.checkReplaceProxy(video.pic));
            load.transform(new RoundTransformation(MD5.string2MD5(video.pic + "position=" + baseViewHolder.getLayoutPosition())).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView);
            return;
        }
        imageView.setImageResource(R.drawable.img_loading_placeholder);
    }
}
