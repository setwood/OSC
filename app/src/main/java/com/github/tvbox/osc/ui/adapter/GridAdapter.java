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

public class GridAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    private boolean mShowList;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public GridAdapter(boolean z) {
        super(z ? R.layout.item_list : R.layout.item_grid, new ArrayList());
        this.mShowList = false;
        this.mShowList = z;
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, Movie.Video video) {
        if (this.mShowList) {
            baseViewHolder.setText(R.id.tvNote, video.note);
            baseViewHolder.setText(R.id.tvName, video.name);
            ImageView imageView = (ImageView) baseViewHolder.getView(R.id.ivThumb);
            if (!TextUtils.isEmpty(video.pic)) {
                RequestCreator load = Picasso.get().load(DefaultConfig.checkReplaceProxy(video.pic));
                load.transform(new RoundTransformation(MD5.string2MD5(video.pic + "position=" + baseViewHolder.getLayoutPosition())).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView);
                return;
            }
            imageView.setImageResource(R.drawable.img_loading_placeholder);
            return;
        }
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvYear);
        if (video.year <= 0) {
            textView.setVisibility(8);
        } else {
            textView.setText(String.valueOf(video.year));
            textView.setVisibility(0);
        }
        ((TextView) baseViewHolder.getView(R.id.tvLang)).setVisibility(8);
        ((TextView) baseViewHolder.getView(R.id.tvArea)).setVisibility(8);
        if (TextUtils.isEmpty(video.note)) {
            baseViewHolder.setVisible(R.id.tvNote, false);
        } else {
            baseViewHolder.setVisible(R.id.tvNote, true);
            baseViewHolder.setText(R.id.tvNote, video.note);
        }
        baseViewHolder.setText(R.id.tvName, video.name);
        baseViewHolder.setText(R.id.tvActor, video.actor);
        ImageView imageView2 = (ImageView) baseViewHolder.getView(R.id.ivThumb);
        if (!TextUtils.isEmpty(video.pic)) {
            RequestCreator load2 = Picasso.get().load(DefaultConfig.checkReplaceProxy(video.pic));
            load2.transform(new RoundTransformation(MD5.string2MD5(video.pic + "position=" + baseViewHolder.getLayoutPosition())).centerCorp(true).override(AutoSizeUtils.mm2px(this.mContext, 300.0f), AutoSizeUtils.mm2px(this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(imageView2);
            return;
        }
        imageView2.setImageResource(R.drawable.img_loading_placeholder);
    }
}
