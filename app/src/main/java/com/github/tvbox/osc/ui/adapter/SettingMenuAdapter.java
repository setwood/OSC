package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import java.util.ArrayList;

public class SettingMenuAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SettingMenuAdapter() {
        super(R.layout.item_setting_menu, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, String str) {
        baseViewHolder.setText(R.id.tvName, str);
        baseViewHolder.addOnClickListener(2131296992);
    }
}
