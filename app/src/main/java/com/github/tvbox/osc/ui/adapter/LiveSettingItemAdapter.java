package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveSettingItem;
import java.util.ArrayList;

public class LiveSettingItemAdapter extends BaseQuickAdapter<LiveSettingItem, BaseViewHolder> {
    private int focusedItemIndex = -1;

    public LiveSettingItemAdapter() {
        super(R.layout.item_live_setting, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, LiveSettingItem liveSettingItem) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.tvSettingItemName);
        textView.setText(liveSettingItem.getItemName());
        int itemIndex = liveSettingItem.getItemIndex();
        if (!liveSettingItem.isItemSelected() || itemIndex == this.focusedItemIndex) {
            textView.setTextColor(-1);
        } else {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        }
    }

    public void selectItem(int i, boolean z, boolean z2) {
        int selectedItemIndex;
        if (z2 && (selectedItemIndex = getSelectedItemIndex()) != -1) {
            ((LiveSettingItem) getData().get(selectedItemIndex)).setItemSelected(false);
            notifyItemChanged(selectedItemIndex);
        }
        if (i != -1) {
            ((LiveSettingItem) getData().get(i)).setItemSelected(z);
            notifyItemChanged(i);
        }
    }

    public void setFocusedItemIndex(int i) {
        int i2 = this.focusedItemIndex;
        this.focusedItemIndex = i;
        if (i2 != -1) {
            notifyItemChanged(i2);
        }
        int i3 = this.focusedItemIndex;
        if (i3 != -1) {
            notifyItemChanged(i3);
        }
    }

    public int getSelectedItemIndex() {
        for (LiveSettingItem liveSettingItem : getData()) {
            if (liveSettingItem.isItemSelected()) {
                return liveSettingItem.getItemIndex();
            }
        }
        return -1;
    }
}
