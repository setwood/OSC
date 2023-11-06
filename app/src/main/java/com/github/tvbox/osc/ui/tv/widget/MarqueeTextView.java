package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {
    public boolean isFocused() {
        return true;
    }

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setSelected(true);
        setSingleLine(true);
        setMarqueeRepeatLimit(-1);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
    }
}
