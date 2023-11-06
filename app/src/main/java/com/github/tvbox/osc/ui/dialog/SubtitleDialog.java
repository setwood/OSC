package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.SubtitleHelper;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;

public class SubtitleDialog extends BaseDialog {
    private LocalFileChooserListener mLocalFileChooserListener;
    private SearchSubtitleListener mSearchSubtitleListener;
    private SubtitleViewListener mSubtitleViewListener;
    public TextView selectInternal;
    public TextView selectLocal;
    public TextView selectRemote;
    private TextView subtitleOption;
    private ImageView subtitleSizeMinus;
    private ImageView subtitleSizePlus;
    private TextView subtitleSizeText;
    private TextView subtitleStyleOne;
    private TextView subtitleStyleTwo;
    private ImageView subtitleTimeMinus;
    private ImageView subtitleTimePlus;
    private TextView subtitleTimeText;

    public interface LocalFileChooserListener {
        void openLocalFileChooserDialog();
    }

    public interface SearchSubtitleListener {
        void openSearchSubtitleDialog();
    }

    public interface SubtitleViewListener {
        void selectInternalSubtitle();

        void setSubtitleDelay(int i);

        void setTextSize(int i);

        void setTextStyle(int i);
    }

    public SubtitleDialog(Context context) {
        super(context);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
        setContentView(R.layout.dialog_subtitle);
        initView(context);
    }

