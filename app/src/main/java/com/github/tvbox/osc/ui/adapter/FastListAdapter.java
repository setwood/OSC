package com.github.tvbox.osc.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import java.util.ArrayList;

public class FastListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    View focusView;
    public long lostFocusTimestamp = 0;
    public int setp = 0;

    public FastListAdapter() {
        super(R.layout.item_search_word_hot, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, String str) {
        baseViewHolder.setText(R.id.tvSearchWord, str);
    }

    public void onLostFocus(View view) {
        if (this.lostFocusTimestamp == 0) {
            this.lostFocusTimestamp = System.currentTimeMillis();
        }
    }

    public int onSetFocus(View view) {
        if (System.currentTimeMillis() - this.lostFocusTimestamp > 200) {
            this.setp = 0;
        }
        int indexOfChild = ((ViewGroup) view.getParent()).indexOfChild(view);
        View view2 = this.focusView;
        int i = 1;
        if (view2 != null) {
            int indexOfChild2 = ((ViewGroup) view2.getParent()).indexOfChild(this.focusView);
            if (Math.abs(indexOfChild - indexOfChild2) > this.setp) {
                this.setp = 0;
                if (indexOfChild > indexOfChild2) {
                    i = -1;
                }
                ((ViewGroup) this.focusView.getParent()).getChildAt(indexOfChild + i).requestFocus();
                return -1;
            }
        }
        this.lostFocusTimestamp = 0;
        this.focusView = view;
        this.setp = 1;
        return 1;
    }

    public void reset() {
        this.lostFocusTimestamp = 0;
        this.setp = 0;
        this.focusView = null;
    }
}
