package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveEpgDate;
import java.util.ArrayList;

public class LiveEpgDateAdapter extends BaseQuickAdapter<LiveEpgDate, BaseViewHolder> {
    private int focusedIndex = -1;
    private int selectedIndex = -1;

    public LiveEpgDateAdapter() {
        super(R.layout.item_live_channel_group, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, LiveEpgDate liveEpgDate) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvChannelGroupName);
        textView.setText(liveEpgDate.getDatePresented());
        textView.setBackgroundColor(0);
        if (liveEpgDate.getIndex() == this.selectedIndex && liveEpgDate.getIndex() != this.focusedIndex) {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        } else if (liveEpgDate.getIndex() == this.selectedIndex && liveEpgDate.getIndex() == this.focusedIndex) {
            textView.setTextColor(this.mContext.getResources().getColor(2131034178));
        } else {
            textView.setTextColor(this.mContext.getResources().getColor(2131034178));
        }
    }

    public void setSelectedIndex(int i) {
        int i2 = this.selectedIndex;
        if (i != i2) {
            this.selectedIndex = i;
            if (i2 != -1) {
                notifyItemChanged(i2);
            }
            int i3 = this.selectedIndex;
            if (i3 != -1) {
                notifyItemChanged(i3);
            }
        }
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setFocusedIndex(int i) {
        int i2 = this.focusedIndex;
        this.focusedIndex = i;
        if (i2 != -1) {
            notifyItemChanged(i2);
        }
        int i3 = this.focusedIndex;
        if (i3 != -1) {
            notifyItemChanged(i3);
            return;
        }
        int i4 = this.selectedIndex;
        if (i4 != -1) {
            notifyItemChanged(i4);
        }
    }
}
