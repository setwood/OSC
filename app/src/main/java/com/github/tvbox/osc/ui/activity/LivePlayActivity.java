package com.github.tvbox.osc.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.Epginfo;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.bean.LiveEpgDate;
import com.github.tvbox.osc.bean.LivePlayerManager;
import com.github.tvbox.osc.bean.LiveSettingGroup;
import com.github.tvbox.osc.bean.LiveSettingItem;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.player.controller.LiveController;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.adapter.LiveChannelGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveChannelItemAdapter;
import com.github.tvbox.osc.ui.adapter.LiveEpgAdapter;
import com.github.tvbox.osc.ui.adapter.LiveEpgDateAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingItemAdapter;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.LivePasswordDialog;
import com.github.tvbox.osc.util.EpgUtil;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.live.TxtSubscribe;
import com.github.tvbox.osc.util.urlhttp.CallBackUtil;
import com.github.tvbox.osc.util.urlhttp.UrlHttpUtil;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class LivePlayActivity extends BaseActivity {
    private static LiveChannelItem channel_Name;
    public static int currentChannelGroupIndex;
    private static final Hashtable hsEpg = new Hashtable();
    private static String shiyi_time;
    boolean PiPON = ((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, false)).booleanValue();
    private final ArrayList<Integer> channelGroupPasswordConfirmed = new ArrayList<>();
    private LiveController controller;
    private int currentLiveChangeSourceTimes = 0;
    private int currentLiveChannelIndex = -1;
    private LiveChannelItem currentLiveChannelItem = null;
    private LiveEpgDateAdapter epgDateAdapter;
    private LiveEpgAdapter epgListAdapter;
    public String epgStringAddress = "";
    private List<Epginfo> epgdata = new ArrayList();
    private boolean isSHIYI = false;
    boolean isVOD = false;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private final List<LiveChannelGroup> liveChannelGroupList = new ArrayList();
    private LiveChannelItemAdapter liveChannelItemAdapter;
    private final LivePlayerManager livePlayerManager = new LivePlayerManager();
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList();
    private LiveSettingItemAdapter liveSettingItemAdapter;
    LinearLayout llSeekBar;
    LinearLayout mBack;
    private TvRecyclerView mChannelGridView;
    private final Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass14 */

        public void run() {
            LivePlayActivity.access$2608(LivePlayActivity.this);
            if (LivePlayActivity.this.currentLiveChannelItem.getSourceNum() == LivePlayActivity.this.currentLiveChangeSourceTimes) {
                LivePlayActivity.this.currentLiveChangeSourceTimes = 0;
                Integer[] nextChannel = LivePlayActivity.this.getNextChannel(((Boolean) Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)).booleanValue() ? -1 : 1);
                LivePlayActivity.this.playChannel(nextChannel[0].intValue(), nextChannel[1].intValue(), false);
                return;
            }
            LivePlayActivity.this.playNextSource();
        }
    };
    TextView mCurrentTime;
    private LinearLayout mDivLeft;
    private LinearLayout mDivRight;
    private TvRecyclerView mEpgDateGridView;
    private TvRecyclerView mEpgInfoGridView;
    private long mExitTime = 0;
    private final Runnable mFocusAndShowSettingGroup = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass11 */

        public void run() {
            if (LivePlayActivity.this.mSettingGroupView.isScrolling() || LivePlayActivity.this.mSettingItemView.isScrolling() || LivePlayActivity.this.mSettingGroupView.isComputingLayout() || LivePlayActivity.this.mSettingItemView.isComputingLayout()) {
                LivePlayActivity.this.mHandler.postDelayed(this, 100);
                return;
            }
            RecyclerView.ViewHolder findViewHolderForAdapterPosition = LivePlayActivity.this.mSettingGroupView.findViewHolderForAdapterPosition(0);
            if (findViewHolderForAdapterPosition != null) {
                findViewHolderForAdapterPosition.itemView.requestFocus();
            }
            LivePlayActivity.this.tvRightSettingLayout.setVisibility(0);
            LivePlayActivity.this.tvRightSettingLayout.setAlpha(0.0f);
            LivePlayActivity.this.tvRightSettingLayout.setTranslationX((float) (LivePlayActivity.this.tvRightSettingLayout.getWidth() / 2));
            LivePlayActivity.this.tvRightSettingLayout.animate().translationX(0.0f).alpha(1.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).setListener(null);
            LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideSettingLayoutRun);
            LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideSettingLayoutRun, 6000);
            LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mUpdateLayout, 255);
        }
    };
    private final Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass4 */

        public void run() {
            if (LivePlayActivity.this.mGroupGridView.isScrolling() || LivePlayActivity.this.mChannelGridView.isScrolling() || LivePlayActivity.this.mGroupGridView.isComputingLayout() || LivePlayActivity.this.mChannelGridView.isComputingLayout()) {
                LivePlayActivity.this.mHandler.postDelayed(this, 100);
                return;
            }
            LivePlayActivity.this.liveChannelGroupAdapter.setSelectedGroupIndex(LivePlayActivity.currentChannelGroupIndex);
            LivePlayActivity.this.liveChannelItemAdapter.setSelectedChannelIndex(LivePlayActivity.this.currentLiveChannelIndex);
            RecyclerView.ViewHolder findViewHolderForAdapterPosition = LivePlayActivity.this.mChannelGridView.findViewHolderForAdapterPosition(LivePlayActivity.this.currentLiveChannelIndex);
            if (findViewHolderForAdapterPosition != null) {
                findViewHolderForAdapterPosition.itemView.requestFocus();
            }
            LivePlayActivity.this.tvLeftChannelListLayout.setVisibility(0);
            LivePlayActivity.this.tvLeftChannelListLayout.setAlpha(0.0f);
            LivePlayActivity.this.tvLeftChannelListLayout.setTranslationX((float) ((-LivePlayActivity.this.tvLeftChannelListLayout.getWidth()) / 2));
            LivePlayActivity.this.tvLeftChannelListLayout.animate().translationX(0.0f).alpha(1.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).setListener(null);
            LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
            LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
            LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mUpdateLayout, 255);
        }
    };
    private LinearLayout mGroupEPG;
    private TvRecyclerView mGroupGridView;
    private final Handler mHandler = new Handler();
    private final Runnable mHideChannelInfoRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass7 */

        public void run() {
            LivePlayActivity.this.mBack.setVisibility(4);
            if (LivePlayActivity.this.tvBottomLayout.getVisibility() == 0) {
                LivePlayActivity.this.tvBottomLayout.animate().alpha(0.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).translationY((float) (LivePlayActivity.this.tvBottomLayout.getHeight() / 2)).setListener(new AnimatorListenerAdapter() {
                    /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass7.AnonymousClass1 */

                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        LivePlayActivity.this.tvBottomLayout.setVisibility(4);
                        LivePlayActivity.this.tvBottomLayout.clearAnimation();
                    }
                });
            }
        }
    };
    private final Runnable mHideChannelListRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass6 */

        public void run() {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) LivePlayActivity.this.tvLeftChannelListLayout.getLayoutParams();
            if (LivePlayActivity.this.tvLeftChannelListLayout.getVisibility() == 0) {
                LivePlayActivity.this.tvLeftChannelListLayout.animate().translationX((float) ((-LivePlayActivity.this.tvLeftChannelListLayout.getWidth()) / 2)).alpha(0.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass6.AnonymousClass1 */

                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        LivePlayActivity.this.tvLeftChannelListLayout.setVisibility(4);
                        LivePlayActivity.this.tvLeftChannelListLayout.clearAnimation();
                    }
                });
            }
        }
    };
    private final Runnable mHideSettingLayoutRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass12 */

        public void run() {
            if (LivePlayActivity.this.tvRightSettingLayout.getVisibility() == 0) {
                LivePlayActivity.this.tvRightSettingLayout.animate().translationX((float) (LivePlayActivity.this.tvRightSettingLayout.getWidth() / 2)).alpha(0.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass12.AnonymousClass1 */

                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        LivePlayActivity.this.tvRightSettingLayout.setVisibility(4);
                        LivePlayActivity.this.tvRightSettingLayout.clearAnimation();
                        LivePlayActivity.this.liveSettingGroupAdapter.setSelectedGroupIndex(-1);
                    }
                });
            }
        }
    };
    boolean mIsDragging;
    SeekBar mSeekBar;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    TextView mTotalTime;
    private final Runnable mUpdateLayout = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass5 */

        public void run() {
            LivePlayActivity.this.tvLeftChannelListLayout.requestLayout();
            LivePlayActivity.this.tvRightSettingLayout.requestLayout();
        }
    };
    private final Runnable mUpdateNetSpeedRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass36 */

        public void run() {
            if (LivePlayActivity.this.mVideoView != null) {
                TextView textView = LivePlayActivity.this.tvNetSpeed;
                double tcpSpeed = (double) ((float) LivePlayActivity.this.mVideoView.getTcpSpeed());
                Double.isNaN(tcpSpeed);
                textView.setText(String.format("%.2fMB/s", Double.valueOf((tcpSpeed / 1024.0d) / 1024.0d)));
                LivePlayActivity.this.mHandler.postDelayed(this, 1000);
            }
        }
    };
    private final Runnable mUpdateTimeRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass35 */

        public void run() {
            LivePlayActivity.this.tvTime.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            if ((LivePlayActivity.this.mVideoView != null) && (true ^ LivePlayActivity.this.mIsDragging)) {
                int currentPosition = (int) LivePlayActivity.this.mVideoView.getCurrentPosition();
                LivePlayActivity.this.mCurrentTime.setText(PlayerUtils.stringForTimeVod(currentPosition));
                LivePlayActivity.this.mSeekBar.setProgress(currentPosition);
            }
            LivePlayActivity.this.mHandler.postDelayed(this, 1000);
        }
    };
    private VideoView mVideoView;
    private boolean onStopCalled;
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
    private LinearLayout tvBottomLayout;
    private LinearLayout tvLeftChannelListLayout;
    private TextView tvNetSpeed;
    private LinearLayout tvRightSettingLayout;
    private TextView tvTime;
    private TextView tv_channelname;
    private TextView tv_channelnum;
    private TextView tv_curr_name;
    private TextView tv_curr_time;
    private ImageView tv_logo;
    private TextView tv_next_name;
    private TextView tv_next_time;
    private TextView tv_size;
    private TextView tv_source;
    private TextView tv_sys_time;
    private final Runnable tv_sys_timeRunnable = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass9 */

        public void run() {
            Date date = new Date();
            LivePlayActivity.this.tv_sys_time.setText(new SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(date));
            LivePlayActivity.this.mHandler.postDelayed(this, 1000);
        }
    };

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    static /* synthetic */ int access$2608(LivePlayActivity livePlayActivity) {
        int i = livePlayActivity.currentLiveChangeSourceTimes;
        livePlayActivity.currentLiveChangeSourceTimes = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        hideSystemUI(false);
        String str = (String) Hawk.get(HawkConfig.EPG_URL, "");
        this.epgStringAddress = str;
        if (StringUtils.isBlank(str)) {
            this.epgStringAddress = "http://epg.51zmt.top:8000/api/diyp/";
        }
        EventBus.getDefault().register(this);
        setLoadSir(findViewById(R.id.live_root));
        this.mVideoView = (VideoView) findViewById(R.id.mVideoView);
        this.tv_size = (TextView) findViewById(R.id.tv_size);
        this.tv_source = (TextView) findViewById(R.id.tv_source);
        this.tv_sys_time = (TextView) findViewById(R.id.tv_sys_time);
        this.llSeekBar = (LinearLayout) findViewById(R.id.ll_seekbar);
        this.mCurrentTime = (TextView) findViewById(2131296412);
        this.mSeekBar = (SeekBar) findViewById(2131296810);
        this.mTotalTime = (TextView) findViewById(2131296941);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tvBackButton);
        this.mBack = linearLayout;
        linearLayout.setVisibility(4);
        LinearLayout linearLayout2 = (LinearLayout) findViewById(R.id.tvBottomLayout);
        this.tvBottomLayout = linearLayout2;
        linearLayout2.setVisibility(4);
        this.tv_channelname = (TextView) findViewById(R.id.tv_channel_name);
        this.tv_channelnum = (TextView) findViewById(R.id.tv_channel_number);
        this.tv_logo = (ImageView) findViewById(R.id.tv_logo);
        this.tv_curr_time = (TextView) findViewById(R.id.tv_current_program_time);
        this.tv_curr_name = (TextView) findViewById(R.id.tv_current_program_name);
        this.tv_next_time = (TextView) findViewById(R.id.tv_next_program_time);
        this.tv_next_name = (TextView) findViewById(R.id.tv_next_program_name);
        this.mGroupEPG = (LinearLayout) findViewById(R.id.mGroupEPG);
        this.mDivRight = (LinearLayout) findViewById(R.id.mDivRight);
        this.mDivLeft = (LinearLayout) findViewById(R.id.mDivLeft);
        this.mEpgDateGridView = (TvRecyclerView) findViewById(R.id.mEpgDateGridView);
        this.mEpgInfoGridView = (TvRecyclerView) findViewById(R.id.mEpgInfoGridView);
        this.tvLeftChannelListLayout = (LinearLayout) findViewById(R.id.tvLeftChannelListLayout);
        this.mGroupGridView = (TvRecyclerView) findViewById(R.id.mGroupGridView);
        this.mChannelGridView = (TvRecyclerView) findViewById(R.id.mChannelGridView);
        this.tvRightSettingLayout = (LinearLayout) findViewById(R.id.tvRightSettingLayout);
        this.mSettingGroupView = (TvRecyclerView) findViewById(R.id.mSettingGroupView);
        this.mSettingItemView = (TvRecyclerView) findViewById(R.id.mSettingItemView);
        this.tvTime = (TextView) findViewById(R.id.tvTime);
        this.tvNetSpeed = (TextView) findViewById(R.id.tvNetSpeed);
        initEpgDateView();
        initEpgListView();
        initVideoView();
        initChannelGroupView();
        initLiveChannelView();
        initSettingGroupView();
        initSettingItemView();
        initLiveChannelList();
        initLiveSettingGroupList();
        this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass1 */

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelInfoRun);
                    LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelInfoRun, 6000);
                    long duration = (LivePlayActivity.this.mVideoView.getDuration() * ((long) i)) / ((long) seekBar.getMax());
                    if (LivePlayActivity.this.mCurrentTime != null) {
                        LivePlayActivity.this.mCurrentTime.setText(PlayerUtils.stringForTimeVod((int) duration));
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                LivePlayActivity.this.mIsDragging = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                LivePlayActivity.this.mIsDragging = false;
                LivePlayActivity.this.mVideoView.seekTo((long) ((int) ((LivePlayActivity.this.mVideoView.getDuration() * ((long) seekBar.getProgress())) / ((long) seekBar.getMax()))));
            }
        });
        this.mSeekBar.setOnKeyListener(new View.OnKeyListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass2 */

            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == 0) {
                    if (i == 21 || i == 22) {
                        LivePlayActivity.this.mIsDragging = true;
                    }
                } else if (keyEvent.getAction() == 1) {
                    LivePlayActivity.this.mIsDragging = false;
                    LivePlayActivity.this.mVideoView.seekTo((long) ((int) ((LivePlayActivity.this.mVideoView.getDuration() * ((long) LivePlayActivity.this.mSeekBar.getProgress())) / ((long) LivePlayActivity.this.mSeekBar.getMax()))));
                }
                return false;
            }
        });
        this.mBack.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass3 */

            public void onClick(View view) {
                LivePlayActivity.this.finish();
            }
        });
    }

    public void onUserLeaveHint() {
        if (supportsPiPMode() && this.PiPON) {
            this.mHandler.post(this.mHideChannelListRun);
            this.mHandler.post(this.mHideChannelInfoRun);
            this.mHandler.post(this.mHideSettingLayoutRun);
            enterPictureInPictureMode();
        }
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        if (this.tvLeftChannelListLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelListRun);
            this.mHandler.post(this.mHideChannelListRun);
        } else if (this.tvRightSettingLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideSettingLayoutRun);
            this.mHandler.post(this.mHideSettingLayoutRun);
        } else if (this.tvBottomLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelInfoRun);
            this.mHandler.post(this.mHideChannelInfoRun);
        } else {
            this.mHandler.removeCallbacks(this.mConnectTimeoutChangeSourceRun);
            this.mHandler.removeCallbacks(this.mUpdateNetSpeedRun);
            this.mHandler.removeCallbacks(this.mUpdateTimeRun);
            this.mHandler.removeCallbacks(this.tv_sys_timeRunnable);
            exit();
        }
    }

    private void exit() {
        if (System.currentTimeMillis() - this.mExitTime < SimpleExoPlayer.DEFAULT_DETACH_SURFACE_TIMEOUT_MS) {
            super.onBackPressed();
            return;
        }
        this.mExitTime = System.currentTimeMillis();
        Toast.makeText(this.mContext, getString(R.string.hm_exit_live), 0).show();
    }

    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == 82) {
                showSettingGroup();
            } else if (!isListOrSettingLayoutVisible()) {
                if (!(keyCode == 66 || keyCode == 85)) {
                    switch (keyCode) {
                        case 19:
                            if (!((Boolean) Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)).booleanValue()) {
                                playPrevious();
                                break;
                            } else {
                                playNext();
                                break;
                            }
                        case 20:
                            if (!((Boolean) Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)).booleanValue()) {
                                playNext();
                                break;
                            } else {
                                playPrevious();
                                break;
                            }
                        case 21:
                            if (this.isVOD) {
                                showChannelInfo();
                                break;
                            } else {
                                showSettingGroup();
                                break;
                            }
                        case 22:
                            if (this.isVOD) {
                                showChannelInfo();
                                break;
                            } else {
                                playNextSource();
                                break;
                            }
                    }
                }
                showChannelList();
            }
        } else {
            keyEvent.getAction();
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onResume() {
        super.onResume();
        VideoView videoView = this.mVideoView;
        if (videoView != null) {
            videoView.resume();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onStop() {
        super.onStop();
        this.onStopCalled = true;
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity
    public void onPause() {
        super.onPause();
        if (this.mVideoView == null) {
            return;
        }
        if (!supportsPiPMode()) {
            this.mVideoView.pause();
        } else if (isInPictureInPictureMode()) {
            this.mVideoView.resume();
        } else {
            this.mVideoView.pause();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onPictureInPictureModeChanged(boolean z) {
        super.onPictureInPictureModeChanged(z);
        if (supportsPiPMode() && !isInPictureInPictureMode() && this.onStopCalled) {
            this.mVideoView.release();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        VideoView videoView = this.mVideoView;
        if (videoView != null) {
            videoView.release();
            this.mVideoView = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showChannelList() {
        if (this.tvBottomLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelInfoRun);
            this.mHandler.post(this.mHideChannelInfoRun);
        } else if (this.tvRightSettingLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideSettingLayoutRun);
            this.mHandler.post(this.mHideSettingLayoutRun);
        }
        boolean z = true;
        boolean z2 = this.tvLeftChannelListLayout.getVisibility() == 4;
        if (this.tvRightSettingLayout.getVisibility() != 4) {
            z = false;
        }
        if (z2 && z) {
            this.liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            int i = this.currentLiveChannelIndex;
            if (i > -1) {
                this.mChannelGridView.scrollToPosition(i);
            }
            this.mChannelGridView.setSelection(this.currentLiveChannelIndex);
            this.mGroupGridView.scrollToPosition(currentChannelGroupIndex);
            this.mGroupGridView.setSelection(currentChannelGroupIndex);
            this.mHandler.postDelayed(this.mFocusCurrentChannelAndShowChannelList, 200);
            this.mHandler.post(this.tv_sys_timeRunnable);
            return;
        }
        this.mHandler.removeCallbacks(this.mHideChannelListRun);
        this.mHandler.post(this.mHideChannelListRun);
        this.mHandler.removeCallbacks(this.tv_sys_timeRunnable);
    }

    public void divLoadEpgR(View view) {
        this.mGroupGridView.setVisibility(8);
        this.mEpgInfoGridView.setVisibility(0);
        this.mGroupEPG.setVisibility(0);
        this.mDivLeft.setVisibility(0);
        this.mDivRight.setVisibility(8);
        this.tvLeftChannelListLayout.setVisibility(4);
        showChannelList();
    }

    public void divLoadEpgL(View view) {
        this.mGroupGridView.setVisibility(0);
        this.mEpgInfoGridView.setVisibility(8);
        this.mGroupEPG.setVisibility(8);
        this.mDivLeft.setVisibility(8);
        this.mDivRight.setVisibility(0);
        this.tvLeftChannelListLayout.setVisibility(4);
        showChannelList();
    }

    private void showChannelInfo() {
        if (supportsTouch()) {
            this.mBack.setVisibility(0);
        }
        if (this.tvBottomLayout.getVisibility() == 8 || this.tvBottomLayout.getVisibility() == 4) {
            this.tvBottomLayout.setVisibility(0);
            LinearLayout linearLayout = this.tvBottomLayout;
            linearLayout.setTranslationY((float) (linearLayout.getHeight() / 2));
            this.tvBottomLayout.setAlpha(0.0f);
            this.tvBottomLayout.animate().alpha(1.0f).setDuration(250).setInterpolator(new DecelerateInterpolator()).translationY(0.0f).setListener(null);
        }
        this.mHandler.removeCallbacks(this.mHideChannelInfoRun);
        this.mHandler.postDelayed(this.mHideChannelInfoRun, 6000);
        this.mHandler.postDelayed(this.mUpdateLayout, 255);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showEpg(Date date, ArrayList<Epginfo> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            arrayList.add(new Epginfo(date, "暂无节目信息", date, "00:00", "23:59", 0));
            this.epgdata = arrayList;
            this.epgListAdapter.setNewData(arrayList);
            return;
        }
        this.epgdata = arrayList;
        this.epgListAdapter.CanBack(Boolean.valueOf(this.currentLiveChannelItem.getinclude_back()));
        this.epgListAdapter.setNewData(this.epgdata);
        final int size = this.epgdata.size() - 1;
        while (size >= 0 && new Date().compareTo(this.epgdata.get(size).startdateTime) < 0) {
            size--;
        }
        if (size >= 0 && new Date().compareTo(this.epgdata.get(size).enddateTime) <= 0) {
            this.mEpgInfoGridView.setSelectedPosition(size);
            this.mEpgInfoGridView.setSelection(size);
            this.epgListAdapter.setSelectedEpgIndex(size);
            this.mEpgInfoGridView.post(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass8 */

                public void run() {
                    LivePlayActivity.this.mEpgInfoGridView.smoothScrollToPosition(size);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showBottomEpg() {
        if (!this.isSHIYI && channel_Name.getChannelName() != null) {
            showChannelInfo();
            StringBuilder sb = new StringBuilder();
            sb.append(channel_Name.getChannelName());
            sb.append("_");
            LiveEpgDateAdapter liveEpgDateAdapter = this.epgDateAdapter;
            sb.append(((LiveEpgDate) liveEpgDateAdapter.getItem(liveEpgDateAdapter.getSelectedIndex())).getDatePresented());
            String sb2 = sb.toString();
            Hashtable hashtable = hsEpg;
            if (hashtable.containsKey(sb2)) {
                String[] epgInfo = EpgUtil.getEpgInfo(channel_Name.getChannelName());
                getTvLogo(channel_Name.getChannelName(), epgInfo == null ? null : epgInfo[0]);
                ArrayList arrayList = (ArrayList) hashtable.get(sb2);
                if (arrayList != null && arrayList.size() > 0) {
                    Date date = new Date();
                    int size = arrayList.size() - 1;
                    while (true) {
                        if (size < 0) {
                            break;
                        } else if (date.after(((Epginfo) arrayList.get(size)).startdateTime) && date.before(((Epginfo) arrayList.get(size)).enddateTime)) {
                            this.tv_curr_time.setText(((Epginfo) arrayList.get(size)).start + " - " + ((Epginfo) arrayList.get(size)).end);
                            this.tv_curr_name.setText(((Epginfo) arrayList.get(size)).title);
                            if (size != arrayList.size() - 1) {
                                TextView textView = this.tv_next_time;
                                StringBuilder sb3 = new StringBuilder();
                                int i = size + 1;
                                sb3.append(((Epginfo) arrayList.get(i)).start);
                                sb3.append(" - ");
                                sb3.append(((Epginfo) arrayList.get(i)).end);
                                textView.setText(sb3.toString());
                                this.tv_next_name.setText(((Epginfo) arrayList.get(i)).title);
                            } else {
                                this.tv_next_time.setText("00:00 - 23:59");
                                this.tv_next_name.setText("No Information");
                            }
                        } else {
                            size--;
                        }
                    }
                }
                this.epgListAdapter.CanBack(Boolean.valueOf(this.currentLiveChannelItem.getinclude_back()));
                this.epgListAdapter.setNewData(arrayList);
                return;
            }
            int selectedIndex = this.epgDateAdapter.getSelectedIndex();
            if (selectedIndex < 0) {
                getEpg(new Date());
            } else {
                getEpg(((LiveEpgDate) this.epgDateAdapter.getData().get(selectedIndex)).getDateParamVal());
            }
        }
    }

    private void getTvLogo(String str, String str2) {
        Picasso.get().load(str2).placeholder(R.drawable.img_logo_placeholder).into(this.tv_logo);
    }

    public void getEpg(final Date date) {
        String str;
        String str2;
        final String channelName = channel_Name.getChannelName();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String[] epgInfo = EpgUtil.getEpgInfo(channelName);
        if (epgInfo == null) {
            str = null;
        } else {
            str = epgInfo[0];
        }
        getTvLogo(channelName, str);
        String str3 = (epgInfo == null || epgInfo[1].isEmpty()) ? channelName : epgInfo[1];
        this.epgListAdapter.CanBack(Boolean.valueOf(this.currentLiveChannelItem.getinclude_back()));
        if (!this.epgStringAddress.contains("{name}") || !this.epgStringAddress.contains("{date}")) {
            str2 = this.epgStringAddress + "?ch=" + URLEncoder.encode(str3) + "&date=" + simpleDateFormat.format(date);
        } else {
            str2 = this.epgStringAddress.replace("{name}", URLEncoder.encode(str3)).replace("{date}", simpleDateFormat.format(date));
        }
        UrlHttpUtil.get(str2, new CallBackUtil.CallBackString() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass10 */

            @Override // com.github.tvbox.osc.util.urlhttp.CallBackUtil
            public void onFailure(int i, String str) {
                LivePlayActivity.this.showEpg(date, new ArrayList());
            }

            public void onResponse(String str) {
                JSONArray optJSONArray;
                ArrayList arrayList = new ArrayList();
                try {
                    if (str.contains("epg_data") && (optJSONArray = new JSONObject(str).optJSONArray("epg_data")) != null) {
                        for (int i = 0; i < optJSONArray.length(); i++) {
                            JSONObject jSONObject = optJSONArray.getJSONObject(i);
                            arrayList.add(new Epginfo(date, jSONObject.optString("title"), date, jSONObject.optString(TtmlNode.START), jSONObject.optString(TtmlNode.END), i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LivePlayActivity.this.showEpg(date, arrayList);
                String str2 = channelName + "_" + ((LiveEpgDate) LivePlayActivity.this.epgDateAdapter.getItem(LivePlayActivity.this.epgDateAdapter.getSelectedIndex())).getDatePresented();
                if (!LivePlayActivity.hsEpg.contains(str2)) {
                    LivePlayActivity.hsEpg.put(str2, arrayList);
                }
                LivePlayActivity.this.showBottomEpg();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean playChannel(int i, int i2, boolean z) {
        if (!(i == currentChannelGroupIndex && i2 == this.currentLiveChannelIndex && !z) && (!z || this.currentLiveChannelItem.getSourceNum() != 1)) {
            VideoView videoView = this.mVideoView;
            if (videoView == null) {
                return true;
            }
            videoView.release();
            if (!z) {
                currentChannelGroupIndex = i;
                this.currentLiveChannelIndex = i2;
                LiveChannelItem liveChannelItem = getLiveChannels(i).get(this.currentLiveChannelIndex);
                this.currentLiveChannelItem = liveChannelItem;
                Hawk.put(HawkConfig.LIVE_CHANNEL, liveChannelItem.getChannelName());
                this.livePlayerManager.getLiveChannelPlayer(this.mVideoView, this.currentLiveChannelItem.getChannelName());
            }
            LiveChannelItem liveChannelItem2 = this.currentLiveChannelItem;
            channel_Name = liveChannelItem2;
            liveChannelItem2.setinclude_back(liveChannelItem2.getUrl().indexOf("PLTV/8888") != -1);
            this.mHandler.post(this.tv_sys_timeRunnable);
            this.tv_channelname.setText(channel_Name.getChannelName());
            TextView textView = this.tv_channelnum;
            textView.setText("" + channel_Name.getChannelNum());
            LiveChannelItem liveChannelItem3 = channel_Name;
            if (liveChannelItem3 == null || liveChannelItem3.getSourceNum() <= 0) {
                this.tv_source.setText("1/1");
            } else {
                TextView textView2 = this.tv_source;
                textView2.setText("线路 " + (channel_Name.getSourceIndex() + 1) + "/" + channel_Name.getSourceNum());
            }
            getEpg(new Date());
            this.mVideoView.setUrl(this.currentLiveChannelItem.getUrl());
            showChannelInfo();
            this.mVideoView.start();
            return true;
        }
        showChannelInfo();
        return true;
    }

    private void playNext() {
        if (isCurrentLiveChannelValid()) {
            Integer[] nextChannel = getNextChannel(1);
            playChannel(nextChannel[0].intValue(), nextChannel[1].intValue(), false);
        }
    }

    private void playPrevious() {
        if (isCurrentLiveChannelValid()) {
            Integer[] nextChannel = getNextChannel(-1);
            playChannel(nextChannel[0].intValue(), nextChannel[1].intValue(), false);
        }
    }

    public void playPreSource() {
        if (isCurrentLiveChannelValid()) {
            this.currentLiveChannelItem.preSource();
            playChannel(currentChannelGroupIndex, this.currentLiveChannelIndex, true);
        }
    }

    public void playNextSource() {
        if (isCurrentLiveChannelValid()) {
            this.currentLiveChannelItem.nextSource();
            playChannel(currentChannelGroupIndex, this.currentLiveChannelIndex, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showSettingGroup() {
        if (this.tvLeftChannelListLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelListRun);
            this.mHandler.post(this.mHideChannelListRun);
        }
        if (this.tvRightSettingLayout.getVisibility() != 4) {
            this.mHandler.removeCallbacks(this.mHideSettingLayoutRun);
            this.mHandler.post(this.mHideSettingLayoutRun);
        } else if (isCurrentLiveChannelValid()) {
            loadCurrentSourceList();
            this.liveSettingGroupAdapter.setNewData(this.liveSettingGroupList);
            selectSettingGroup(0, false);
            this.mSettingGroupView.scrollToPosition(0);
            this.mSettingItemView.scrollToPosition(this.currentLiveChannelItem.getSourceIndex());
            this.mHandler.postDelayed(this.mFocusAndShowSettingGroup, 200);
        }
    }

    private void initVideoView() {
        LiveController liveController = new LiveController(this);
        this.controller = liveController;
        liveController.setListener(new LiveController.LiveControlListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass13 */

            @Override // com.github.tvbox.osc.player.controller.LiveController.LiveControlListener
            public boolean singleTap() {
                LivePlayActivity.this.showChannelList();
                return true;
            }

            @Override // com.github.tvbox.osc.player.controller.LiveController.LiveControlListener
            public void longPress() {
                LivePlayActivity.this.showSettingGroup();
            }

            @Override // com.github.tvbox.osc.player.controller.LiveController.LiveControlListener
            public void playStateChanged(int i) {
                if (i != -1) {
                    if (i != 1) {
                        if (i != 2) {
                            if (i != 3) {
                                if (i != 5) {
                                    if (i != 6) {
                                        if (i != 7) {
                                            return;
                                        }
                                    }
                                }
                            }
                            LivePlayActivity.this.currentLiveChangeSourceTimes = 0;
                            LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mConnectTimeoutChangeSourceRun);
                            return;
                        }
                        if (LivePlayActivity.this.mVideoView.getVideoSize().length >= 2) {
                            TextView textView = LivePlayActivity.this.tv_size;
                            textView.setText(LivePlayActivity.this.mVideoView.getVideoSize()[0] + " x " + LivePlayActivity.this.mVideoView.getVideoSize()[1]);
                        }
                        int duration = (int) LivePlayActivity.this.mVideoView.getDuration();
                        if (duration > 0) {
                            LivePlayActivity.this.isVOD = true;
                            LivePlayActivity.this.llSeekBar.setVisibility(0);
                            LivePlayActivity.this.mSeekBar.setProgress(10);
                            LivePlayActivity.this.mSeekBar.setMax(duration);
                            LivePlayActivity.this.mSeekBar.setProgress(0);
                            LivePlayActivity.this.mTotalTime.setText(PlayerUtils.stringForTimeVod(duration));
                            return;
                        }
                        LivePlayActivity.this.isVOD = false;
                        LivePlayActivity.this.llSeekBar.setVisibility(8);
                        return;
                    }
                    LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mConnectTimeoutChangeSourceRun);
                    LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mConnectTimeoutChangeSourceRun, ((long) (((Integer) Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1)).intValue() + 1)) * DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
                    return;
                }
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mConnectTimeoutChangeSourceRun);
                LivePlayActivity.this.mHandler.post(LivePlayActivity.this.mConnectTimeoutChangeSourceRun);
            }

            @Override // com.github.tvbox.osc.player.controller.LiveController.LiveControlListener
            public void changeSource(int i) {
                if (i > 0) {
                    LivePlayActivity.this.playNextSource();
                } else {
                    LivePlayActivity.this.playPreSource();
                }
            }
        });
        this.controller.setCanChangePosition(false);
        this.controller.setEnableInNormal(true);
        this.controller.setGestureEnabled(true);
        this.controller.setDoubleTapTogglePlayEnabled(false);
        this.mVideoView.setVideoController(this.controller);
        this.mVideoView.setProgressManager(null);
    }

    private void initEpgListView() {
        this.mEpgInfoGridView.setHasFixedSize(true);
        this.mEpgInfoGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        LiveEpgAdapter liveEpgAdapter = new LiveEpgAdapter();
        this.epgListAdapter = liveEpgAdapter;
        this.mEpgInfoGridView.setAdapter(liveEpgAdapter);
        this.mEpgInfoGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass15 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
            }
        });
        this.mEpgInfoGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass16 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.epgListAdapter.setFocusedEpgIndex(-1);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                LivePlayActivity.this.epgListAdapter.setFocusedEpgIndex(i);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                Date date;
                if (LivePlayActivity.this.epgDateAdapter.getSelectedIndex() < 0) {
                    date = new Date();
                } else {
                    date = ((LiveEpgDate) LivePlayActivity.this.epgDateAdapter.getData().get(LivePlayActivity.this.epgDateAdapter.getSelectedIndex())).getDateParamVal();
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                Epginfo epginfo = (Epginfo) LivePlayActivity.this.epgListAdapter.getItem(i);
                String format = simpleDateFormat.format(date);
                String str = format + epginfo.originStart.replace(":", "") + "30";
                String str2 = format + epginfo.originEnd.replace(":", "") + "30";
                Date date2 = new Date();
                if (date2.compareTo(epginfo.startdateTime) >= 0) {
                    LivePlayActivity.this.epgListAdapter.setSelectedEpgIndex(i);
                    if (date2.compareTo(epginfo.startdateTime) >= 0 && date2.compareTo(epginfo.enddateTime) <= 0) {
                        LivePlayActivity.this.mVideoView.release();
                        LivePlayActivity.this.isSHIYI = false;
                        LivePlayActivity.this.mVideoView.setUrl(LivePlayActivity.this.currentLiveChannelItem.getUrl());
                        LivePlayActivity.this.mVideoView.start();
                        LivePlayActivity.this.epgListAdapter.setShiyiSelection(-1, false, LivePlayActivity.this.timeFormat.format(date));
                    }
                    if (date2.compareTo(epginfo.startdateTime) >= 0) {
                        LivePlayActivity.this.mVideoView.release();
                        String unused = LivePlayActivity.shiyi_time = str + "-" + str2;
                        LivePlayActivity.this.isSHIYI = true;
                        LivePlayActivity.this.mVideoView.setUrl(LivePlayActivity.this.currentLiveChannelItem.getUrl() + "?playseek=" + LivePlayActivity.shiyi_time);
                        LivePlayActivity.this.mVideoView.start();
                        LivePlayActivity.this.epgListAdapter.setShiyiSelection(i, true, LivePlayActivity.this.timeFormat.format(date));
                        LivePlayActivity.this.epgListAdapter.notifyDataSetChanged();
                        LivePlayActivity.this.mEpgInfoGridView.setSelectedPosition(i);
                    }
                }
            }
        });
        this.epgListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass17 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Date date;
                if (LivePlayActivity.this.epgDateAdapter.getSelectedIndex() < 0) {
                    date = new Date();
                } else {
                    date = ((LiveEpgDate) LivePlayActivity.this.epgDateAdapter.getData().get(LivePlayActivity.this.epgDateAdapter.getSelectedIndex())).getDateParamVal();
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                Epginfo epginfo = (Epginfo) LivePlayActivity.this.epgListAdapter.getItem(i);
                String format = simpleDateFormat.format(date);
                String str = format + epginfo.originStart.replace(":", "") + "30";
                String str2 = format + epginfo.originEnd.replace(":", "") + "30";
                Date date2 = new Date();
                if (date2.compareTo(epginfo.startdateTime) >= 0) {
                    LivePlayActivity.this.epgListAdapter.setSelectedEpgIndex(i);
                    if (date2.compareTo(epginfo.startdateTime) >= 0 && date2.compareTo(epginfo.enddateTime) <= 0) {
                        LivePlayActivity.this.mVideoView.release();
                        LivePlayActivity.this.isSHIYI = false;
                        LivePlayActivity.this.mVideoView.setUrl(LivePlayActivity.this.currentLiveChannelItem.getUrl());
                        LivePlayActivity.this.mVideoView.start();
                        LivePlayActivity.this.epgListAdapter.setShiyiSelection(-1, false, LivePlayActivity.this.timeFormat.format(date));
                    }
                    if (date2.compareTo(epginfo.startdateTime) >= 0) {
                        LivePlayActivity.this.mVideoView.release();
                        String unused = LivePlayActivity.shiyi_time = str + "-" + str2;
                        LivePlayActivity.this.isSHIYI = true;
                        LivePlayActivity.this.mVideoView.setUrl(LivePlayActivity.this.currentLiveChannelItem.getUrl() + "?playseek=" + LivePlayActivity.shiyi_time);
                        LivePlayActivity.this.mVideoView.start();
                        LivePlayActivity.this.epgListAdapter.setShiyiSelection(i, true, LivePlayActivity.this.timeFormat.format(date));
                        LivePlayActivity.this.epgListAdapter.notifyDataSetChanged();
                        LivePlayActivity.this.mEpgInfoGridView.setSelectedPosition(i);
                    }
                }
            }
        });
    }

    private void initEpgDateView() {
        this.mEpgDateGridView.setHasFixedSize(true);
        this.mEpgDateGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        this.epgDateAdapter = new LiveEpgDateAdapter();
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.SIMPLIFIED_CHINESE);
        instance.add(5, -6);
        for (int i = 0; i < 9; i++) {
            Date time = instance.getTime();
            LiveEpgDate liveEpgDate = new LiveEpgDate();
            liveEpgDate.setIndex(i);
            if (i == 5) {
                liveEpgDate.setDatePresented("昨天");
            } else if (i == 6) {
                liveEpgDate.setDatePresented("今天");
            } else if (i == 7) {
                liveEpgDate.setDatePresented("明天");
            } else if (i == 8) {
                liveEpgDate.setDatePresented("后天");
            } else {
                liveEpgDate.setDatePresented(simpleDateFormat.format(time));
            }
            liveEpgDate.setDateParamVal(time);
            this.epgDateAdapter.addData(liveEpgDate);
            instance.add(5, 1);
        }
        this.mEpgDateGridView.setAdapter(this.epgDateAdapter);
        this.mEpgDateGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass18 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
            }
        });
        this.mEpgDateGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass19 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.epgDateAdapter.setFocusedIndex(-1);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                LivePlayActivity.this.epgDateAdapter.setFocusedIndex(i);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                LivePlayActivity.this.epgDateAdapter.setSelectedIndex(i);
                LivePlayActivity livePlayActivity = LivePlayActivity.this;
                livePlayActivity.getEpg(((LiveEpgDate) livePlayActivity.epgDateAdapter.getData().get(i)).getDateParamVal());
            }
        });
        this.epgDateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass20 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                LivePlayActivity.this.epgDateAdapter.setSelectedIndex(i);
                LivePlayActivity livePlayActivity = LivePlayActivity.this;
                livePlayActivity.getEpg(((LiveEpgDate) livePlayActivity.epgDateAdapter.getData().get(i)).getDateParamVal());
            }
        });
        this.epgDateAdapter.setSelectedIndex(1);
    }

    private void initChannelGroupView() {
        this.mGroupGridView.setHasFixedSize(true);
        this.mGroupGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        LiveChannelGroupAdapter liveChannelGroupAdapter2 = new LiveChannelGroupAdapter();
        this.liveChannelGroupAdapter = liveChannelGroupAdapter2;
        this.mGroupGridView.setAdapter(liveChannelGroupAdapter2);
        this.mGroupGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass21 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
            }
        });
        this.mGroupGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass22 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.selectChannelGroup(i, true, -1);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                if (LivePlayActivity.this.isNeedInputPassword(i)) {
                    LivePlayActivity.this.showPasswordDialog(i, -1);
                }
            }
        });
        this.liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass23 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                LivePlayActivity.this.selectChannelGroup(i, false, -1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void selectChannelGroup(int i, boolean z, int i2) {
        if (z) {
            this.liveChannelGroupAdapter.setFocusedGroupIndex(i);
            this.liveChannelItemAdapter.setFocusedChannelIndex(-1);
        }
        if ((i > -1 && i != this.liveChannelGroupAdapter.getSelectedGroupIndex()) || isNeedInputPassword(i)) {
            this.liveChannelGroupAdapter.setSelectedGroupIndex(i);
            if (isNeedInputPassword(i)) {
                showPasswordDialog(i, i2);
                return;
            }
            loadChannelGroupDataAndPlay(i, i2);
        }
        if (this.tvLeftChannelListLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelListRun);
            this.mHandler.postDelayed(this.mHideChannelListRun, 6000);
        }
    }

    private void initLiveChannelView() {
        this.mChannelGridView.setHasFixedSize(true);
        this.mChannelGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        LiveChannelItemAdapter liveChannelItemAdapter2 = new LiveChannelItemAdapter();
        this.liveChannelItemAdapter = liveChannelItemAdapter2;
        this.mChannelGridView.setAdapter(liveChannelItemAdapter2);
        this.mChannelGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass24 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
            }
        });
        this.mChannelGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass25 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (i >= 0) {
                    LivePlayActivity.this.liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                    LivePlayActivity.this.liveChannelItemAdapter.setFocusedChannelIndex(i);
                    LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideChannelListRun);
                    LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.clickLiveChannel(i);
            }
        });
        this.liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass26 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                LivePlayActivity.this.clickLiveChannel(i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickLiveChannel(int i) {
        this.liveChannelItemAdapter.setSelectedChannelIndex(i);
        this.epgDateAdapter.setSelectedIndex(6);
        if (this.tvLeftChannelListLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelListRun);
            this.mHandler.post(this.mHideChannelListRun);
        }
        playChannel(this.liveChannelGroupAdapter.getSelectedGroupIndex(), i, false);
    }

    private void initSettingGroupView() {
        this.mSettingGroupView.setHasFixedSize(true);
        this.mSettingGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        LiveSettingGroupAdapter liveSettingGroupAdapter2 = new LiveSettingGroupAdapter();
        this.liveSettingGroupAdapter = liveSettingGroupAdapter2;
        this.mSettingGroupView.setAdapter(liveSettingGroupAdapter2);
        this.mSettingGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass27 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideSettingLayoutRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideSettingLayoutRun, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            }
        });
        this.mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass28 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.selectSettingGroup(i, true);
            }
        });
        this.liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass29 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                LivePlayActivity.this.selectSettingGroup(i, false);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void selectSettingGroup(int i, boolean z) {
        if (isCurrentLiveChannelValid()) {
            if (z) {
                this.liveSettingGroupAdapter.setFocusedGroupIndex(i);
                this.liveSettingItemAdapter.setFocusedItemIndex(-1);
            }
            if (i != this.liveSettingGroupAdapter.getSelectedGroupIndex() && i >= -1) {
                this.liveSettingGroupAdapter.setSelectedGroupIndex(i);
                this.liveSettingItemAdapter.setNewData(this.liveSettingGroupList.get(i).getLiveSettingItems());
                int i2 = 0;
                if (i == 0) {
                    this.liveSettingItemAdapter.selectItem(this.currentLiveChannelItem.getSourceIndex(), true, false);
                } else if (i == 1) {
                    this.liveSettingItemAdapter.selectItem(this.livePlayerManager.getLivePlayerScale(), true, true);
                } else if (i == 2) {
                    this.liveSettingItemAdapter.selectItem(this.livePlayerManager.getLivePlayerType(), true, true);
                }
                int selectedItemIndex = this.liveSettingItemAdapter.getSelectedItemIndex();
                if (selectedItemIndex >= 0) {
                    i2 = selectedItemIndex;
                }
                this.mSettingItemView.scrollToPosition(i2);
                this.mHandler.removeCallbacks(this.mHideSettingLayoutRun);
                this.mHandler.postDelayed(this.mHideSettingLayoutRun, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            }
        }
    }

    private void initSettingItemView() {
        this.mSettingItemView.setHasFixedSize(true);
        this.mSettingItemView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        LiveSettingItemAdapter liveSettingItemAdapter2 = new LiveSettingItemAdapter();
        this.liveSettingItemAdapter = liveSettingItemAdapter2;
        this.mSettingItemView.setAdapter(liveSettingItemAdapter2);
        this.mSettingItemView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass30 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideSettingLayoutRun);
                LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideSettingLayoutRun, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            }
        });
        this.mSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass31 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (i >= 0) {
                    LivePlayActivity.this.liveSettingGroupAdapter.setFocusedGroupIndex(-1);
                    LivePlayActivity.this.liveSettingItemAdapter.setFocusedItemIndex(i);
                    LivePlayActivity.this.mHandler.removeCallbacks(LivePlayActivity.this.mHideSettingLayoutRun);
                    LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideSettingLayoutRun, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                LivePlayActivity.this.clickSettingItem(i);
            }
        });
        this.liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass32 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                LivePlayActivity.this.clickSettingItem(i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickSettingItem(int i) {
        boolean z;
        int selectedGroupIndex = this.liveSettingGroupAdapter.getSelectedGroupIndex();
        if (selectedGroupIndex < 4) {
            if (i != this.liveSettingItemAdapter.getSelectedItemIndex()) {
                this.liveSettingItemAdapter.selectItem(i, true, true);
            } else {
                return;
            }
        }
        if (selectedGroupIndex == 0) {
            this.currentLiveChannelItem.setSourceIndex(i);
            playChannel(currentChannelGroupIndex, this.currentLiveChannelIndex, true);
        } else if (selectedGroupIndex == 1) {
            this.livePlayerManager.changeLivePlayerScale(this.mVideoView, i, this.currentLiveChannelItem.getChannelName());
        } else if (selectedGroupIndex == 2) {
            this.mVideoView.release();
            this.livePlayerManager.changeLivePlayerType(this.mVideoView, i, this.currentLiveChannelItem.getChannelName());
            this.mVideoView.setUrl(this.currentLiveChannelItem.getUrl());
            this.mVideoView.start();
        } else if (selectedGroupIndex == 3) {
            Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, Integer.valueOf(i));
        } else if (selectedGroupIndex == 4) {
            if (i == 0) {
                z = !((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)).booleanValue();
                Hawk.put(HawkConfig.LIVE_SHOW_TIME, Boolean.valueOf(z));
                showTime();
            } else if (i == 1) {
                z = !((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)).booleanValue();
                Hawk.put(HawkConfig.LIVE_SHOW_NET_SPEED, Boolean.valueOf(z));
                showNetSpeed();
            } else if (i == 2) {
                z = !((Boolean) Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)).booleanValue();
                Hawk.put(HawkConfig.LIVE_CHANNEL_REVERSE, Boolean.valueOf(z));
            } else if (i == 3) {
                z = !((Boolean) Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)).booleanValue();
                Hawk.put(HawkConfig.LIVE_CROSS_GROUP, Boolean.valueOf(z));
            } else if (i != 4) {
                if (i == 5) {
                    ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.LIVE_HISTORY, new ArrayList());
                    if (!arrayList.isEmpty()) {
                        String str = (String) Hawk.get(HawkConfig.LIVE_URL, "");
                        int indexOf = arrayList.contains(str) ? arrayList.indexOf(str) : 0;
                        final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(this);
                        apiHistoryDialog.setTip(getString(R.string.dia_history_live));
                        apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass33 */

                            @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                            public void click(String str) {
                                Hawk.put(HawkConfig.LIVE_URL, str);
                                LivePlayActivity.this.liveChannelGroupList.clear();
                                try {
                                    String encodeToString = Base64.encodeToString(str.getBytes("UTF-8"), 10);
                                    LivePlayActivity.this.loadProxyLives("http://127.0.0.1:9978/proxy?do=live&type=txt&ext=" + encodeToString);
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                }
                                apiHistoryDialog.dismiss();
                            }

                            @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                            public void del(String str, ArrayList<String> arrayList) {
                                Hawk.put(HawkConfig.LIVE_HISTORY, arrayList);
                            }
                        }, arrayList, indexOf);
                        apiHistoryDialog.show();
                    } else {
                        return;
                    }
                }
                z = false;
            } else {
                z = !((Boolean) Hawk.get(HawkConfig.LIVE_SKIP_PASSWORD, false)).booleanValue();
                Hawk.put(HawkConfig.LIVE_SKIP_PASSWORD, Boolean.valueOf(z));
            }
            this.liveSettingItemAdapter.selectItem(i, z, false);
        }
        this.mHandler.removeCallbacks(this.mHideSettingLayoutRun);
        this.mHandler.postDelayed(this.mHideSettingLayoutRun, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    private void initLiveChannelList() {
        List<LiveChannelGroup> channelGroupList = ApiConfig.get().getChannelGroupList();
        if (channelGroupList.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", 0).show();
            finish();
        } else if (channelGroupList.size() != 1 || !channelGroupList.get(0).getGroupName().startsWith("http://127.0.0.1")) {
            this.liveChannelGroupList.clear();
            this.liveChannelGroupList.addAll(channelGroupList);
            showSuccess();
            initLiveState();
        } else {
            showLoading();
            loadProxyLives(channelGroupList.get(0).getGroupName());
        }
    }

    public void loadProxyLives(String str) {
        try {
            String str2 = new String(Base64.decode(Uri.parse(str).getQueryParameter("ext"), 10), "UTF-8");
            showLoading();
            OkGo.get(str2).execute(new AbsCallback<String>() {
                /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass34 */

                @Override // com.lzy.okgo.convert.Converter
                public String convertResponse(Response response) throws Throwable {
                    return response.body().string();
                }

                @Override // com.lzy.okgo.callback.Callback
                public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    TxtSubscribe.parse(linkedHashMap, response.body());
                    ApiConfig.get().loadLives(TxtSubscribe.live2JsonArray(linkedHashMap));
                    List<LiveChannelGroup> channelGroupList = ApiConfig.get().getChannelGroupList();
                    if (channelGroupList.isEmpty()) {
                        Toast.makeText(App.getInstance(), "频道列表为空", 0).show();
                        LivePlayActivity.this.finish();
                        return;
                    }
                    LivePlayActivity.this.liveChannelGroupList.clear();
                    LivePlayActivity.this.liveChannelGroupList.addAll(channelGroupList);
                    LivePlayActivity.this.mHandler.post(new Runnable() {
                        /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass34.AnonymousClass1 */

                        public void run() {
                            LivePlayActivity.this.showSuccess();
                            LivePlayActivity.this.initLiveState();
                        }
                    });
                }
            });
        } catch (Throwable unused) {
            Toast.makeText(App.getInstance(), "频道列表为空", 0).show();
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 11) {
            Bundle bundle = (Bundle) refreshEvent.obj;
            int i = bundle.getInt("groupIndex", 0);
            int i2 = bundle.getInt("channelIndex", 0);
            if (i != this.liveChannelGroupAdapter.getSelectedGroupIndex()) {
                selectChannelGroup(i, true, i2);
                return;
            }
            clickLiveChannel(i2);
            this.mGroupGridView.scrollToPosition(i);
            this.mChannelGridView.scrollToPosition(i2);
            playChannel(i, i2, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initLiveState() {
        int i;
        int i2;
        String str = (String) Hawk.get(HawkConfig.LIVE_CHANNEL, "");
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            int i3 = -1;
            int i4 = -1;
            for (LiveChannelGroup liveChannelGroup : this.liveChannelGroupList) {
                Iterator<LiveChannelItem> it = liveChannelGroup.getLiveChannels().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LiveChannelItem next = it.next();
                    if (next.getChannelName().equals(str)) {
                        i3 = liveChannelGroup.getGroupIndex();
                        i4 = next.getChannelIndex();
                        continue;
                        break;
                    }
                }
                if (i3 != -1) {
                    break;
                }
            }
            i = i3;
            i2 = i4;
            if (i == -1) {
                int firstNoPasswordChannelGroup = getFirstNoPasswordChannelGroup();
                i = firstNoPasswordChannelGroup == -1 ? 0 : firstNoPasswordChannelGroup;
                i2 = 0;
            }
        } else {
            Bundle extras = intent.getExtras();
            i = extras.getInt("groupIndex", 0);
            i2 = extras.getInt("channelIndex", 0);
        }
        this.livePlayerManager.init(this.mVideoView);
        showTime();
        showNetSpeed();
        this.tvLeftChannelListLayout.setVisibility(4);
        this.tvRightSettingLayout.setVisibility(4);
        this.liveChannelGroupAdapter.setNewData(this.liveChannelGroupList);
        selectChannelGroup(i, false, i2);
    }

    private boolean isListOrSettingLayoutVisible() {
        return this.tvLeftChannelListLayout.getVisibility() == 0 || this.tvRightSettingLayout.getVisibility() == 0;
    }

    private void initLiveSettingGroupList() {
        ArrayList arrayList = new ArrayList(Arrays.asList("线路选择", "画面比例", "播放解码", "超时换源", "偏好设置"));
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList arrayList5 = new ArrayList(Arrays.asList("系统", "ijk硬解", "ijk软解", "exo"));
        ArrayList arrayList6 = new ArrayList(Arrays.asList("5s", "10s", "15s", "20s", "25s", "30s"));
        ArrayList arrayList7 = new ArrayList(Arrays.asList("显示时间", "显示网速", "换台反转", "跨选分类", "关闭密码", "直播列表"));
        arrayList2.add(arrayList3);
        arrayList2.add(arrayList4);
        arrayList2.add(arrayList5);
        arrayList2.add(arrayList6);
        arrayList2.add(arrayList7);
        this.liveSettingGroupList.clear();
        for (int i = 0; i < arrayList.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> arrayList8 = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName((String) arrayList.get(i));
            for (int i2 = 0; i2 < ((ArrayList) arrayList2.get(i)).size(); i2++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(i2);
                liveSettingItem.setItemName((String) ((ArrayList) arrayList2.get(i)).get(i2));
                arrayList8.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(arrayList8);
            this.liveSettingGroupList.add(liveSettingGroup);
        }
        this.liveSettingGroupList.get(3).getLiveSettingItems().get(((Integer) Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1)).intValue()).setItemSelected(true);
        this.liveSettingGroupList.get(4).getLiveSettingItems().get(0).setItemSelected(((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)).booleanValue());
        this.liveSettingGroupList.get(4).getLiveSettingItems().get(1).setItemSelected(((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)).booleanValue());
        this.liveSettingGroupList.get(4).getLiveSettingItems().get(2).setItemSelected(((Boolean) Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)).booleanValue());
        this.liveSettingGroupList.get(4).getLiveSettingItems().get(3).setItemSelected(((Boolean) Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)).booleanValue());
        this.liveSettingGroupList.get(4).getLiveSettingItems().get(4).setItemSelected(((Boolean) Hawk.get(HawkConfig.LIVE_SKIP_PASSWORD, false)).booleanValue());
    }

    private void loadCurrentSourceList() {
        ArrayList<String> channelSourceNames = this.currentLiveChannelItem.getChannelSourceNames();
        ArrayList<LiveSettingItem> arrayList = new ArrayList<>();
        for (int i = 0; i < channelSourceNames.size(); i++) {
            LiveSettingItem liveSettingItem = new LiveSettingItem();
            liveSettingItem.setItemIndex(i);
            liveSettingItem.setItemName(channelSourceNames.get(i));
            arrayList.add(liveSettingItem);
        }
        this.liveSettingGroupList.get(0).setLiveSettingItems(arrayList);
    }

    /* access modifiers changed from: package-private */
    public void showTime() {
        if (((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)).booleanValue()) {
            this.mHandler.post(this.mUpdateTimeRun);
            this.tvTime.setVisibility(0);
            return;
        }
        this.mHandler.removeCallbacks(this.mUpdateTimeRun);
        this.tvTime.setVisibility(8);
    }

    private void showNetSpeed() {
        if (((Boolean) Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)).booleanValue()) {
            this.mHandler.post(this.mUpdateNetSpeedRun);
            this.tvNetSpeed.setVisibility(0);
            return;
        }
        this.mHandler.removeCallbacks(this.mUpdateNetSpeedRun);
        this.tvNetSpeed.setVisibility(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showPasswordDialog(final int i, final int i2) {
        if (this.tvLeftChannelListLayout.getVisibility() == 0) {
            this.mHandler.removeCallbacks(this.mHideChannelListRun);
        }
        LivePasswordDialog livePasswordDialog = new LivePasswordDialog(this);
        livePasswordDialog.setOnListener(new LivePasswordDialog.OnListener() {
            /* class com.github.tvbox.osc.ui.activity.LivePlayActivity.AnonymousClass37 */

            @Override // com.github.tvbox.osc.ui.dialog.LivePasswordDialog.OnListener
            public void onChange(String str) {
                if (str.equals(((LiveChannelGroup) LivePlayActivity.this.liveChannelGroupList.get(i)).getGroupPassword())) {
                    LivePlayActivity.this.channelGroupPasswordConfirmed.add(Integer.valueOf(i));
                    LivePlayActivity.this.loadChannelGroupDataAndPlay(i, i2);
                } else {
                    Toast.makeText(App.getInstance(), "密码错误", 0).show();
                }
                if (LivePlayActivity.this.tvLeftChannelListLayout.getVisibility() == 0) {
                    LivePlayActivity.this.mHandler.postDelayed(LivePlayActivity.this.mHideChannelListRun, 6000);
                }
            }

            @Override // com.github.tvbox.osc.ui.dialog.LivePasswordDialog.OnListener
            public void onCancel() {
                if (LivePlayActivity.this.tvLeftChannelListLayout.getVisibility() == 0) {
                    LivePlayActivity.this.liveChannelItemAdapter.setNewData(LivePlayActivity.this.getLiveChannels(LivePlayActivity.this.liveChannelGroupAdapter.getSelectedGroupIndex()));
                }
            }
        });
        livePasswordDialog.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadChannelGroupDataAndPlay(int i, int i2) {
        this.liveChannelItemAdapter.setNewData(getLiveChannels(i));
        if (i == currentChannelGroupIndex) {
            int i3 = this.currentLiveChannelIndex;
            if (i3 > -1) {
                this.mChannelGridView.scrollToPosition(i3);
            }
            this.liveChannelItemAdapter.setSelectedChannelIndex(this.currentLiveChannelIndex);
        } else {
            this.mChannelGridView.scrollToPosition(0);
            this.liveChannelItemAdapter.setSelectedChannelIndex(-1);
        }
        if (i2 > -1) {
            clickLiveChannel(i2);
            this.mGroupGridView.scrollToPosition(i);
            this.mChannelGridView.scrollToPosition(i2);
            playChannel(i, i2, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedInputPassword(int i) {
        return !this.liveChannelGroupList.get(i).getGroupPassword().isEmpty() && !isPasswordConfirmed(i);
    }

    private boolean isPasswordConfirmed(int i) {
        if (((Boolean) Hawk.get(HawkConfig.LIVE_SKIP_PASSWORD, false)).booleanValue()) {
            return true;
        }
        Iterator<Integer> it = this.channelGroupPasswordConfirmed.iterator();
        while (it.hasNext()) {
            if (it.next().intValue() == i) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<LiveChannelItem> getLiveChannels(int i) {
        if (!isNeedInputPassword(i)) {
            return this.liveChannelGroupList.get(i).getLiveChannels();
        }
        return new ArrayList<>();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Integer[] getNextChannel(int i) {
        int i2;
        int i3 = currentChannelGroupIndex;
        int i4 = this.currentLiveChannelIndex;
        if (i > 0) {
            i2 = i4 + 1;
            if (i2 >= getLiveChannels(i3).size()) {
                if (((Boolean) Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)).booleanValue()) {
                    while (true) {
                        i3++;
                        if (i3 >= this.liveChannelGroupList.size()) {
                            i3 = 0;
                        }
                        if (this.liveChannelGroupList.get(i3).getGroupPassword().isEmpty() && i3 != currentChannelGroupIndex) {
                            break;
                        }
                    }
                }
                i2 = 0;
            }
        } else {
            i2 = i4 - 1;
            if (i2 < 0) {
                if (((Boolean) Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)).booleanValue()) {
                    while (true) {
                        i3--;
                        if (i3 < 0) {
                            i3 = this.liveChannelGroupList.size() - 1;
                        }
                        if (this.liveChannelGroupList.get(i3).getGroupPassword().isEmpty() && i3 != currentChannelGroupIndex) {
                            break;
                        }
                    }
                }
                i2 = getLiveChannels(i3).size() - 1;
            }
        }
        return new Integer[]{Integer.valueOf(i3), Integer.valueOf(i2)};
    }

    private int getFirstNoPasswordChannelGroup() {
        for (LiveChannelGroup liveChannelGroup : this.liveChannelGroupList) {
            if (liveChannelGroup.getGroupPassword().isEmpty()) {
                return liveChannelGroup.getGroupIndex();
            }
        }
        return -1;
    }

    private boolean isCurrentLiveChannelValid() {
        if (this.currentLiveChannelItem != null) {
            return true;
        }
        Toast.makeText(App.getInstance(), "请先选择频道", 0).show();
        return false;
    }
}
