package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveSettingGroup;
import java.util.ArrayList;

public class LiveSettingGroupAdapter extends BaseQuickAdapter<LiveSettingGroup, BaseViewHolder> {
    private int focusedGroupIndex = -1;
    private int selectedGroupIndex = -1;

    public LiveSettingGroupAdapter() {
        super(R.layout.item_live_setting_group, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, LiveSettingGroup liveSettingGroup) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvSettingGroupName);
        textView.setText(liveSettingGroup.getGroupName());
        int groupIndex = liveSettingGroup.getGroupIndex();
        if (groupIndex != this.selectedGroupIndex || groupIndex == this.focusedGroupIndex) {
            textView.setTextColor(-1);
        } else {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        }
    }

    public void setSelectedGroupIndex(int i) {
        int i2 = this.selectedGroupIndex;
        this.selectedGroupIndex = i;
        if (i2 != -1) {
            notifyItemChanged(i2);
        }
        int i3 = this.selectedGroupIndex;
        if (i3 != -1) {
            notifyItemChanged(i3);
        }
    }

    public int getSelectedGroupIndex() {
        return this.selectedGroupIndex;
    }

    public void setFocusedGroupIndex(int i) {
        this.focusedGroupIndex = i;
        if (i != -1) {
            notifyItemChanged(i);
            return;
        }
        int i2 = this.selectedGroupIndex;
        if (i2 != -1) {
            notifyItemChanged(i2);
        }
    }
}
