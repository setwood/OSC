package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ListView;
import com.github.tvbox.osc.ui.activity.LivePlayActivity;

public class ChannelListView extends ListView {
    DataChangedListener dataChangedListener;
    public int pos = LivePlayActivity.currentChannelGroupIndex;
    private int y;

    public interface DataChangedListener {
        void onSuccess();
    }

    public ChannelListView(Context context) {
        super(context);
    }

    public ChannelListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ChannelListView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setSelect(int i, int i2) {
        super.setSelection(i);
        this.pos = i;
        this.y = i2;
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        if (z) {
            setSelectionFromTop(this.pos, this.y);
        }
    }

    /* access modifiers changed from: protected */
    public void handleDataChanged() {
        super.handleDataChanged();
        DataChangedListener dataChangedListener2 = this.dataChangedListener;
        if (dataChangedListener2 != null) {
            dataChangedListener2.onSuccess();
        }
    }

    public void setDataChangedListener(DataChangedListener dataChangedListener2) {
        this.dataChangedListener = dataChangedListener2;
    }
}
