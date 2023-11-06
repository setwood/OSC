package com.github.tvbox.osc.ui.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Epginfo;
import com.github.tvbox.osc.ui.tv.widget.AudioWaveView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LiveEpgAdapter extends BaseQuickAdapter<Epginfo, BaseViewHolder> {
    private boolean ShiyiSelection = false;
    private int focusedEpgIndex = -1;
    private int selectedEpgIndex = -1;
    private String shiyiDate = null;
    private boolean source_include_back = false;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");

    public LiveEpgAdapter() {
        super(R.layout.item_epglist, new ArrayList());
    }

    public void CanBack(Boolean bool) {
        this.source_include_back = bool.booleanValue();
    }

    /* access modifiers changed from: protected */
    public void convert(BaseViewHolder baseViewHolder, Epginfo epginfo) {
        Date date = new Date();
        TextView textView = (TextView) baseViewHolder.getView(R.id.tv_epg_name);
        TextView textView2 = (TextView) baseViewHolder.getView(R.id.tv_epg_time);
        TextView textView3 = (TextView) baseViewHolder.getView(R.id.shiyi);
        AudioWaveView audioWaveView = (AudioWaveView) baseViewHolder.getView(R.id.wqddg_AudioWaveView);
        audioWaveView.setVisibility(8);
        if (epginfo.index != this.selectedEpgIndex || epginfo.index == this.focusedEpgIndex || (!epginfo.currentEpgDate.equals(this.shiyiDate) && !epginfo.currentEpgDate.equals(this.timeFormat.format(date)))) {
            textView.setTextColor(this.mContext.getResources().getColor(2131034178));
            textView2.setTextColor(this.mContext.getResources().getColor(2131034178));
        } else {
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
            textView2.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
        }
        if (date.compareTo(epginfo.startdateTime) >= 0 && date.compareTo(epginfo.enddateTime) <= 0) {
            textView3.setVisibility(0);
            textView3.setBackgroundColor(this.mContext.getResources().getColor(R.color.color_32364E));
            textView3.setTextColor(this.mContext.getResources().getColor(2131034178));
            textView3.setText("直播");
        } else if (date.compareTo(epginfo.enddateTime) > 0 && this.source_include_back) {
            textView3.setVisibility(0);
            textView3.setBackgroundColor(this.mContext.getResources().getColor(R.color.color_32364E_40));
            textView3.setTextColor(this.mContext.getResources().getColor(2131034178));
            textView3.setText("回看");
        } else if (date.compareTo(epginfo.startdateTime) >= 0 || !this.source_include_back) {
            textView3.setVisibility(8);
        } else {
            textView3.setVisibility(0);
            textView3.setBackgroundColor(this.mContext.getResources().getColor(R.color.color_3D3D3D));
            textView3.setTextColor(this.mContext.getResources().getColor(2131034178));
            textView3.setText("预约");
        }
        textView.setText(epginfo.title);
        textView2.setText(epginfo.start + " - " + epginfo.end);
        textView.setTextColor(this.mContext.getResources().getColor(2131034178));
        textView2.setTextColor(this.mContext.getResources().getColor(2131034178));
        if (!this.ShiyiSelection) {
            if (date.compareTo(epginfo.startdateTime) < 0 || date.compareTo(epginfo.enddateTime) > 0) {
                audioWaveView.setVisibility(8);
                return;
            }
            audioWaveView.setVisibility(0);
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
            textView2.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
            textView.setFreezesText(true);
            textView2.setFreezesText(true);
            textView3.setText("直播中");
        } else if (epginfo.index != this.selectedEpgIndex || !epginfo.currentEpgDate.equals(this.shiyiDate)) {
            audioWaveView.setVisibility(8);
        } else {
            audioWaveView.setVisibility(0);
            textView.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
            textView2.setTextColor(this.mContext.getResources().getColor(R.color.color_theme));
            textView.setFreezesText(true);
            textView2.setFreezesText(true);
            textView3.setText("回看中");
            if (date.compareTo(epginfo.startdateTime) >= 0 && date.compareTo(epginfo.enddateTime) <= 0) {
                textView3.setText("直播中");
            }
            audioWaveView.setVisibility(0);
        }
    }

    public void setShiyiSelection(int i, boolean z, String str) {
        this.selectedEpgIndex = i;
        if (!z) {
            str = null;
        }
        this.shiyiDate = str;
        this.ShiyiSelection = z;
        notifyItemChanged(i);
    }

    public int getSelectedIndex() {
        return this.selectedEpgIndex;
    }

    public void setSelectedEpgIndex(int i) {
        if (i != this.selectedEpgIndex) {
            this.selectedEpgIndex = i;
            if (i != -1) {
                notifyItemChanged(i);
            }
        }
    }

    public int getFocusedEpgIndex() {
        return this.focusedEpgIndex;
    }

    public void setFocusedEpgIndex(int i) {
        this.focusedEpgIndex = i;
        if (i != -1) {
            notifyItemChanged(i);
        }
    }
}
