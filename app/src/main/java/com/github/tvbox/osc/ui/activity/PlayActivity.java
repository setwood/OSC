package com.github.tvbox.osc.ui.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import com.github.catvod.crawler.Spider;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.bean.Subtitle;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.CacheManager;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.player.IjkMediaPlayer;
import com.github.tvbox.osc.player.MyVideoView;
import com.github.tvbox.osc.player.TrackInfo;
import com.github.tvbox.osc.player.TrackInfoBean;
import com.github.tvbox.osc.player.controller.VodController;
import com.github.tvbox.osc.player.thirdparty.Kodi;
import com.github.tvbox.osc.player.thirdparty.MXPlayer;
import com.github.tvbox.osc.player.thirdparty.ReexPlayer;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.SubtitleDialog;
import com.github.tvbox.osc.util.AdBlocker;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.MD5;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.XWalkUtils;
import com.github.tvbox.osc.util.thunder.Thunder;
import com.github.tvbox.osc.util.urlhttp.UrlHttpUtil;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor;
import com.google.android.exoplayer2.source.rtsp.RtspHeaders;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.cookie.SerializableCookie;
import com.lzy.okgo.request.GetRequest;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.orhanobut.hawk.Hawk;
import com.umeng.analytics.pro.an;
import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.jessyan.autosize.AutoSize;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import xyz.doikki.videoplayer.player.AbstractPlayer;
import xyz.doikki.videoplayer.player.ProgressManager;

