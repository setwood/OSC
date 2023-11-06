package com.github.tvbox.osc.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.github.tvbox.osc.base.BaseLazyFragment;
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
import com.github.tvbox.osc.ui.activity.DetailActivity;
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
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.GetRequest;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.orhanobut.hawk.Hawk;
import com.umeng.analytics.pro.an;
import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

public class PlayFragment extends BaseLazyFragment {
    private int autoRetryCount = 0;
    public boolean extPlay;
    private boolean loadFound = false;
    private final Map<String, Boolean> loadedUrls = new HashMap();
    private VodController mController;
    private Handler mHandler;
    private ImageView mPlayLoadErr;
    private TextView mPlayLoadTip;
    private ProgressBar mPlayLoading;
    private SysWebClient mSysWebClient;
    private WebView mSysWebView;
    public MyVideoView mVideoView;
    private VodInfo mVodInfo;
    private JSONObject mVodPlayerCfg;
    private XWalkWebClient mX5WebClient;
    private XWalkView mXwalkWebView;
    private String parseFlag;
    ExecutorService parseThreadPool;
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

    private void initData() {
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public int getLayoutResID() {
        return R.layout.activity_play;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 12) {
            this.mController.mSubtitleView.setTextSize((float) ((Integer) refreshEvent.obj).intValue());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public void init() {
        initView();
        initViewModel();
        initData();
    }

    public VodController getVodController() {
        return this.mController;
    }

    private void initView() {
        EventBus.getDefault().register(this);
        this.mHandler = new Handler(new Handler.Callback() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass1 */

            public boolean handleMessage(Message message) {
                if (message.what == 100) {
                    PlayFragment.this.stopParse();
                    PlayFragment.this.errorWithRetry("嗅探错误", false);
                }
                return false;
            }
        });
        this.mVideoView = (MyVideoView) findViewById(R.id.mVideoView);
        this.mPlayLoadTip = (TextView) findViewById(R.id.play_load_tip);
        this.mPlayLoading = (ProgressBar) findViewById(R.id.play_loading);
        this.mPlayLoadErr = (ImageView) findViewById(R.id.play_load_error);
        VodController vodController = new VodController(requireContext());
        this.mController = vodController;
        vodController.setCanChangePosition(true);
        this.mController.setEnableInNormal(true);
        this.mController.setGestureEnabled(true);
        this.mVideoView.setProgressManager(new ProgressManager() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass2 */

            @Override // xyz.doikki.videoplayer.player.ProgressManager
            public void saveProgress(String str, long j) {
                CacheManager.save(MD5.string2MD5(str), Long.valueOf(j));
            }

            @Override // xyz.doikki.videoplayer.player.ProgressManager
            public long getSavedProgress(String str) {
                int i;
                try {
                    i = PlayFragment.this.mVodPlayerCfg.getInt("st");
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
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass3 */

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void playNext(boolean z) {
                if (PlayFragment.this.mVodInfo.reverseSort) {
                    PlayFragment.this.playPrevious();
                    return;
                }
                String str = PlayFragment.this.progressKey;
                PlayFragment.this.playNext(z);
                if (z && str != null) {
                    CacheManager.delete(MD5.string2MD5(str), 0);
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void playPre() {
                if (PlayFragment.this.mVodInfo.reverseSort) {
                    PlayFragment.this.playNext(false);
                } else {
                    PlayFragment.this.playPrevious();
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void changeParse(ParseBean parseBean) {
                PlayFragment.this.autoRetryCount = 0;
                PlayFragment.this.doParse(parseBean);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void updatePlayerCfg() {
                PlayFragment.this.mVodInfo.playerCfg = PlayFragment.this.mVodPlayerCfg.toString();
                EventBus.getDefault().post(new RefreshEvent(0, PlayFragment.this.mVodPlayerCfg));
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void replay(boolean z) {
                PlayFragment.this.autoRetryCount = 0;
                PlayFragment.this.play(z);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void errReplay() {
                PlayFragment.this.errorWithRetry("视频播放出错", false);
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void selectSubtitle() {
                try {
                    PlayFragment.this.selectMySubtitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void selectAudioTrack() {
                PlayFragment.this.selectMyAudioTrack();
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void openVideo() {
                PlayFragment.this.openMyVideo();
            }

            @Override // com.github.tvbox.osc.player.controller.VodController.VodControlListener
            public void prepared() {
                PlayFragment.this.initSubtitleView();
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
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass4 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setTextSize(int i) {
                PlayFragment.this.mController.mSubtitleView.setTextSize((float) i);
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setSubtitleDelay(int i) {
                PlayFragment.this.mController.mSubtitleView.setSubtitleDelay(Integer.valueOf(i));
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void selectInternalSubtitle() {
                PlayFragment.this.selectMyInternalSubtitle();
            }

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SubtitleViewListener
            public void setTextStyle(int i) {
                PlayFragment.this.setSubtitleViewTextStyle(i);
            }
        });
        subtitleDialog.setSearchSubtitleListener(new SubtitleDialog.SearchSubtitleListener() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass5 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.SearchSubtitleListener
            public void openSearchSubtitleDialog() {
                final SearchSubtitleDialog searchSubtitleDialog = new SearchSubtitleDialog(PlayFragment.this.mContext);
                searchSubtitleDialog.setSubtitleLoader(new SearchSubtitleDialog.SubtitleLoader() {
                    /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass5.AnonymousClass1 */

                    @Override // com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.SubtitleLoader
                    public void loadSubtitle(final Subtitle subtitle) {
                        PlayFragment.this.requireActivity().runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass5.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                String url = subtitle.getUrl();
                                LOG.i("Remote Subtitle Url: " + url);
                                PlayFragment.this.setSubtitle(url);
                                if (searchSubtitleDialog != null) {
                                    searchSubtitleDialog.dismiss();
                                }
                            }
                        });
                    }
                });
                if (PlayFragment.this.mVodInfo.playFlag.contains("Ali") || PlayFragment.this.mVodInfo.playFlag.contains("parse")) {
                    searchSubtitleDialog.setSearchWord(PlayFragment.this.mVodInfo.playNote);
                } else {
                    searchSubtitleDialog.setSearchWord(PlayFragment.this.mVodInfo.name);
                }
                searchSubtitleDialog.show();
            }
        });
        subtitleDialog.setLocalFileChooserListener(new SubtitleDialog.LocalFileChooserListener() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass6 */

            @Override // com.github.tvbox.osc.ui.dialog.SubtitleDialog.LocalFileChooserListener
            public void openLocalFileChooserDialog() {
                new ChooserDialog((Activity) PlayFragment.this.getActivity()).withFilter(false, false, "srt", "ass", "scc", "stl", "ttml").withStartFile("/storage/emulated/0/Download").withChosenListener(new ChooserDialog.Result() {
                    /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass6.AnonymousClass1 */

                    @Override // com.obsez.android.lib.filechooser.ChooserDialog.Result
                    public void onChoosePath(String str, File file) {
                        LOG.i("Local Subtitle Path: " + str);
                        PlayFragment.this.setSubtitle(str);
                    }
                }).build().show();
            }
        });
        subtitleDialog.show();
    }

    /* access modifiers changed from: package-private */
    public void setSubtitleViewTextStyle(int i) {
        if (i == 0) {
            this.mController.mSubtitleView.setTextColor(getContext().getResources().getColorStateList(2131034178));
            this.mController.mSubtitleView.setShadowLayer(3.0f, 2.0f, 2.0f, R.color.color_000000_80);
        } else if (i == 1) {
            this.mController.mSubtitleView.setTextColor(getContext().getResources().getColorStateList(2131034176));
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
            final SelectDialog selectDialog = new SelectDialog(this.mContext);
            selectDialog.setTip(getString(R.string.vod_sub_sel));
            selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<TrackInfoBean>() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass7 */

                public void click(TrackInfoBean trackInfoBean, int i) {
                    PlayFragment.this.mController.mSubtitleView.setVisibility(0);
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
                            PlayFragment.this.mController.mSubtitleView.destroy();
                            PlayFragment.this.mController.mSubtitleView.clearSubtitleCache();
                            PlayFragment.this.mController.mSubtitleView.isInternal = true;
                            ((IjkMediaPlayer) mediaPlayer).setTrack(trackInfoBean.index);
                            new Handler().postDelayed(new Runnable() {
                                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass7.AnonymousClass1 */

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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass8 */

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
                final SelectDialog selectDialog = new SelectDialog(getActivity());
                selectDialog.setTip(getString(R.string.vod_audio));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<TrackInfoBean>() {
                    /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass9 */

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
                                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass9.AnonymousClass1 */

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
                    /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass10 */

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
    public void setTip(final String str, final boolean z, final boolean z2) {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass11 */

                public void run() {
                    PlayFragment.this.mPlayLoadTip.setText(str);
                    int i = 0;
                    PlayFragment.this.mPlayLoadTip.setVisibility(0);
                    PlayFragment.this.mPlayLoading.setVisibility(z ? 0 : 8);
                    ImageView imageView = PlayFragment.this.mPlayLoadErr;
                    if (!z2) {
                        i = 8;
                    }
                    imageView.setVisibility(i);
                }
            });
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
            requireActivity().runOnUiThread(new Runnable() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass12 */

                public void run() {
                    if (z) {
                        Toast.makeText(PlayFragment.this.mContext, str, 0).show();
                    } else {
                        PlayFragment.this.setTip(str, false, true);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void playUrl(final String str, final HashMap<String, String> hashMap) {
        if (this.mActivity != null) {
            requireActivity().runOnUiThread(new Runnable() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass13 */

                public void run() {
                    boolean z;
                    PlayFragment.this.stopParse();
                    if (PlayFragment.this.mVideoView != null) {
                        PlayFragment.this.mVideoView.release();
                        String str = str;
                        if (str != null) {
                            PlayFragment.this.videoURL = str;
                            try {
                                int i = PlayFragment.this.mVodPlayerCfg.getInt(an.az);
                                boolean z2 = false;
                                PlayFragment.this.extPlay = false;
                                if (i >= 10) {
                                    String str2 = PlayFragment.this.mVodInfo.name + " : " + PlayFragment.this.mVodInfo.seriesMap.get(PlayFragment.this.mVodInfo.playFlag).get(PlayFragment.this.mVodInfo.playIndex).name;
                                    PlayFragment.this.setTip("调用外部播放器" + PlayerHelper.getPlayerName(i) + "进行播放", true, false);
                                    switch (i) {
                                        case 10:
                                            PlayFragment.this.extPlay = true;
                                            z = MXPlayer.run(PlayFragment.this.requireActivity(), str, str2, PlayFragment.this.playSubtitle, hashMap);
                                            break;
                                        case 11:
                                            PlayFragment.this.extPlay = true;
                                            z = ReexPlayer.run(PlayFragment.this.requireActivity(), str, str2, PlayFragment.this.playSubtitle, hashMap);
                                            break;
                                        case 12:
                                            PlayFragment.this.extPlay = true;
                                            z = Kodi.run(PlayFragment.this.requireActivity(), str, str2, PlayFragment.this.playSubtitle, hashMap);
                                            break;
                                        default:
                                            z = false;
                                            break;
                                    }
                                    PlayFragment playFragment = PlayFragment.this;
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("调用外部播放器");
                                    sb.append(PlayerHelper.getPlayerName(i));
                                    sb.append(z ? "成功" : "失败");
                                    String sb2 = sb.toString();
                                    if (!z) {
                                        z2 = true;
                                    }
                                    playFragment.setTip(sb2, z, z2);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            PlayFragment.this.hideTip();
                            PlayerHelper.updateCfg(PlayFragment.this.mVideoView, PlayFragment.this.mVodPlayerCfg);
                            PlayFragment.this.mVideoView.setProgressKey(PlayFragment.this.progressKey);
                            if (hashMap != null) {
                                PlayFragment.this.mVideoView.setUrl(str, hashMap);
                            } else {
                                PlayFragment.this.mVideoView.setUrl(str);
                            }
                            PlayFragment.this.mVideoView.start();
                            PlayFragment.this.mController.resetSpeed();
                        }
                    }
                }
            });
        }
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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass14 */

                @Override // tv.danmaku.ijk.media.player.IMediaPlayer.OnTimedTextListener
                public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
                    if (PlayFragment.this.mController.mSubtitleView.isInternal) {
                        com.github.tvbox.osc.subtitle.model.Subtitle subtitle = new com.github.tvbox.osc.subtitle.model.Subtitle();
                        subtitle.content = ijkTimedText.getText();
                        PlayFragment.this.mController.mSubtitleView.onSubtitleChanged(subtitle);
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
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass15 */

            public void onChanged(JSONObject jSONObject) {
                if (jSONObject != null) {
                    try {
                        HashMap<String, String> hashMap = null;
                        PlayFragment.this.progressKey = jSONObject.optString("proKey", null);
                        boolean equals = jSONObject.optString("parse", "1").equals("1");
                        boolean equals2 = jSONObject.optString("jx", SessionDescription.SUPPORTED_SDP_VERSION).equals("1");
                        PlayFragment.this.playSubtitle = jSONObject.optString("subt", "");
                        PlayFragment.this.subtitleCacheKey = jSONObject.optString("subtKey", null);
                        String optString = jSONObject.optString("playUrl", "");
                        String optString2 = jSONObject.optString("flag");
                        String string = jSONObject.getString("url");
                        PlayFragment.this.webUserAgent = null;
                        PlayFragment.this.webHeaderMap = null;
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
                                        PlayFragment.this.webUserAgent = jSONObject2.getString(next).trim();
                                    }
                                }
                                PlayFragment.this.webHeaderMap = hashMap;
                            } catch (Throwable unused) {
                            }
                        }
                        boolean z = false;
                        if (equals || equals2) {
                            if ((optString.isEmpty() && ApiConfig.get().getVipParseFlags().contains(optString2)) || equals2) {
                                z = true;
                            }
                            PlayFragment.this.initParse(optString2, z, optString, string);
                            return;
                        }
                        PlayFragment.this.mController.showParse(false);
                        PlayFragment playFragment = PlayFragment.this;
                        playFragment.playUrl(optString + string, hashMap);
                    } catch (Throwable unused2) {
                        PlayFragment.this.errorWithRetry("获取播放信息错误", true);
                    }
                } else {
                    PlayFragment.this.errorWithRetry("获取播放信息错误", true);
                }
            }
        });
    }

    public void setData(Bundle bundle) {
        this.mVodInfo = (VodInfo) bundle.getSerializable("VodInfo");
        this.sourceKey = bundle.getString("sourceKey");
        this.sourceBean = ApiConfig.get().getSource(this.sourceKey);
        initPlayerCfg();
        play(false);
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

    public boolean onBackPressed() {
        return this.mController.onBackPressed();
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent != null) {
            return this.mController.onKeyEvent(keyEvent);
        }
        return false;
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        this.mVideoView.pause();
    }

    @Override // androidx.fragment.app.Fragment, com.github.tvbox.osc.base.BaseLazyFragment
    public void onResume() {
        super.onResume();
        MyVideoView myVideoView = this.mVideoView;
        if (myVideoView != null) {
            myVideoView.resume();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPictureInPictureModeChanged(boolean z) {
        if (!z) {
            this.mVideoView.isPlaying();
        }
        super.onPictureInPictureModeChanged(z);
    }

    @Override // androidx.fragment.app.Fragment, com.github.tvbox.osc.base.BaseLazyFragment
    public void onHiddenChanged(boolean z) {
        if (z) {
            MyVideoView myVideoView = this.mVideoView;
            if (myVideoView != null) {
                myVideoView.pause();
            }
        } else {
            MyVideoView myVideoView2 = this.mVideoView;
            if (myVideoView2 != null) {
                myVideoView2.resume();
            }
        }
        super.onHiddenChanged(z);
    }

    @Override // androidx.fragment.app.Fragment, com.github.tvbox.osc.base.BaseLazyFragment
    public void onDestroyView() {
        super.onDestroyView();
        MyVideoView myVideoView = this.mVideoView;
        if (myVideoView != null) {
            myVideoView.release();
            this.mVideoView = null;
        }
        stopLoadWebView(true);
        stopParse();
    }

    public void playNext(boolean z) {
        VodInfo vodInfo = this.mVodInfo;
        if (!((vodInfo == null || vodInfo.seriesMap.get(this.mVodInfo.playFlag) == null || this.mVodInfo.playIndex + 1 >= this.mVodInfo.seriesMap.get(this.mVodInfo.playFlag).size()) ? false : true)) {
            Toast.makeText(requireContext(), "已经是最后一集了", 0).show();
            if (z && ((DetailActivity) this.mActivity).fullWindows) {
                ((DetailActivity) this.mActivity).toggleFullPreview();
                return;
            }
            return;
        }
        this.mVodInfo.playIndex++;
        play(false);
    }

    public void playPrevious() {
        VodInfo vodInfo = this.mVodInfo;
        if (!((vodInfo == null || vodInfo.seriesMap.get(this.mVodInfo.playFlag) == null || this.mVodInfo.playIndex - 1 < 0) ? false : true)) {
            Toast.makeText(requireContext(), "已经是第一集了", 0).show();
            return;
        }
        this.mVodInfo.playIndex--;
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
            try {
                myVideoView.release();
            } catch (Exception unused) {
            }
        }
        String str = this.mVodInfo.sourceKey + "-" + this.mVodInfo.id + "-" + this.mVodInfo.playFlag + "-" + this.mVodInfo.playIndex + "-" + vodSeries.name + "-subt";
        String str2 = this.mVodInfo.sourceKey + this.mVodInfo.id + this.mVodInfo.playFlag + this.mVodInfo.playIndex;
        if (z) {
            CacheManager.delete(MD5.string2MD5(str2), 0);
            CacheManager.delete(MD5.string2MD5(str), "");
        }
        if (Thunder.play(vodSeries.url, new Thunder.ThunderCallback() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass16 */

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void list(String str) {
            }

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void status(int i, String str) {
                if (i < 0) {
                    PlayFragment.this.setTip(str, false, true);
                } else {
                    PlayFragment.this.setTip(str, true, false);
                }
            }

            @Override // com.github.tvbox.osc.util.thunder.Thunder.ThunderCallback
            public void play(String str) {
                PlayFragment.this.playUrl(str, null);
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
            ((GetRequest) ((GetRequest) OkGo.get(parseBean.getUrl() + encodeUrl(this.webUrl)).tag("json_jx")).headers(httpHeaders)).execute(new AbsCallback<String>() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass17 */

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
                        PlayFragment playFragment = PlayFragment.this;
                        JSONObject jsonParse = playFragment.jsonParse(playFragment.webUrl, body);
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
                        PlayFragment.this.playUrl(jsonParse.getString("url"), hashMap);
                    } catch (Throwable th) {
                        th.printStackTrace();
                        PlayFragment.this.errorWithRetry("解析错误", false);
                    }
                }

                @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                public void onError(com.lzy.okgo.model.Response<String> response) {
                    super.onError(response);
                    PlayFragment.this.errorWithRetry("解析错误", false);
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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass18 */

                public void run() {
                    final JSONObject jsonExt = ApiConfig.get().jsonExt(parseBean.getUrl(), linkedHashMap, PlayFragment.this.webUrl);
                    boolean z = false;
                    if (jsonExt == null || !jsonExt.has("url")) {
                        PlayFragment.this.errorWithRetry("解析错误", false);
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
                        PlayFragment.this.requireActivity().runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass18.AnonymousClass1 */

                            public void run() {
                                Context context = PlayFragment.this.mContext;
                                Toast.makeText(context, "解析来自:" + jsonExt.optString("jxFrom"), 0).show();
                            }
                        });
                    }
                    if (jsonExt.optInt("parse", 0) == 1) {
                        z = true;
                    }
                    if (z) {
                        PlayFragment.this.loadUrl(DefaultConfig.checkReplaceProxy(jsonExt.optString("url", "")));
                        return;
                    }
                    PlayFragment.this.playUrl(jsonExt.optString("url", ""), hashMap);
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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass19 */

                public void run() {
                    ApiConfig apiConfig = ApiConfig.get();
                    final JSONObject jsonExtMix = apiConfig.jsonExtMix(PlayFragment.this.parseFlag + "111", parseBean.getUrl(), str, linkedHashMap2, PlayFragment.this.webUrl);
                    if (jsonExtMix == null || !jsonExtMix.has("url")) {
                        PlayFragment.this.errorWithRetry("解析错误", false);
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
                            PlayFragment.this.requireActivity().runOnUiThread(new Runnable() {
                                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass19.AnonymousClass2 */

                                public void run() {
                                    Context context = PlayFragment.this.mContext;
                                    Toast.makeText(context, "解析来自:" + jsonExtMix.optString("jxFrom"), 0).show();
                                }
                            });
                        }
                        PlayFragment.this.playUrl(jsonExtMix.optString("url", ""), hashMap);
                    } else {
                        PlayFragment.this.requireActivity().runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass19.AnonymousClass1 */

                            public void run() {
                                String checkReplaceProxy = DefaultConfig.checkReplaceProxy(jsonExtMix.optString("url", ""));
                                PlayFragment.this.stopParse();
                                PlayFragment.this.setTip("正在嗅探播放地址", true, false);
                                PlayFragment.this.mHandler.removeMessages(100);
                                PlayFragment.this.mHandler.sendEmptyMessageDelayed(100, SilenceSkippingAudioProcessor.DEFAULT_PADDING_SILENCE_US);
                                PlayFragment.this.loadWebView(checkReplaceProxy);
                            }
                        });
                    }
                }
            });
        }
    }

    private String encodeUrl(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception unused) {
            return str;
        }
    }

    /* access modifiers changed from: package-private */
    public void loadWebView(final String str) {
        if (this.mSysWebView != null || this.mXwalkWebView != null) {
            loadUrl(str);
        } else if (!((Boolean) Hawk.get(HawkConfig.PARSE_WEBVIEW, true)).booleanValue()) {
            XWalkUtils.tryUseXWalk(this.mContext, new XWalkUtils.XWalkState() {
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass20 */

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void success() {
                    PlayFragment.this.initWebView(false);
                    PlayFragment.this.loadUrl(str);
                }

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void fail() {
                    Toast.makeText(PlayFragment.this.mContext, "XWalkView不兼容，已替换为系统自带WebView", 0).show();
                    PlayFragment.this.initWebView(true);
                    PlayFragment.this.loadUrl(str);
                }

                @Override // com.github.tvbox.osc.util.XWalkUtils.XWalkState
                public void ignore() {
                    Toast.makeText(PlayFragment.this.mContext, "XWalkView运行组件未下载，已替换为系统自带WebView", 0).show();
                    PlayFragment.this.initWebView(true);
                    PlayFragment.this.loadUrl(str);
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
        requireActivity().runOnUiThread(new Runnable() {
            /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass21 */

            public void run() {
                if (PlayFragment.this.mXwalkWebView != null) {
                    PlayFragment.this.mXwalkWebView.stopLoading();
                    if (PlayFragment.this.webUserAgent != null) {
                        PlayFragment.this.mXwalkWebView.getSettings().setUserAgentString(PlayFragment.this.webUserAgent);
                    }
                    if (PlayFragment.this.webHeaderMap != null) {
                        PlayFragment.this.mXwalkWebView.loadUrl(str, PlayFragment.this.webHeaderMap);
                    } else {
                        PlayFragment.this.mXwalkWebView.loadUrl(str);
                    }
                }
                if (PlayFragment.this.mSysWebView != null) {
                    PlayFragment.this.mSysWebView.stopLoading();
                    if (PlayFragment.this.webUserAgent != null) {
                        PlayFragment.this.mSysWebView.getSettings().setUserAgentString(PlayFragment.this.webUserAgent);
                    }
                    if (PlayFragment.this.webHeaderMap != null) {
                        PlayFragment.this.mSysWebView.loadUrl(str, PlayFragment.this.webHeaderMap);
                    } else {
                        PlayFragment.this.mSysWebView.loadUrl(str);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void stopLoadWebView(final boolean z) {
        if (this.mActivity != null) {
            try {
                requireActivity().runOnUiThread(new Runnable() {
                    /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass22 */

                    public void run() {
                        if (PlayFragment.this.mXwalkWebView != null) {
                            PlayFragment.this.mXwalkWebView.stopLoading();
                            PlayFragment.this.mXwalkWebView.loadUrl("about:blank");
                            if (z) {
                                PlayFragment.this.mXwalkWebView.removeAllViews();
                                PlayFragment.this.mXwalkWebView.onDestroy();
                                PlayFragment.this.mXwalkWebView = null;
                            }
                        }
                        if (PlayFragment.this.mSysWebView != null) {
                            PlayFragment.this.mSysWebView.stopLoading();
                            PlayFragment.this.mSysWebView.loadUrl("about:blank");
                            if (z) {
                                ViewGroup viewGroup = (ViewGroup) PlayFragment.this.mSysWebView.getParent();
                                if (viewGroup != null) {
                                    viewGroup.removeView(PlayFragment.this.mSysWebView);
                                }
                                PlayFragment.this.mSysWebView.removeAllViews();
                                PlayFragment.this.mSysWebView.destroy();
                                PlayFragment.this.mSysWebView = null;
                            }
                        }
                    }
                });
            } catch (Exception unused) {
            }
        }
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
            if (PlayFragment.this.mContext instanceof Activity) {
                AutoSize.autoConvertDensityOfCustomAdapt((Activity) PlayFragment.this.mContext, PlayFragment.this);
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
            if (PlayFragment.this.mContext instanceof Activity) {
                AutoSize.autoConvertDensityOfCustomAdapt((Activity) PlayFragment.this.mContext, PlayFragment.this);
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
            requireActivity().addContentView(webView, layoutParams);
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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass23 */

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
            if (!PlayFragment.this.loadedUrls.containsKey(str)) {
                z = AdBlocker.isAd(str);
                PlayFragment.this.loadedUrls.put(str, Boolean.valueOf(z));
            } else {
                z = ((Boolean) PlayFragment.this.loadedUrls.get(str)).booleanValue();
            }
            if (!z && !PlayFragment.this.loadFound && PlayFragment.this.checkVideoFormat(str)) {
                PlayFragment.this.mHandler.removeMessages(100);
                PlayFragment.this.loadFound = true;
                if (hashMap == null || hashMap.isEmpty()) {
                    PlayFragment.this.playUrl(str, null);
                } else {
                    PlayFragment.this.playUrl(str, hashMap);
                }
                String cookie = CookieManager.getInstance().getCookie(str);
                if (!TextUtils.isEmpty(cookie)) {
                    hashMap.put("Cookie", StringUtils.SPACE + cookie);
                }
                PlayFragment.this.playUrl(str, hashMap);
                PlayFragment.this.stopLoadWebView(false);
            }
            if (z || PlayFragment.this.loadFound) {
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
            requireActivity().addContentView(xWalkView, layoutParams);
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
                /* class com.github.tvbox.osc.ui.fragment.PlayFragment.AnonymousClass24 */

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
            if (!PlayFragment.this.loadedUrls.containsKey(uri)) {
                z = AdBlocker.isAd(uri);
                PlayFragment.this.loadedUrls.put(uri, Boolean.valueOf(z));
            } else {
                z = ((Boolean) PlayFragment.this.loadedUrls.get(uri)).booleanValue();
            }
            if (!z && !PlayFragment.this.loadFound && PlayFragment.this.checkVideoFormat(uri)) {
                PlayFragment.this.mHandler.removeMessages(100);
                PlayFragment.this.loadFound = true;
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
                    PlayFragment.this.playUrl(uri, hashMap);
                } else {
                    PlayFragment.this.playUrl(uri, null);
                }
                String cookie = CookieManager.getInstance().getCookie(uri);
                if (!TextUtils.isEmpty(cookie)) {
                    hashMap.put("Cookie", StringUtils.SPACE + cookie);
                }
                PlayFragment.this.playUrl(uri, hashMap);
                PlayFragment.this.stopLoadWebView(false);
            }
            if (z || PlayFragment.this.loadFound) {
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