    private void initView(Context context) {
        this.subtitleOption = (TextView) findViewById(2131296930);
        this.selectInternal = (TextView) findViewById(R.id.selectInternal);
        this.selectLocal = (TextView) findViewById(R.id.selectLocal);
        this.selectRemote = (TextView) findViewById(R.id.selectRemote);
        this.subtitleSizeMinus = (ImageView) findViewById(R.id.subtitleSizeMinus);
        this.subtitleSizeText = (TextView) findViewById(R.id.subtitleSizeText);
        this.subtitleSizePlus = (ImageView) findViewById(R.id.subtitleSizePlus);
        this.subtitleTimeMinus = (ImageView) findViewById(R.id.subtitleTimeMinus);
        this.subtitleTimeText = (TextView) findViewById(R.id.subtitleTimeText);
        this.subtitleTimePlus = (ImageView) findViewById(R.id.subtitleTimePlus);
        this.subtitleStyleOne = (TextView) findViewById(R.id.subtitleStyleOne);
        this.subtitleStyleTwo = (TextView) findViewById(R.id.subtitleStyleTwo);
        this.subtitleOption.setText(HomeActivity.getRes().getString(R.string.vod_sub_option));
        this.selectInternal.setText(HomeActivity.getRes().getString(R.string.vod_sub_int));
        this.selectLocal.setText(HomeActivity.getRes().getString(R.string.vod_sub_ext));
        this.selectRemote.setText(HomeActivity.getRes().getString(R.string.vod_sub_remote));
        this.subtitleSizeText.setText(HomeActivity.getRes().getString(R.string.vod_sub_size));
        this.subtitleTimeText.setText(HomeActivity.getRes().getString(R.string.vod_sub_delay));
        this.selectInternal.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass1 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SubtitleDialog.this.dismiss();
                SubtitleDialog.this.mSubtitleViewListener.selectInternalSubtitle();
            }
        });
        this.selectLocal.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass2 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SubtitleDialog.this.dismiss();
                SubtitleDialog.this.mLocalFileChooserListener.openLocalFileChooserDialog();
            }
        });
        this.selectRemote.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SubtitleDialog.this.dismiss();
                SubtitleDialog.this.mSearchSubtitleListener.openSearchSubtitleDialog();
            }
        });
        this.subtitleSizeText.setText(Integer.toString(SubtitleHelper.getTextSize(getOwnerActivity())));
        this.subtitleSizeMinus.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass4 */

            public void onClick(View view) {
                int parseInt = Integer.parseInt(SubtitleDialog.this.subtitleSizeText.getText().toString()) - 2;
                if (parseInt <= 10) {
                    parseInt = 10;
                }
                SubtitleDialog.this.subtitleSizeText.setText(Integer.toString(parseInt));
                SubtitleHelper.setTextSize(parseInt);
                SubtitleDialog.this.mSubtitleViewListener.setTextSize(parseInt);
            }
        });
        this.subtitleSizePlus.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass5 */

            public void onClick(View view) {
                int parseInt = Integer.parseInt(SubtitleDialog.this.subtitleSizeText.getText().toString()) + 2;
                if (parseInt >= 60) {
                    parseInt = 60;
                }
                SubtitleDialog.this.subtitleSizeText.setText(Integer.toString(parseInt));
                SubtitleHelper.setTextSize(parseInt);
                SubtitleDialog.this.mSubtitleViewListener.setTextSize(parseInt);
            }
        });
        int timeDelay = SubtitleHelper.getTimeDelay();
        this.subtitleTimeText.setText(timeDelay != 0 ? Double.toString((double) (timeDelay / 1000)) : SessionDescription.SUPPORTED_SDP_VERSION);
        this.subtitleTimeMinus.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass6 */

            public void onClick(View view) {
                String str;
                FastClickCheckUtil.check(view);
                double parseFloat = (double) Float.parseFloat(SubtitleDialog.this.subtitleTimeText.getText().toString());
                Double.isNaN(parseFloat);
                double d = parseFloat - 8.0d;
                if (d == 0.0d) {
                    str = SessionDescription.SUPPORTED_SDP_VERSION;
                } else {
                    str = Double.toString(d);
                }
                SubtitleDialog.this.subtitleTimeText.setText(str);
                SubtitleHelper.setTimeDelay((int) (d * 1000.0d));
                SubtitleDialog.this.mSubtitleViewListener.setSubtitleDelay((int) -500.0d);
            }
        });
        this.subtitleTimePlus.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass7 */

            public void onClick(View view) {
                String str;
                FastClickCheckUtil.check(view);
                double parseFloat = (double) Float.parseFloat(SubtitleDialog.this.subtitleTimeText.getText().toString());
                Double.isNaN(parseFloat);
                double d = parseFloat + 0.5d;
                if (d == 0.0d) {
                    str = SessionDescription.SUPPORTED_SDP_VERSION;
                } else {
                    str = Double.toString(d);
                }
                SubtitleDialog.this.subtitleTimeText.setText(str);
                SubtitleHelper.setTimeDelay((int) (d * 1000.0d));
                SubtitleDialog.this.mSubtitleViewListener.setSubtitleDelay((int) 500.0d);
            }
        });
        this.subtitleStyleOne.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass8 */

            public void onClick(View view) {
                SubtitleDialog.this.dismiss();
                SubtitleDialog.this.mSubtitleViewListener.setTextStyle(0);
                Toast.makeText(SubtitleDialog.this.getContext(), "设置样式成功", 0).show();
            }
        });
        this.subtitleStyleTwo.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SubtitleDialog.AnonymousClass9 */

            public void onClick(View view) {
                SubtitleDialog.this.dismiss();
                SubtitleDialog.this.mSubtitleViewListener.setTextStyle(1);
                Toast.makeText(SubtitleDialog.this.getContext(), "设置样式成功", 0).show();
            }
        });
    }

    public void setLocalFileChooserListener(LocalFileChooserListener localFileChooserListener) {
        this.mLocalFileChooserListener = localFileChooserListener;
    }

    public void setSearchSubtitleListener(SearchSubtitleListener searchSubtitleListener) {
        this.mSearchSubtitleListener = searchSubtitleListener;
    }

    public void setSubtitleViewListener(SubtitleViewListener subtitleViewListener) {
        this.mSubtitleViewListener = subtitleViewListener;
    }
}
