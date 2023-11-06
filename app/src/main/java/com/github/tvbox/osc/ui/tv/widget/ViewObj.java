package com.github.tvbox.osc.ui.tv.widget;

import android.view.View;
import android.view.ViewGroup;

public class ViewObj {
    private final ViewGroup.MarginLayoutParams params;
    private final View view;

    public ViewObj(View view2, ViewGroup.MarginLayoutParams marginLayoutParams) {
        this.view = view2;
        this.params = marginLayoutParams;
    }

    public void setMarginLeft(int i) {
        this.params.leftMargin = i;
        this.view.setLayoutParams(this.params);
    }

    public void setMarginTop(int i) {
        this.params.topMargin = i;
        this.view.setLayoutParams(this.params);
    }

    public void setMarginRight(int i) {
        this.params.rightMargin = i;
        this.view.setLayoutParams(this.params);
    }

    public void setMarginBottom(int i) {
        this.params.bottomMargin = i;
        this.view.setLayoutParams(this.params);
    }

    public void setWidth(int i) {
        this.params.width = i;
        this.view.setLayoutParams(this.params);
    }

    public void setHeight(int i) {
        this.params.height = i;
        this.view.setLayoutParams(this.params);
    }
}