public class PlayActivity extends BaseActivity {
    private static final int PIP_BOARDCAST_ACTION_NEXT = 2;
    private static final int PIP_BOARDCAST_ACTION_PLAYPAUSE = 1;
    private static final int PIP_BOARDCAST_ACTION_PREV = 0;
    boolean PiPON = ((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, false)).booleanValue();
    private int autoRetryCount = 0;
    private boolean extPlay = false;
    private boolean loadFound = false;
    private final Map<String, Boolean> loadedUrls = new HashMap();
    private VodController mController;
    private Handler mHandler;
    private ImageView mPlayLoadErr;
    private TextView mPlayLoadTip;
    private ProgressBar mPlayLoading;
    private SysWebClient mSysWebClient;
    private WebView mSysWebView;
    private MyVideoView mVideoView;
    private VodInfo mVodInfo;
    private JSONObject mVodPlayerCfg;
    private XWalkWebClient mX5WebClient;
    private XWalkView mXwalkWebView;
    private boolean onStopCalled;
    private String parseFlag;
    ExecutorService parseThreadPool;
    private BroadcastReceiver pipActionReceiver;
    private String playSubtitle;
    private String progressKey;
    private SourceBean sourceBean;
    private String sourceKey;
    private SourceViewModel sourceViewModel;
    private String subtitleCacheKey;
    private String videoURL;
    private Map<String, String> webHeaderMap;
    private String webUrl;
    private String webUserAgent;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_play;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        initView();
        initViewModel();
        initData();
    }

    private void initView() {
        hideSystemUI(false);
        this.mHandler = new Handler(new Handler.Callback() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass1 */

            public boolean handleMessage(Message message) {
                if (message.what == 100) {
                    PlayActivity.this.stopParse();
                    PlayActivity.this.errorWithRetry("嗅探错误", false);
                }
                return false;
            }
        });
        this.mVideoView = (MyVideoView) findViewById(R.id.mVideoView);
        this.mPlayLoadTip = (TextView) findViewById(R.id.play_load_tip);
        this.mPlayLoading = (ProgressBar) findViewById(R.id.play_loading);
        this.mPlayLoadErr = (ImageView) findViewById(R.id.play_load_error);
        VodController vodController = new VodController(this);
        this.mController = vodController;
        vodController.setCanChangePosition(true);
        this.mController.setEnableInNormal(true);
        this.mController.setGestureEnabled(true);
        this.mVideoView.setProgressManager(new ProgressManager() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass2 */

            @Override // xyz.doikki.videoplayer.player.ProgressManager
            public void saveProgress(String str, long j) {
                CacheManager.save(MD5.string2MD5(str), Long.valueOf(j));
            }

            @Override // xyz.doikki.videoplayer.player.ProgressManager
            public long getSavedProgress(String str) {
                int i;
                try {
                    i = PlayActivity.this.mVodPlayerCfg.getInt("st");
                } catch (JSONException e) {
                    e.printStackTrace();
                    i = 0;
                }
                long j = (long) (i * 1000);
                if (CacheManager.getCache(MD5.string2MD5(str)) == null) {
                    return j;
                }
                long longValue = ((Long) CacheManager.getCache(MD5.string2MD5(str))).longValue();
                return longValue < j ? j : longValue;
            }
        });
        this.mController.setListener(new VodController.VodControlListener() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass3 */

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void playNext(boolean z) {
                if (PlayActivity.this.mVodInfo.reverseSort) {
                    PlayActivity.this.playPrevious();
                    return;
                }
                String str = PlayActivity.this.progressKey;
                PlayActivity.this.playNext(z);
                if (z && str != null) {
                    CacheManager.delete(MD5.string2MD5(str), 0);
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void playPre() {
                if (PlayActivity.this.mVodInfo.reverseSort) {
                    PlayActivity.this.playNext(false);
                } else {
                    PlayActivity.this.playPrevious();
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void changeParse(ParseBean parseBean) {
                PlayActivity.this.autoRetryCount = 0;
                PlayActivity.this.doParse(parseBean);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void updatePlayerCfg() {
                PlayActivity.this.mVodInfo.playerCfg = PlayActivity.this.mVodPlayerCfg.toString();
                EventBus.getDefault().post(new RefreshEvent(0, PlayActivity.this.mVodPlayerCfg));
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void replay(boolean z) {
                PlayActivity.this.autoRetryCount = 0;
                PlayActivity.this.play(z);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void errReplay() {
                PlayActivity.this.errorWithRetry("视频播放出错", false);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void selectSubtitle() {
                try {
                    PlayActivity.this.selectMySubtitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void selectAudioTrack() {
                PlayActivity.this.selectMyAudioTrack();
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void openVideo() {
                PlayActivity.this.openMyVideo();
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void prepared() {
                PlayActivity.this.initSubtitleView();
            }
        });
        this.mVideoView.setVideoController(this.mController);
    }

    /* access modifiers changed from: package-private */
    public void setSubtitle(String str) {
        if (str != null && str.length() > 0) {
            this.mController.mSubtitleView.setVisibility(8);
            this.mController.mSubtitleView.setSubtitlePath(str);
            this.mController.mSubtitleView.setVisibility(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void selectMySubtitle() throws Exception {
        SubtitleDialog subtitleDialog = new SubtitleDialog(this.mContext);
        subtitleDialog.setSubtitleViewListener(new SubtitleDialog.SubtitleViewListener() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass4 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setTextSize(int i) {
                PlayActivity.this.mController.mSubtitleView.setTextSize((float) i);
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setSubtitleDelay(int i) {
                PlayActivity.this.mController.mSubtitleView.setSubtitleDelay(Integer.valueOf(i));
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void selectInternalSubtitle() {
                PlayActivity.this.selectMyInternalSubtitle();
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setTextStyle(int i) {
                PlayActivity.this.setSubtitleViewTextStyle(i);
            }
        });
        subtitleDialog.setSearchSubtitleListener(new SubtitleDialog.SearchSubtitleListener() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass5 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SearchSubtitleListener
            public void openSearchSubtitleDialog() {
                final SearchSubtitleDialog searchSubtitleDialog = new SearchSubtitleDialog(PlayActivity.this.mContext);
                searchSubtitleDialog.setSubtitleLoader(new SearchSubtitleDialog.SubtitleLoader() {
                    /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass5.AnonymousClass1 */

                    @Override // com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.SubtitleLoader
                    public void loadSubtitle(final Subtitle subtitle) {
                        PlayActivity.this.runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass5.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                String url = subtitle.getUrl();
                                LOG.i("Remote Subtitle Url: " + url);
                                PlayActivity.this.setSubtitle(url);
                                if (searchSubtitleDialog != null) {
                                    searchSubtitleDialog.dismiss();
                                }
                            }
                        });
                    }
                });
                if (PlayActivity.this.mVodInfo.playFlag.contains("Ali") || PlayActivity.this.mVodInfo.playFlag.contains("parse")) {
                    searchSubtitleDialog.setSearchWord(PlayActivity.this.mVodInfo.playNote);
                } else {
                    searchSubtitleDialog.setSearchWord(PlayActivity.this.mVodInfo.name);
                }
                searchSubtitleDialog.show();
            }
        });
        subtitleDialog.setLocalFileChooserListener(new SubtitleDialog.LocalFileChooserListener() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass6 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.LocalFileChooserListener
            public void openLocalFileChooserDialog() {
                new ChooserDialog((Activity) PlayActivity.this).withFilter(false, false, "srt", "ass", "scc", "stl", "ttml").withStartFile("/storage/emulated/0/Download").withChosenListener(new ChooserDialog.Result() {
                    /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass6.AnonymousClass1 */

                    @Override // com.obsez.android.lib.filechooser.ChooserDialog.Result
                    public void onChoosePath(String str, File file) {
                        LOG.i("Local Subtitle Path: " + str);
                        PlayActivity.this.setSubtitle(str);
                    }
                }).build().show();
            }
        });
        subtitleDialog.show();
    }

    /* access modifiers changed from: package-private */
    public void setSubtitleViewTextStyle(int i) {
        if (i == 0) {
            this.mController.mSubtitleView.setTextColor(getBaseContext().getResources().getColorStateList(2131034178));
            this.mController.mSubtitleView.setShadowLayer(3.0f, 2.0f, 2.0f, R.color.color_000000_80);
        } else if (i == 1) {
            this.mController.mSubtitleView.setTextColor(getBaseContext().getResources().getColorStateList(2131034176));
            this.mController.mSubtitleView.setShadowLayer(3.0f, 2.0f, 2.0f, 2131034178);
        }
    }

    /* access modifiers changed from: package-private */
    public void selectMyInternalSubtitle() {
        final AbstractPlayer mediaPlayer = this.mVideoView.getMediaPlayer();
        boolean z = mediaPlayer instanceof IjkMediaPlayer;
        if (z) {
            TrackInfo trackInfo = null;
            if (z) {
                trackInfo = ((IjkMediaPlayer) mediaPlayer).getTrackInfo();
            }
            final List<TrackInfoBean> subtitle = trackInfo.getSubtitle();
            if (subtitle.size() < 1) {
                Toast.makeText(this.mContext, getString(R.string.vod_sub_na), 0).show();
                return;
            }
            final SelectDialog selectDialog = new SelectDialog(this);
            selectDialog.setTip(getString(R.string.vod_sub_sel));
            selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<TrackInfoBean>() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass7 */

                public void click(TrackInfoBean trackInfoBean, int i) {
                    PlayActivity.this.mController.mSubtitleView.setVisibility(0);
                    try {
                        Iterator it = subtitle.iterator();
                        while (true) {
                            boolean z = true;
                            if (!it.hasNext()) {
                                break;
                            }
                            TrackInfoBean trackInfoBean2 = (TrackInfoBean) it.next();
                            if (trackInfoBean2.index != trackInfoBean.index) {
                                z = false;
                            }
                            trackInfoBean2.selected = z;
                        }
                        mediaPlayer.pause();
                        final long currentPosition = mediaPlayer.getCurrentPosition();
                        if (mediaPlayer instanceof IjkMediaPlayer) {
                            PlayActivity.this.mController.mSubtitleView.destroy();
                            PlayActivity.this.mController.mSubtitleView.clearSubtitleCache();
                            PlayActivity.this.mController.mSubtitleView.isInternal = true;
                            ((IjkMediaPlayer) mediaPlayer).setTrack(trackInfoBean.index);
                            new Handler().postDelayed(new Runnable() {
                                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass7.AnonymousClass1 */

                                public void run() {
                                    mediaPlayer.seekTo(currentPosition);
                                    mediaPlayer.start();
                                }
                            }, 800);
                        }
                        selectDialog.dismiss();
                    } catch (Exception unused) {
                        LOG.e("切换内置字幕出错");
                    }
                }

                public String getDisplay(TrackInfoBean trackInfoBean) {
                    return trackInfoBean.index + " : " + trackInfoBean.language;
                }
            }, new DiffUtil.ItemCallback<TrackInfoBean>() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass8 */

                public boolean areItemsTheSame(TrackInfoBean trackInfoBean, TrackInfoBean trackInfoBean2) {
                    return trackInfoBean.index == trackInfoBean2.index;
                }

                public boolean areContentsTheSame(TrackInfoBean trackInfoBean, TrackInfoBean trackInfoBean2) {
                    return trackInfoBean.index == trackInfoBean2.index;
                }
            }, subtitle, trackInfo.getSubtitleSelected(false));
            selectDialog.show();
        }
    }

    /* access modifiers changed from: package-private */
    public void selectMyAudioTrack() {
        final AbstractPlayer mediaPlayer = this.mVideoView.getMediaPlayer();
        boolean z = mediaPlayer instanceof IjkMediaPlayer;
        if (z) {
            TrackInfo trackInfo = null;
            if (z) {
                trackInfo = ((IjkMediaPlayer) mediaPlayer).getTrackInfo();
            }
            if (trackInfo == null) {
                Toast.makeText(this.mContext, getString(R.string.vod_no_audio), 0).show();
                return;
            }
            final List<TrackInfoBean> audio = trackInfo.getAudio();
            if (audio.size() >= 1) {
                final SelectDialog selectDialog = new SelectDialog(this);
                selectDialog.setTip(getString(R.string.vod_audio));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<TrackInfoBean>() {
                    /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass9 */

                    public void click(TrackInfoBean trackInfoBean, int i) {
                        try {
                            for (TrackInfoBean trackInfoBean2 : audio) {
                                trackInfoBean2.selected = trackInfoBean2.index == trackInfoBean.index;
                            }
                            mediaPlayer.pause();
                            final long currentPosition = mediaPlayer.getCurrentPosition();
                            AbstractPlayer abstractPlayer = mediaPlayer;
                            if (abstractPlayer instanceof IjkMediaPlayer) {
                                ((IjkMediaPlayer) abstractPlayer).setTrack(trackInfoBean.index);
                            }
                            new Handler().postDelayed(new Runnable() {
                                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass9.AnonymousClass1 */

                                public void run() {
                                    mediaPlayer.seekTo(currentPosition);
                                    mediaPlayer.start();
                                }
                            }, 800);
                            selectDialog.dismiss();
                        } catch (Exception unused) {
                            LOG.e("切换音轨出错");
                        }
                    }

                    public String getDisplay(TrackInfoBean trackInfoBean) {
                        String replace = trackInfoBean.name.replace("AUDIO,", "").replace("N/A,", "").replace(StringUtils.SPACE, "");
                        return trackInfoBean.index + " : " + trackInfoBean.language + " - " + replace;
                    }
                }, new DiffUtil.ItemCallback<TrackInfoBean>() {
                    /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass10 */

                    public boolean areItemsTheSame(TrackInfoBean trackInfoBean, TrackInfoBean trackInfoBean2) {
                        return trackInfoBean.index == trackInfoBean2.index;
                    }

                    public boolean areContentsTheSame(TrackInfoBean trackInfoBean, TrackInfoBean trackInfoBean2) {
                        return trackInfoBean.index == trackInfoBean2.index;
                    }
                }, audio, trackInfo.getAudioSelected(false));
                selectDialog.show();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void openMyVideo() {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.parse(this.videoURL), UrlHttpUtil.FILE_TYPE_VIDEO);
        startActivity(Intent.createChooser(intent, "Open Video with ..."));
    }

    /* access modifiers changed from: package-private */
    public void setTip(String str, boolean z, boolean z2) {
        try {
            this.mPlayLoadTip.setText(str);
            int i = 0;
            this.mPlayLoadTip.setVisibility(0);
            this.mPlayLoading.setVisibility(z ? 0 : 8);
            ImageView imageView = this.mPlayLoadErr;
            if (!z2) {
                i = 8;
            }
            imageView.setVisibility(i);
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: package-private */
    public void hideTip() {
        this.mPlayLoadTip.setVisibility(8);
        this.mPlayLoading.setVisibility(8);
        this.mPlayLoadErr.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void errorWithRetry(final String str, final boolean z) {
        if (!autoRetry()) {
            runOnUiThread(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass11 */

                public void run() {
                    if (z) {
                        Toast.makeText(PlayActivity.this.mContext, str, 0).show();
                        PlayActivity.this.finish();
                        return;
                    }
                    PlayActivity.this.setTip(str, false, true);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void playUrl(final String str, final HashMap<String, String> hashMap) {
        runOnUiThread(new Runnable() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass12 */

            public void run() {
                boolean z;
                PlayActivity.this.stopParse();
                if (PlayActivity.this.mVideoView != null) {
                    PlayActivity.this.mVideoView.release();
                    String str = str;
                    if (str != null) {
                        PlayActivity.this.videoURL = str;
                        try {
                            int i = PlayActivity.this.mVodPlayerCfg.getInt(an.az);
                            boolean z2 = false;
                            PlayActivity.this.extPlay = false;
                            if (i >= 10) {
                                String str2 = PlayActivity.this.mVodInfo.name + " : " + PlayActivity.this.mVodInfo.seriesMap.get(PlayActivity.this.mVodInfo.playFlag).get(PlayActivity.this.mVodInfo.playIndex).name;
                                PlayActivity.this.setTip("调用外部播放器" + PlayerHelper.getPlayerName(i) + "进行播放", true, false);
                                switch (i) {
                                    case 10:
                                        PlayActivity.this.extPlay = true;
                                        PlayActivity playActivity = PlayActivity.this;
                                        z = MXPlayer.run(playActivity, str, str2, playActivity.playSubtitle, hashMap);
                                        break;
                                    case 11:
                                        PlayActivity.this.extPlay = true;
                                        PlayActivity playActivity2 = PlayActivity.this;
                                        z = ReexPlayer.run(playActivity2, str, str2, playActivity2.playSubtitle, hashMap);
                                        break;
                                    case 12:
                                        PlayActivity.this.extPlay = true;
                                        PlayActivity playActivity3 = PlayActivity.this;
                                        z = Kodi.run(playActivity3, str, str2, playActivity3.playSubtitle, hashMap);
                                        break;
                                    default:
                                        z = false;
                                        break;
                                }
                                PlayActivity playActivity4 = PlayActivity.this;
                                StringBuilder sb = new StringBuilder();
                                sb.append("调用外部播放器");
                                sb.append(PlayerHelper.getPlayerName(i));
                                sb.append(z ? "成功" : "失败");
                                String sb2 = sb.toString();
                                if (!z) {
                                    z2 = true;
                                }
                                playActivity4.setTip(sb2, z, z2);
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        PlayActivity.this.hideTip();
                        PlayerHelper.updateCfg(PlayActivity.this.mVideoView, PlayActivity.this.mVodPlayerCfg);
                        PlayActivity.this.mVideoView.setProgressKey(PlayActivity.this.progressKey);
                        if (hashMap != null) {
                            PlayActivity.this.mVideoView.setUrl(str, hashMap);
                        } else {
                            PlayActivity.this.mVideoView.setUrl(str);
                        }
                        PlayActivity.this.mVideoView.start();
                        PlayActivity.this.mController.resetSpeed();
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSubtitleView() {
        if (this.mVideoView.getMediaPlayer() instanceof IjkMediaPlayer) {
            TrackInfo trackInfo = ((IjkMediaPlayer) this.mVideoView.getMediaPlayer()).getTrackInfo();
            if (trackInfo != null && trackInfo.getSubtitle().size() > 0) {
                this.mController.mSubtitleView.hasInternal = true;
            }
            ((IjkMediaPlayer) this.mVideoView.getMediaPlayer()).setOnTimedTextListener(new IMediaPlayer.OnTimedTextListener() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass13 */

                @Override // tv.danmaku.ijk.media.player.IMediaPlayer.OnTimedTextListener
                public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
                    if (PlayActivity.this.mController.mSubtitleView.isInternal) {
                        com.github.tvbox.osc.subtitle.model.Subtitle subtitle = new com.github.tvbox.osc.subtitle.model.Subtitle();
                        subtitle.content = ijkTimedText.getText();
                        PlayActivity.this.mController.mSubtitleView.onSubtitleChanged(subtitle);
                    }
                }
            });
        }
        this.mController.mSubtitleView.bindToMediaPlayer(this.mVideoView.getMediaPlayer());
        this.mController.mSubtitleView.setPlaySubtitleCacheKey(this.subtitleCacheKey);
        String str = (String) CacheManager.getCache(MD5.string2MD5(this.subtitleCacheKey));
        if (str == null || str.isEmpty()) {
            String str2 = this.playSubtitle;
            if (str2 != null && str2.length() > 0) {
                this.mController.mSubtitleView.setSubtitlePath(this.playSubtitle);
            } else if (this.mController.mSubtitleView.hasInternal) {
                this.mController.mSubtitleView.isInternal = true;
            }
        } else {
            this.mController.mSubtitleView.setSubtitlePath(str);
        }
    }

    private void initViewModel() {
        SourceViewModel sourceViewModel2 = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
        this.sourceViewModel = sourceViewModel2;
        sourceViewModel2.playResult.observe(this, new Observer<JSONObject>() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass14 */

            public void onChanged(JSONObject jSONObject) {
                if (jSONObject != null) {
                    try {
                        HashMap<String, String> hashMap = null;
                        PlayActivity.this.progressKey = jSONObject.optString("proKey", null);
                        boolean equals = jSONObject.optString("parse", "1").equals("1");
                        boolean equals2 = jSONObject.optString("jx", SessionDescription.SUPPORTED_SDP_VERSION).equals("1");
                        PlayActivity.this.playSubtitle = jSONObject.optString("subt", "");
                        PlayActivity.this.subtitleCacheKey = jSONObject.optString("subtKey", null);
                        String optString = jSONObject.optString("playUrl", "");
                        String optString2 = jSONObject.optString("flag");
                        String string = jSONObject.getString("url");
                        PlayActivity.this.webUserAgent = null;
                        PlayActivity.this.webHeaderMap = null;
                        if (jSONObject.has("header")) {
                            try {
                                JSONObject jSONObject2 = new JSONObject(jSONObject.getString("header"));
                                Iterator<String> keys = jSONObject2.keys();
                                while (keys.hasNext()) {
                                    String next = keys.next();
                                    if (hashMap == null) {
                                        hashMap = new HashMap<>();
                                    }
                                    hashMap.put(next, jSONObject2.getString(next));
                                    if (next.equalsIgnoreCase(RtspHeaders.USER_AGENT)) {
                                        PlayActivity.this.webUserAgent = jSONObject2.getString(next).trim();
                                    }
                                }
                                PlayActivity.this.webHeaderMap = hashMap;
                            } catch (Throwable unused) {
                            }
                        }
                        boolean z = false;
                        if (equals || equals2) {
                            if ((optString.isEmpty() && ApiConfig.get().getVipParseFlags().contains(optString2)) || equals2) {
                                z = true;
                            }
                            PlayActivity.this.initParse(optString2, z, optString, string);
                            return;
                        }
                        PlayActivity.this.mController.showParse(false);
                        PlayActivity playActivity = PlayActivity.this;
                        playActivity.playUrl(optString + string, hashMap);
                    } catch (Throwable unused2) {
                        PlayActivity.this.errorWithRetry("获取播放信息错误", true);
                    }
                } else {
                    PlayActivity.this.errorWithRetry("获取播放信息错误", true);
                }
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            this.mVodInfo = (VodInfo) extras.getSerializable("VodInfo");
            this.sourceKey = extras.getString("sourceKey");
            this.sourceBean = ApiConfig.get().getSource(this.sourceKey);
            initPlayerCfg();
            play(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void initPlayerCfg() {
        try {
            this.mVodPlayerCfg = new JSONObject(this.mVodInfo.playerCfg);
        } catch (Throwable unused) {
            this.mVodPlayerCfg = new JSONObject();
        }
        try {
            if (!this.mVodPlayerCfg.has(an.az)) {
                this.mVodPlayerCfg.put(an.az, this.sourceBean.getPlayerType() == -1 ? ((Integer) Hawk.get(HawkConfig.PLAY_TYPE, 1)).intValue() : this.sourceBean.getPlayerType());
            }
            if (!this.mVodPlayerCfg.has("pr")) {
                this.mVodPlayerCfg.put("pr", Hawk.get(HawkConfig.PLAY_RENDER, 0));
            }
            if (!this.mVodPlayerCfg.has("ijk")) {
                this.mVodPlayerCfg.put("ijk", Hawk.get(HawkConfig.IJK_CODEC, ""));
            }
            if (!this.mVodPlayerCfg.has("sc")) {
                this.mVodPlayerCfg.put("sc", Hawk.get(HawkConfig.PLAY_SCALE, 0));
            }
            if (!this.mVodPlayerCfg.has("sp")) {
                this.mVodPlayerCfg.put("sp", 1.0d);
            }
            if (!this.mVodPlayerCfg.has("st")) {
                this.mVodPlayerCfg.put("st", 0);
            }
            if (!this.mVodPlayerCfg.has("et")) {
                this.mVodPlayerCfg.put("et", 0);
            }
        } catch (Throwable unused2) {
        }
        this.mController.setPlayerConfig(this.mVodPlayerCfg);
    }

    /* access modifiers changed from: package-private */
    public void initPlayerDrive() {
        try {
            if (!this.mVodPlayerCfg.has(an.az)) {
                this.mVodPlayerCfg.put(an.az, Hawk.get(HawkConfig.PLAY_TYPE, 1));
            }
            if (!this.mVodPlayerCfg.has("pr")) {
                this.mVodPlayerCfg.put("pr", Hawk.get(HawkConfig.PLAY_RENDER, 0));
            }
            if (!this.mVodPlayerCfg.has("ijk")) {
                this.mVodPlayerCfg.put("ijk", Hawk.get(HawkConfig.IJK_CODEC, ""));
            }
            if (!this.mVodPlayerCfg.has("sc")) {
                this.mVodPlayerCfg.put("sc", Hawk.get(HawkConfig.PLAY_SCALE, 0));
            }
            if (!this.mVodPlayerCfg.has("sp")) {
                this.mVodPlayerCfg.put("sp", 1.0d);
            }
            if (!this.mVodPlayerCfg.has("st")) {
                this.mVodPlayerCfg.put("st", 0);
            }
            if (!this.mVodPlayerCfg.has("et")) {
                this.mVodPlayerCfg.put("et", 0);
            }
        } catch (Throwable unused) {
        }
        this.mController.setPlayerConfig(this.mVodPlayerCfg);
    }

    public void onUserLeaveHint() {
        Rational rational;
        if (supportsPiPMode() && !this.extPlay && this.PiPON) {
            int i = this.mVideoView.getVideoSize()[0];
            int i2 = this.mVideoView.getVideoSize()[1];
            if (i != 0) {
                double d = (double) i;
                double d2 = (double) i2;
                Double.isNaN(d);
                Double.isNaN(d2);
                if (d / d2 > 2.39d) {
                    Double.isNaN(d);
                    i2 = (int) (d / 2.35d);
                }
                rational = new Rational(i, i2);
            } else {
                rational = new Rational(16, 9);
            }
            ArrayList arrayList = new ArrayList();
            arrayList.add(generateRemoteAction(17301541, 0, "Prev", "Play Previous"));
            arrayList.add(generateRemoteAction(17301540, 1, "Play/Pause", "Play or Pause"));
            arrayList.add(generateRemoteAction(17301538, 2, "Next", "Play Next"));
            enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(rational).setActions(arrayList).build());
            this.mController.hideBottom();
        }
        super.onUserLeaveHint();
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        if (!this.mController.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null || !this.mController.onKeyEvent(keyEvent)) {
            return super.dispatchKeyEvent(keyEvent);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onResume() {
        super.onResume();
        MyVideoView myVideoView = this.mVideoView;
        if (myVideoView != null) {
            this.onStopCalled = false;
            myVideoView.resume();
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

    private RemoteAction generateRemoteAction(int i, int i2, String str, String str2) {
        return new RemoteAction(Icon.createWithResource(this, i), str, str2, PendingIntent.getBroadcast(this, i2, new Intent("PIP_VOD_CONTROL").putExtra("action", i2), 0));
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onPictureInPictureModeChanged(boolean z) {
        super.onPictureInPictureModeChanged(z);
        if (!supportsPiPMode() || !z) {
            if (this.onStopCalled) {
                this.mVideoView.release();
            }
            unregisterReceiver(this.pipActionReceiver);
            this.pipActionReceiver = null;
            return;
        }
        AnonymousClass15 r3 = new BroadcastReceiver() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass15 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals("PIP_VOD_CONTROL") && PlayActivity.this.mController != null) {
                    int intExtra = intent.getIntExtra("action", 1);
                    if (intExtra == 0) {
                        PlayActivity.this.playPrevious();
                    } else if (intExtra == 1) {
                        PlayActivity.this.mController.togglePlay();
                    } else if (intExtra == 2) {
                        PlayActivity.this.playNext(false);
                    }
                }
            }
        };
        this.pipActionReceiver = r3;
        registerReceiver(r3, new IntentFilter("PIP_VOD_CONTROL"));
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        MyVideoView myVideoView = this.mVideoView;
        if (myVideoView != null) {
            myVideoView.release();
            this.mVideoView = null;
        }
        stopLoadWebView(true);
        stopParse();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playNext(boolean z) {
        boolean z2;
        VodInfo vodInfo = this.mVodInfo;
        if (vodInfo == null || vodInfo.seriesMap.get(this.mVodInfo.playFlag) == null || (!this.mVodInfo.reverseSort ? this.mVodInfo.playIndex + 1 >= this.mVodInfo.seriesMap.get(this.mVodInfo.playFlag).size() : this.mVodInfo.playIndex - 1 < 0)) {
            z2 = false;
        } else {
            z2 = true;
        }
        if (!z2) {
            Toast.makeText(this, "已经是最后一集了", 0).show();
            if (z) {
                finish();
                return;
            }
            return;
        }
        if (this.mVodInfo.reverseSort) {
            this.mVodInfo.playIndex--;
        } else {
            this.mVodInfo.playIndex++;
        }
        play(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playPrevious() {
        boolean z;
        VodInfo vodInfo = this.mVodInfo;
        if (vodInfo == null || vodInfo.seriesMap.get(this.mVodInfo.playFlag) == null || (!this.mVodInfo.reverseSort ? this.mVodInfo.playIndex - 1 < 0 : this.mVodInfo.playIndex + 1 >= this.mVodInfo.seriesMap.get(this.mVodInfo.playFlag).size())) {
            z = false;
        } else {
            z = true;
        }
        if (!z) {
            Toast.makeText(this, "已经是第一集了", 0).show();
            return;
        }
        if (this.mVodInfo.reverseSort) {
            this.mVodInfo.playIndex++;
        } else {
            this.mVodInfo.playIndex--;
        }
        play(false);
    }

    /* access modifiers changed from: package-private */
    public boolean autoRetry() {
        int i = this.autoRetryCount;
        if (i < 3) {
            this.autoRetryCount = i + 1;
            play(false);
            return true;
        }
        this.autoRetryCount = 0;
        return false;
    }

    public void play(boolean z) {
        VodInfo.VodSeries vodSeries = this.mVodInfo.seriesMap.get(this.mVodInfo.playFlag).get(this.mVodInfo.playIndex);
        EventBus.getDefault().post(new RefreshEvent(0, Integer.valueOf(this.mVodInfo.playIndex)));
        setTip("正在获取播放信息", true, false);
        this.mController.setTitle(this.mVodInfo.name + " : " + vodSeries.name);
        stopParse();
        MyVideoView myVideoView = this.mVideoView;
        if (myVideoView != null) {
            myVideoView.release();
        }
        String str = this.mVodInfo.sourceKey + "-" + this.mVodInfo.id + "-" + this.mVodInfo.playFlag + "-" + this.mVodInfo.playIndex + "-" + vodSeries.name + "-subt";
        String str2 = this.mVodInfo.sourceKey + this.mVodInfo.id + this.mVodInfo.playFlag + this.mVodInfo.playIndex;
        if (z) {
            CacheManager.delete(MD5.string2MD5(str2), 0);
            CacheManager.delete(MD5.string2MD5(str), "");
        }
        if (vodSeries.url.startsWith("tvbox-drive://")) {
            initPlayerDrive();
            this.mController.showParse(false);
            HashMap<String, String> hashMap = null;
            if (this.mVodInfo.playerCfg != null && this.mVodInfo.playerCfg.length() > 0) {
                JsonObject asJsonObject = JsonParser.parseString(this.mVodInfo.playerCfg).getAsJsonObject();
                if (asJsonObject.has("headers")) {
                    hashMap = new HashMap<>();
                    Iterator<JsonElement> it = asJsonObject.getAsJsonArray("headers").iterator();
                    while (it.hasNext()) {
                        JsonObject asJsonObject2 = it.next().getAsJsonObject();
                        hashMap.put(asJsonObject2.get(SerializableCookie.NAME).getAsString(), asJsonObject2.get("value").getAsString());
                    }
                }
            }
            playUrl(vodSeries.url.replace("tvbox-drive://", ""), hashMap);
        } else if (Thunder.play(vodSeries.url, new Thunder.ThunderCallback() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass16 */

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void list(String str) {
            }

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void status(int i, String str) {
                if (i < 0) {
                    PlayActivity.this.setTip(str, false, true);
                } else {
                    PlayActivity.this.setTip(str, true, false);
                }
            }

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void play(String str) {
                PlayActivity.this.playUrl(str, null);
            }
        })) {
            this.mController.showParse(false);
        } else {
            this.sourceViewModel.getPlay(this.sourceKey, this.mVodInfo.playFlag, str2, vodSeries.url, str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initParse(String str, boolean z, String str2, String str3) {
        ParseBean parseBean;
        this.parseFlag = str;
        this.webUrl = str3;
        this.mController.showParse(z);
        if (z) {
            parseBean = ApiConfig.get().getDefaultParse();
        } else {
            if (str2.startsWith("json:")) {
                parseBean = new ParseBean();
                parseBean.setType(1);
                parseBean.setUrl(str2.substring(5));
            } else {
                if (str2.startsWith("parse:")) {
                    String substring = str2.substring(6);
                    Iterator<ParseBean> it = ApiConfig.get().getParseBeanList().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ParseBean next = it.next();
                        if (next.getName().equals(substring)) {
                            parseBean = next;
                            break;
                        }
                    }
                }
                parseBean = null;
            }
            if (parseBean == null) {
                parseBean = new ParseBean();
                parseBean.setType(0);
                parseBean.setUrl(str2);
            }
        }
        this.loadFound = false;
        doParse(parseBean);
    }

    /* access modifiers changed from: package-private */
    public JSONObject jsonParse(String str, String str2) throws JSONException {
        String str3;
        JSONObject jSONObject = new JSONObject(str2);
        if (jSONObject.has("data")) {
            str3 = jSONObject.getJSONObject("data").getString("url");
        } else {
            str3 = jSONObject.getString("url");
        }
        jSONObject.optString(NotificationCompat.CATEGORY_MESSAGE, "");
        if (str3.startsWith("//")) {
            str3 = "https:" + str3;
        }
        if (!str3.startsWith("http")) {
            return null;
        }
        JSONObject jSONObject2 = new JSONObject();
        String optString = jSONObject.optString(RtspHeaders.USER_AGENT, "");
        if (optString.trim().length() > 0) {
            jSONObject2.put("User-Agent", StringUtils.SPACE + optString);
        }
        String optString2 = jSONObject.optString("referer", "");
        if (optString2.trim().length() > 0) {
            jSONObject2.put(HttpHeaders.REFERER, StringUtils.SPACE + optString2);
        }
        JSONObject jSONObject3 = new JSONObject();
        jSONObject3.put("header", jSONObject2);
        jSONObject3.put("url", str3);
        return jSONObject3;
    }

    /* access modifiers changed from: package-private */
    public void stopParse() {
        this.mHandler.removeMessages(100);
        stopLoadWebView(false);
        this.loadFound = false;
        OkGo.getInstance().cancelTag("json_jx");
        ExecutorService executorService = this.parseThreadPool;
        if (executorService != null) {
            try {
                executorService.shutdown();
                this.parseThreadPool = null;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doParse(final ParseBean parseBean) {
        stopParse();
        if (parseBean.getType() == 0) {
            setTip("正在嗅探播放地址", true, false);
            this.mHandler.removeMessages(100);
            this.mHandler.sendEmptyMessageDelayed(100, SilenceSkippingAudioProcessor.DEFAULT_PADDING_SILENCE_US);
            loadWebView(parseBean.getUrl() + this.webUrl);
        } else if (parseBean.getType() == 1) {
            setTip("正在解析播放地址", true, false);
            com.lzy.okgo.model.HttpHeaders httpHeaders = new com.lzy.okgo.model.HttpHeaders();
            try {
                JSONObject jSONObject = new JSONObject(parseBean.getExt());
                if (jSONObject.has("header")) {
                    JSONObject optJSONObject = jSONObject.optJSONObject("header");
                    Iterator<String> keys = optJSONObject.keys();
                    while (keys.hasNext()) {
                        String next = keys.next();
                        httpHeaders.put(next, optJSONObject.optString(next, ""));
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            ((GetRequest) ((GetRequest) OkGo.get(parseBean.getUrl() + this.webUrl).tag("json_jx")).headers(httpHeaders)).execute(new AbsCallback<String>() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass17 */

                @Override // com.lzy.okgo.convert.Converter
                public String convertResponse(Response response) throws Throwable {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                    throw new IllegalStateException("网络请求错误");
                }

                @Override // com.lzy.okgo.callback.Callback
                public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                    String body = response.body();
                    try {
                        PlayActivity playActivity = PlayActivity.this;
                        JSONObject jsonParse = playActivity.jsonParse(playActivity.webUrl, body);
                        HashMap<String, String> hashMap = null;
                        if (jsonParse.has("header")) {
                            try {
                                JSONObject jSONObject = jsonParse.getJSONObject("header");
                                Iterator<String> keys = jSONObject.keys();
                                while (keys.hasNext()) {
                                    String next = keys.next();
                                    if (hashMap == null) {
                                        hashMap = new HashMap<>();
                                    }
                                    hashMap.put(next, jSONObject.getString(next));
                                }
                            } catch (Throwable unused) {
                            }
                        }
                        PlayActivity.this.playUrl(jsonParse.getString("url"), hashMap);
                    } catch (Throwable th) {
                        th.printStackTrace();
                        PlayActivity.this.errorWithRetry("解析错误", false);
                    }
                }

                @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                public void onError(com.lzy.okgo.model.Response<String> response) {
                    super.onError(response);
                    PlayActivity.this.errorWithRetry("解析错误", false);
                }
            });
        } else if (parseBean.getType() == 2) {
            setTip("正在解析播放地址", true, false);
            this.parseThreadPool = Executors.newSingleThreadExecutor();
            final LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (ParseBean parseBean2 : ApiConfig.get().getParseBeanList()) {
                if (parseBean2.getType() == 1) {
                    linkedHashMap.put(parseBean2.getName(), parseBean2.mixUrl());
                }
            }
            this.parseThreadPool.execute(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass18 */

                public void run() {
                    final JSONObject jsonExt = ApiConfig.get().jsonExt(parseBean.getUrl(), linkedHashMap, PlayActivity.this.webUrl);
                    boolean z = false;
                    if (jsonExt == null || !jsonExt.has("url")) {
                        PlayActivity.this.errorWithRetry("解析错误", false);
                        return;
                    }
                    HashMap<String, String> hashMap = null;
                    if (jsonExt.has("header")) {
                        try {
                            JSONObject jSONObject = jsonExt.getJSONObject("header");
                            Iterator<String> keys = jSONObject.keys();
                            while (keys.hasNext()) {
                                String next = keys.next();
                                if (hashMap == null) {
                                    hashMap = new HashMap<>();
                                }
                                hashMap.put(next, jSONObject.getString(next));
                            }
                        } catch (Throwable unused) {
                        }
                    }
                    if (jsonExt.has("jxFrom")) {
                        PlayActivity.this.runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass18.AnonymousClass1 */

                            public void run() {
                                Context context = PlayActivity.this.mContext;
                                Toast.makeText(context, "解析来自:" + jsonExt.optString("jxFrom"), 0).show();
                            }
                        });
                    }
                    if (jsonExt.optInt("parse", 0) == 1) {
                        z = true;
                    }
                    if (z) {
                        PlayActivity.this.loadUrl(DefaultConfig.checkReplaceProxy(jsonExt.optString("url", "")));
                        return;
                    }
                    PlayActivity.this.playUrl(jsonExt.optString("url", ""), hashMap);
                }
            });
        } else if (parseBean.getType() == 3) {
            setTip("正在解析播放地址", true, false);
            this.parseThreadPool = Executors.newSingleThreadExecutor();
            final LinkedHashMap linkedHashMap2 = new LinkedHashMap();
            final String str = "";
            for (ParseBean parseBean3 : ApiConfig.get().getParseBeanList()) {
                HashMap hashMap = new HashMap();
                hashMap.put("url", parseBean3.getUrl());
                if (parseBean3.getUrl().equals(parseBean.getUrl())) {
                    str = parseBean3.getName();
                }
                hashMap.put("type", parseBean3.getType() + "");
                hashMap.put("ext", parseBean3.getExt());
                linkedHashMap2.put(parseBean3.getName(), hashMap);
            }
            this.parseThreadPool.execute(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass19 */

                public void run() {
                    ApiConfig apiConfig = ApiConfig.get();
                    final JSONObject jsonExtMix = apiConfig.jsonExtMix(PlayActivity.this.parseFlag + "111", parseBean.getUrl(), str, linkedHashMap2, PlayActivity.this.webUrl);
                    if (jsonExtMix == null || !jsonExtMix.has("url")) {
                        PlayActivity.this.errorWithRetry("解析错误", false);
                    } else if (!jsonExtMix.has("parse") || jsonExtMix.optInt("parse", 0) != 1) {
                        HashMap<String, String> hashMap = null;
                        if (jsonExtMix.has("header")) {
                            try {
                                JSONObject jSONObject = jsonExtMix.getJSONObject("header");
                                Iterator<String> keys = jSONObject.keys();
                                while (keys.hasNext()) {
                                    String next = keys.next();
                                    if (hashMap == null) {
                                        hashMap = new HashMap<>();
                                    }
                                    hashMap.put(next, jSONObject.getString(next));
                                }
                            } catch (Throwable unused) {
                            }
                        }
                        if (jsonExtMix.has("jxFrom")) {
                            PlayActivity.this.runOnUiThread(new Runnable() {
                                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass19.AnonymousClass2 */

                                public void run() {
                                    Context context = PlayActivity.this.mContext;
                                    Toast.makeText(context, "解析来自:" + jsonExtMix.optString("jxFrom"), 0).show();
                                }
                            });
                        }
                        PlayActivity.this.playUrl(jsonExtMix.optString("url", ""), hashMap);
                    } else {
                        PlayActivity.this.runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass19.AnonymousClass1 */

                            public void run() {
                                String checkReplaceProxy = DefaultConfig.checkReplaceProxy(jsonExtMix.optString("url", ""));
                                PlayActivity.this.stopParse();
                                PlayActivity.this.setTip("正在嗅探播放地址", true, false);
                                PlayActivity.this.mHandler.removeMessages(100);
                                PlayActivity.this.mHandler.sendEmptyMessageDelayed(100, SilenceSkippingAudioProcessor.DEFAULT_PADDING_SILENCE_US);
                                PlayActivity.this.loadWebView(checkReplaceProxy);
                            }
                        });
                    }
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void loadWebView(final String str) {
        if (this.mSysWebView != null || this.mXwalkWebView != null) {
            loadUrl(str);
        } else if (!((Boolean) Hawk.get(HawkConfig.PARSE_WEBVIEW, true)).booleanValue()) {
            XWalkUtils.tryUseXWalk(this.mContext, new XWalkUtils.XWalkState() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass20 */

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void success() {
                    PlayActivity.this.initWebView(false);
                    PlayActivity.this.loadUrl(str);
                }

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void fail() {
                    Toast.makeText(PlayActivity.this.mContext, "XWalkView不兼容，已替换为系统自带WebView", 0).show();
                    PlayActivity.this.initWebView(true);
                    PlayActivity.this.loadUrl(str);
                }

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void ignore() {
                    Toast.makeText(PlayActivity.this.mContext, "XWalkView运行组件未下载，已替换为系统自带WebView", 0).show();
                    PlayActivity.this.initWebView(true);
                    PlayActivity.this.loadUrl(str);
                }
            });
        } else {
            initWebView(true);
            loadUrl(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void initWebView(boolean z) {
        if (z) {
            MyWebView myWebView = new MyWebView(this.mContext);
            this.mSysWebView = myWebView;
            configWebViewSys(myWebView);
            return;
        }
        MyXWalkView myXWalkView = new MyXWalkView(this.mContext);
        this.mXwalkWebView = myXWalkView;
        configWebViewX5(myXWalkView);
    }

    /* access modifiers changed from: package-private */
    public void loadUrl(final String str) {
        runOnUiThread(new Runnable() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass21 */

            public void run() {
                if (PlayActivity.this.mXwalkWebView != null) {
                    PlayActivity.this.mXwalkWebView.stopLoading();
                    new HashMap();
                    if (PlayActivity.this.webUserAgent != null) {
                        PlayActivity.this.mXwalkWebView.getSettings().setUserAgentString(PlayActivity.this.webUserAgent);
                    }
                    if (PlayActivity.this.webHeaderMap != null) {
                        PlayActivity.this.mXwalkWebView.loadUrl(str, PlayActivity.this.webHeaderMap);
                    } else {
                        PlayActivity.this.mXwalkWebView.loadUrl(str);
                    }
                }
                if (PlayActivity.this.mSysWebView != null) {
                    PlayActivity.this.mSysWebView.stopLoading();
                    if (PlayActivity.this.webUserAgent != null) {
                        PlayActivity.this.mSysWebView.getSettings().setUserAgentString(PlayActivity.this.webUserAgent);
                    }
                    if (PlayActivity.this.webHeaderMap != null) {
                        PlayActivity.this.mSysWebView.loadUrl(str, PlayActivity.this.webHeaderMap);
                    } else {
                        PlayActivity.this.mSysWebView.loadUrl(str);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void stopLoadWebView(final boolean z) {
        runOnUiThread(new Runnable() {
            /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass22 */

            public void run() {
                if (PlayActivity.this.mXwalkWebView != null) {
                    PlayActivity.this.mXwalkWebView.stopLoading();
                    PlayActivity.this.mXwalkWebView.loadUrl("about:blank");
                    if (z) {
                        PlayActivity.this.mXwalkWebView.removeAllViews();
                        PlayActivity.this.mXwalkWebView.onDestroy();
                        PlayActivity.this.mXwalkWebView = null;
                    }
                }
                if (PlayActivity.this.mSysWebView != null) {
                    PlayActivity.this.mSysWebView.stopLoading();
                    PlayActivity.this.mSysWebView.loadUrl("about:blank");
                    if (z) {
                        ViewGroup viewGroup = (ViewGroup) PlayActivity.this.mSysWebView.getParent();
                        if (viewGroup != null) {
                            viewGroup.removeView(PlayActivity.this.mSysWebView);
                        }
                        PlayActivity.this.mSysWebView.removeAllViews();
                        PlayActivity.this.mSysWebView.destroy();
                        PlayActivity.this.mSysWebView = null;
                    }
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean checkVideoFormat(String str) {
        Spider csp;
        if (this.sourceBean.getType() != 3 || (csp = ApiConfig.get().getCSP(this.sourceBean)) == null || !csp.manualVideoCheck()) {
            return DefaultConfig.isVideoFormat(str);
        }
        return csp.isVideoFormat(str);
    }

    /* access modifiers changed from: package-private */
    public class MyWebView extends WebView {
        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            return false;
        }

        public MyWebView(Context context) {
            super(context);
        }

        public void setOverScrollMode(int i) {
            super.setOverScrollMode(i);
            if (PlayActivity.this.mContext instanceof Activity) {
                AutoSize.autoConvertDensityOfCustomAdapt((Activity) PlayActivity.this.mContext, PlayActivity.this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MyXWalkView extends XWalkView {
        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            return false;
        }

        public MyXWalkView(Context context) {
            super(context);
        }

        public void setOverScrollMode(int i) {
            super.setOverScrollMode(i);
            if (PlayActivity.this.mContext instanceof Activity) {
                AutoSize.autoConvertDensityOfCustomAdapt((Activity) PlayActivity.this.mContext, PlayActivity.this);
            }
        }
    }

    private void configWebViewSys(WebView webView) {
        ViewGroup.LayoutParams layoutParams;
        if (webView != null) {
            if (((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue()) {
                layoutParams = new ViewGroup.LayoutParams(800, 400);
            } else {
                layoutParams = new ViewGroup.LayoutParams(1, 1);
            }
            webView.setFocusable(false);
            webView.setFocusableInTouchMode(false);
            webView.clearFocus();
            webView.setOverScrollMode(0);
            addContentView(webView, layoutParams);
            WebSettings settings = webView.getSettings();
            settings.setNeedInitialFocus(false);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccess(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setDatabaseEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT >= 17) {
                settings.setMediaPlaybackRequiresUserGesture(false);
            }
            settings.setBlockNetworkImage(!((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue());
            settings.setUseWideViewPort(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(false);
            settings.setLoadWithOverviewMode(true);
            settings.setBuiltInZoomControls(true);
            settings.setSupportZoom(false);
            if (Build.VERSION.SDK_INT >= 21) {
                settings.setMixedContentMode(0);
            }
            settings.setCacheMode(-1);
            settings.setDefaultTextEncodingName("utf-8");
            settings.setUserAgentString(webView.getSettings().getUserAgentString());
            webView.setWebChromeClient(new WebChromeClient() {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass23 */

                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    return false;
                }

                public boolean onJsAlert(WebView webView, String str, String str2, JsResult jsResult) {
                    return true;
                }

                public boolean onJsConfirm(WebView webView, String str, String str2, JsResult jsResult) {
                    return true;
                }

                public boolean onJsPrompt(WebView webView, String str, String str2, String str3, JsPromptResult jsPromptResult) {
                    return true;
                }
            });
            SysWebClient sysWebClient = new SysWebClient();
            this.mSysWebClient = sysWebClient;
            webView.setWebViewClient(sysWebClient);
            webView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        }
    }

    /* access modifiers changed from: private */
    public class SysWebClient extends WebViewClient {
        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return false;
        }

        private SysWebClient() {
        }

        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        /* access modifiers changed from: package-private */
        public WebResourceResponse checkIsVideo(String str, HashMap<String, String> hashMap) {
            boolean z;
            if (str.endsWith("/favicon.ico")) {
                return new WebResourceResponse("image/png", null, null);
            }
            LOG.i("shouldInterceptRequest url:" + str);
            if (!PlayActivity.this.loadedUrls.containsKey(str)) {
                z = AdBlocker.isAd(str);
                PlayActivity.this.loadedUrls.put(str, Boolean.valueOf(z));
            } else {
                z = ((Boolean) PlayActivity.this.loadedUrls.get(str)).booleanValue();
            }
            if (!z && !PlayActivity.this.loadFound && PlayActivity.this.checkVideoFormat(str)) {
                PlayActivity.this.mHandler.removeMessages(100);
                PlayActivity.this.loadFound = true;
                if (hashMap == null || hashMap.isEmpty()) {
                    PlayActivity.this.playUrl(str, null);
                } else {
                    PlayActivity.this.playUrl(str, hashMap);
                }
                String cookie = CookieManager.getInstance().getCookie(str);
                if (!TextUtils.isEmpty(cookie)) {
                    hashMap.put("Cookie", StringUtils.SPACE + cookie);
                }
                PlayActivity.this.playUrl(str, hashMap);
                PlayActivity.this.stopLoadWebView(false);
            }
            if (z || PlayActivity.this.loadFound) {
                return AdBlocker.createEmptyResource();
            }
            return null;
        }

        @Override // android.webkit.WebViewClient
        public WebResourceResponse shouldInterceptRequest(WebView webView, String str) {
            WebResourceResponse checkIsVideo = checkIsVideo(str, new HashMap<>());
            return checkIsVideo == null ? super.shouldInterceptRequest(webView, str) : checkIsVideo;
        }

        @Override // android.webkit.WebViewClient
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            String str;
            try {
                str = webResourceRequest.getUrl().toString();
            } catch (Throwable unused) {
                str = "";
            }
            HashMap<String, String> hashMap = new HashMap<>();
            try {
                Map<String, String> requestHeaders = webResourceRequest.getRequestHeaders();
                for (String str2 : requestHeaders.keySet()) {
                    if (str2.equalsIgnoreCase(RtspHeaders.USER_AGENT) || str2.equalsIgnoreCase("referer") || str2.equalsIgnoreCase("origin")) {
                        hashMap.put(str2, StringUtils.SPACE + requestHeaders.get(str2));
                    }
                }
            } catch (Throwable unused2) {
            }
            WebResourceResponse checkIsVideo = checkIsVideo(str, hashMap);
            return checkIsVideo == null ? super.shouldInterceptRequest(webView, webResourceRequest) : checkIsVideo;
        }

        public void onLoadResource(WebView webView, String str) {
            super.onLoadResource(webView, str);
        }
    }

    private void configWebViewX5(XWalkView xWalkView) {
        ViewGroup.LayoutParams layoutParams;
        if (xWalkView != null) {
            if (((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue()) {
                layoutParams = new ViewGroup.LayoutParams(800, 400);
            } else {
                layoutParams = new ViewGroup.LayoutParams(1, 1);
            }
            xWalkView.setFocusable(false);
            xWalkView.setFocusableInTouchMode(false);
            xWalkView.clearFocus();
            xWalkView.setOverScrollMode(0);
            addContentView(xWalkView, layoutParams);
            XWalkSettings settings = xWalkView.getSettings();
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccess(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setDatabaseEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setBlockNetworkImage(!((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue());
            if (Build.VERSION.SDK_INT >= 17) {
                settings.setMediaPlaybackRequiresUserGesture(false);
            }
            settings.setUseWideViewPort(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(false);
            settings.setLoadWithOverviewMode(true);
            settings.setBuiltInZoomControls(true);
            settings.setSupportZoom(false);
            settings.setCacheMode(-1);
            xWalkView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            xWalkView.setUIClient(new XWalkUIClient(xWalkView) {
                /* class com.github.tvbox.osc.ui.activity.PlayActivity.AnonymousClass24 */

                @Override // org.xwalk.core.XWalkUIClient
                public boolean onConsoleMessage(XWalkView xWalkView, String str, int i, String str2, XWalkUIClient.ConsoleMessageType consoleMessageType) {
                    return false;
                }

                @Override // org.xwalk.core.XWalkUIClient
                public boolean onJsAlert(XWalkView xWalkView, String str, String str2, XWalkJavascriptResult xWalkJavascriptResult) {
                    return true;
                }

                @Override // org.xwalk.core.XWalkUIClient
                public boolean onJsConfirm(XWalkView xWalkView, String str, String str2, XWalkJavascriptResult xWalkJavascriptResult) {
                    return true;
                }

                @Override // org.xwalk.core.XWalkUIClient
                public boolean onJsPrompt(XWalkView xWalkView, String str, String str2, String str3, XWalkJavascriptResult xWalkJavascriptResult) {
                    return true;
                }
            });
            XWalkWebClient xWalkWebClient = new XWalkWebClient(xWalkView);
            this.mX5WebClient = xWalkWebClient;
            xWalkView.setResourceClient(xWalkWebClient);
        }
    }

    /* access modifiers changed from: private */
    public class XWalkWebClient extends XWalkResourceClient {
        @Override // org.xwalk.core.XWalkResourceClient
        public boolean shouldOverrideUrlLoading(XWalkView xWalkView, String str) {
            return false;
        }

        public XWalkWebClient(XWalkView xWalkView) {
            super(xWalkView);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public void onDocumentLoadedInFrame(XWalkView xWalkView, long j) {
            super.onDocumentLoadedInFrame(xWalkView, j);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public void onLoadStarted(XWalkView xWalkView, String str) {
            super.onLoadStarted(xWalkView, str);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public void onLoadFinished(XWalkView xWalkView, String str) {
            super.onLoadFinished(xWalkView, str);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public void onProgressChanged(XWalkView xWalkView, int i) {
            super.onProgressChanged(xWalkView, i);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView xWalkView, XWalkWebResourceRequest xWalkWebResourceRequest) {
            boolean z;
            String uri = xWalkWebResourceRequest.getUrl().toString();
            if (uri.endsWith("/favicon.ico")) {
                return createXWalkWebResourceResponse("image/png", null, null);
            }
            LOG.i("shouldInterceptLoadRequest url:" + uri);
            if (!PlayActivity.this.loadedUrls.containsKey(uri)) {
                z = AdBlocker.isAd(uri);
                PlayActivity.this.loadedUrls.put(uri, Boolean.valueOf(z));
            } else {
                z = ((Boolean) PlayActivity.this.loadedUrls.get(uri)).booleanValue();
            }
            if (!z && !PlayActivity.this.loadFound && PlayActivity.this.checkVideoFormat(uri)) {
                PlayActivity.this.mHandler.removeMessages(100);
                PlayActivity.this.loadFound = true;
                HashMap<String, String> hashMap = new HashMap<>();
                try {
                    Map<String, String> requestHeaders = xWalkWebResourceRequest.getRequestHeaders();
                    for (String str : requestHeaders.keySet()) {
                        if (str.equalsIgnoreCase(RtspHeaders.USER_AGENT) || str.equalsIgnoreCase("referer") || str.equalsIgnoreCase("origin")) {
                            hashMap.put(str, StringUtils.SPACE + requestHeaders.get(str));
                        }
                    }
                } catch (Throwable unused) {
                }
                if (!hashMap.isEmpty()) {
                    PlayActivity.this.playUrl(uri, hashMap);
                } else {
                    PlayActivity.this.playUrl(uri, null);
                }
                String cookie = CookieManager.getInstance().getCookie(uri);
                if (!TextUtils.isEmpty(cookie)) {
                    hashMap.put("Cookie", StringUtils.SPACE + cookie);
                }
                PlayActivity.this.playUrl(uri, hashMap);
                PlayActivity.this.stopLoadWebView(false);
            }
            if (z || PlayActivity.this.loadFound) {
                return createXWalkWebResourceResponse(NanoHTTPD.MIME_PLAINTEXT, "utf-8", new ByteArrayInputStream("".getBytes()));
            }
            return super.shouldInterceptLoadRequest(xWalkView, xWalkWebResourceRequest);
        }

        @Override // org.xwalk.core.XWalkResourceClient
        public void onReceivedSslError(XWalkView xWalkView, ValueCallback<Boolean> valueCallback, SslError sslError) {
            valueCallback.onReceiveValue(true);
        }
    }
}
