package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveChannelItem;
import java.util.ArrayList;

public class LiveChannelItemAdapter extends BaseQuickAdapter<LiveChannelItem, BaseViewHolder> {
    private int focusedChannelIndex = -1;
    private int selectedChannelIndex = -1;

    public LiveChannelItemAdapter() {
        super(R.layout.item_live_channel, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, LiveChannelItem liveChannelItem) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvChannelNum);
        TextView textView2 = (TextView) baseViewHolder.getView(R.id.tvChannelName);
        textView.setText(String.format("%s", Integer.valueOf(liveChannelItem.getChannelNum())));
        textView2.setText(liveChannelItem.getChannelName());
        int channelIndex = liveChannelItem.getChannelIndex();
        if (channelIndex != this.selectedChannelIndex || channelIndex == this.focusedChannelIndex) {
            textView.setTextColor(-1);
            textView2.setTextColor(-1);
            return;
        }
        textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        textView2.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
    }

    public void setSelectedChannelIndex(int i) {
        int i2 = this.selectedChannelIndex;
        if (i != i2) {
            this.selectedChannelIndex = i;
            if (i2 != -1) {
                notifyItemChanged(i2);
            }
            int i3 = this.selectedChannelIndex;
            if (i3 != -1) {
                notifyItemChanged(i3);
            }
        }
    }

    public void setFocusedChannelIndex(int i) {
        int i2 = this.focusedChannelIndex;
        this.focusedChannelIndex = i;
        if (i2 != -1) {
            notifyItemChanged(i2);
        }
        int i3 = this.focusedChannelIndex;
        if (i3 != -1) {
            notifyItemChanged(i3);
            return;
        }
        int i4 = this.selectedChannelIndex;
        if (i4 != -1) {
            notifyItemChanged(i4);
        }
    }
}
