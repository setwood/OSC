package com.github.tvbox.osc.ui.activity;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.ui.adapter.SeriesAdapter;
import com.github.tvbox.osc.ui.adapter.SeriesFlagAdapter;
import com.github.tvbox.osc.ui.dialog.QuickSearchDialog;
import com.github.tvbox.osc.ui.fragment.PlayFragment;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MD5;
import com.github.tvbox.osc.util.SearchHelper;
import com.github.tvbox.osc.util.SubtitleHelper;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.GetRequest;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.umeng.analytics.pro.an;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.jessyan.autosize.utils.AutoSizeUtils;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class DetailActivity extends BaseActivity {
    private static final int PIP_BOARDCAST_ACTION_NEXT = 2;
    private static final int PIP_BOARDCAST_ACTION_PLAYPAUSE = 1;
    private static final int PIP_BOARDCAST_ACTION_PREV = 0;
    private static PlayFragment playFragment;
    boolean PiPON = ((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, false)).booleanValue();
    public boolean fullWindows = false;
    private boolean hadQuickStart = false;
    private ImageView ivThumb;
    private LinearLayout llLayout;
    private FragmentContainerView llPlayerFragmentContainer;
    private View llPlayerFragmentContainerBlock;
    private View llPlayerPlace;
    private HashMap<String, String> mCheckSources = null;
    private LinearLayout mEmptyPlayList;
    private TvRecyclerView mGridView;
    private TvRecyclerView mGridViewFlag;
    private V7GridLayoutManager mGridViewLayoutMgr = null;
    private Movie.Video mVideo;
    private List<Runnable> pauseRunnable = null;
    private BroadcastReceiver pipActionReceiver;
    private String preFlag = "";
    VodInfo previewVodInfo = null;
    private final List<Movie.Video> quickSearchData = new ArrayList();
    private final List<String> quickSearchWord = new ArrayList();
    private ExecutorService searchExecutorService = null;
    private String searchTitle = "";
    private SeriesAdapter seriesAdapter;
    private SeriesFlagAdapter seriesFlagAdapter;
    private View seriesFlagFocus = null;
    boolean seriesSelect = false;
    boolean showPreview = ((Boolean) Hawk.get(HawkConfig.SHOW_PREVIEW, true)).booleanValue();
    public String sourceKey;
    private SourceViewModel sourceViewModel;
    private TextView tvActor;
    private TextView tvArea;
    private TextView tvCollect;
    private TextView tvDes;
    private TextView tvDirector;
    private TextView tvLang;
    private TextView tvName;
    private TextView tvPlay;
    private ImageView tvPlayUrl;
    private TextView tvQuickSearch;
    private TextView tvSite;
    private TextView tvSort;
    private TextView tvType;
    private TextView tvYear;
    public String vodId;
    private VodInfo vodInfo;
    ViewGroup.LayoutParams windowsFull = null;
    ViewGroup.LayoutParams windowsPreview = null;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_detail;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        EventBus.getDefault().register(this);
        initView();
        initViewModel();
        initData();
    }

    private void initView() {
        this.llLayout = (LinearLayout) findViewById(R.id.llLayout);
        this.llPlayerPlace = findViewById(R.id.previewPlayerPlace);
        this.llPlayerFragmentContainer = (FragmentContainerView) findViewById(R.id.previewPlayer);
        this.llPlayerFragmentContainerBlock = findViewById(R.id.previewPlayerBlock);
        this.ivThumb = (ImageView) findViewById(R.id.ivThumb);
        this.llPlayerPlace.setVisibility(this.showPreview ? 0 : 8);
        this.ivThumb.setVisibility(!this.showPreview ? 0 : 8);
        this.tvName = (TextView) findViewById(R.id.tvName);
        this.tvYear = (TextView) findViewById(R.id.tvYear);
        this.tvSite = (TextView) findViewById(R.id.tvSite);
        this.tvArea = (TextView) findViewById(R.id.tvArea);
        this.tvLang = (TextView) findViewById(R.id.tvLang);
        this.tvType = (TextView) findViewById(R.id.tvType);
        this.tvActor = (TextView) findViewById(R.id.tvActor);
        this.tvDirector = (TextView) findViewById(R.id.tvDirector);
        this.tvDes = (TextView) findViewById(R.id.tvDes);
        this.tvPlay = (TextView) findViewById(R.id.tvPlay);
        this.tvSort = (TextView) findViewById(R.id.tvSort);
        this.tvCollect = (TextView) findViewById(R.id.tvCollect);
        this.tvQuickSearch = (TextView) findViewById(R.id.tvQuickSearch);
        this.tvPlayUrl = (ImageView) findViewById(R.id.tvPlayUrl);
        this.mEmptyPlayList = (LinearLayout) findViewById(R.id.mEmptyPlaylist);
        TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.mGridView = tvRecyclerView;
        tvRecyclerView.setHasFixedSize(false);
        V7GridLayoutManager v7GridLayoutManager = new V7GridLayoutManager(this.mContext, 6);
        this.mGridViewLayoutMgr = v7GridLayoutManager;
        this.mGridView.setLayoutManager(v7GridLayoutManager);
        SeriesAdapter seriesAdapter2 = new SeriesAdapter();
        this.seriesAdapter = seriesAdapter2;
        this.mGridView.setAdapter(seriesAdapter2);
        TvRecyclerView tvRecyclerView2 = (TvRecyclerView) findViewById(R.id.mGridViewFlag);
        this.mGridViewFlag = tvRecyclerView2;
        tvRecyclerView2.setHasFixedSize(true);
        this.mGridViewFlag.setLayoutManager(new V7LinearLayoutManager(this.mContext, 0, false));
        SeriesFlagAdapter seriesFlagAdapter2 = new SeriesFlagAdapter();
        this.seriesFlagAdapter = seriesFlagAdapter2;
        this.mGridViewFlag.setAdapter(seriesFlagAdapter2);
        if (this.showPreview) {
            playFragment = new PlayFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.previewPlayer, playFragment).commit();
            getSupportFragmentManager().beginTransaction().show(playFragment).commitAllowingStateLoss();
            this.tvPlay.setText(getString(R.string.det_expand));
            this.tvPlay.setVisibility(8);
        } else {
            this.tvPlay.setVisibility(0);
            this.tvPlay.requestFocus();
        }
        this.tvSort.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass1 */

            public void onClick(View view) {
                if (DetailActivity.this.vodInfo != null && DetailActivity.this.vodInfo.seriesMap.size() > 0) {
                    DetailActivity.this.vodInfo.reverseSort = !DetailActivity.this.vodInfo.reverseSort;
                    DetailActivity.this.preFlag = "";
                    if (DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).size() > DetailActivity.this.vodInfo.playIndex) {
                        DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).get(DetailActivity.this.vodInfo.playIndex).selected = false;
                    }
                    DetailActivity.this.vodInfo.reverse();
                    if (DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).size() > DetailActivity.this.vodInfo.playIndex) {
                        DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).get(DetailActivity.this.vodInfo.playIndex).selected = true;
                    }
                    DetailActivity detailActivity = DetailActivity.this;
                    detailActivity.insertVod(detailActivity.sourceKey, DetailActivity.this.vodInfo);
                    DetailActivity.this.seriesAdapter.notifyDataSetChanged();
                }
            }
        });
        this.tvPlay.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass2 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                if (DetailActivity.this.showPreview) {
                    DetailActivity.this.toggleFullPreview();
                } else {
                    DetailActivity.this.jumpToPlay();
                }
            }
        });
        this.ivThumb.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                DetailActivity.this.jumpToPlay();
            }
        });
        this.llPlayerFragmentContainerBlock.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                DetailActivity.this.toggleFullPreview();
            }
        });
        this.tvQuickSearch.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass5 */

            public void onClick(View view) {
                DetailActivity.this.startQuickSearch();
                QuickSearchDialog quickSearchDialog = new QuickSearchDialog(DetailActivity.this);
                EventBus.getDefault().post(new RefreshEvent(2, DetailActivity.this.quickSearchData));
                EventBus.getDefault().post(new RefreshEvent(4, DetailActivity.this.quickSearchWord));
                quickSearchDialog.show();
                if (DetailActivity.this.pauseRunnable != null && DetailActivity.this.pauseRunnable.size() > 0) {
                    DetailActivity.this.searchExecutorService = Executors.newFixedThreadPool(5);
                    for (Runnable runnable : DetailActivity.this.pauseRunnable) {
                        DetailActivity.this.searchExecutorService.execute(runnable);
                    }
                    DetailActivity.this.pauseRunnable.clear();
                    DetailActivity.this.pauseRunnable = null;
                }
                quickSearchDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass5.AnonymousClass1 */

                    public void onDismiss(DialogInterface dialogInterface) {
                        try {
                            if (DetailActivity.this.searchExecutorService != null) {
                                DetailActivity.this.pauseRunnable = DetailActivity.this.searchExecutorService.shutdownNow();
                                DetailActivity.this.searchExecutorService = null;
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            }
        });
        this.tvCollect.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass6 */

            public void onClick(View view) {
                if (DetailActivity.this.getString(R.string.det_fav_unstar).equals(DetailActivity.this.tvCollect.getText().toString())) {
                    RoomDataManger.insertVodCollect(DetailActivity.this.sourceKey, DetailActivity.this.vodInfo);
                    DetailActivity detailActivity = DetailActivity.this;
                    Toast.makeText(detailActivity, detailActivity.getString(R.string.det_fav_add), 0).show();
                    DetailActivity.this.tvCollect.setText(DetailActivity.this.getString(R.string.det_fav_star));
                    return;
                }
                RoomDataManger.deleteVodCollect(DetailActivity.this.sourceKey, DetailActivity.this.vodInfo);
                DetailActivity detailActivity2 = DetailActivity.this;
                Toast.makeText(detailActivity2, detailActivity2.getString(R.string.det_fav_del), 0).show();
                DetailActivity.this.tvCollect.setText(DetailActivity.this.getString(R.string.det_fav_unstar));
            }
        });
        this.tvPlayUrl.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass7 */

            public void onClick(View view) {
                ((ClipboardManager) DetailActivity.this.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(null, DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).get(0).url));
                DetailActivity detailActivity = DetailActivity.this;
                Toast.makeText(detailActivity, detailActivity.getString(R.string.det_url), 0).show();
            }
        });
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass8 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                DetailActivity.this.seriesSelect = false;
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                DetailActivity.this.seriesSelect = true;
            }
        });
        this.mGridViewFlag.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass9 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            private void refresh(View view, int i) {
                String str = ((VodInfo.VodSeriesFlag) DetailActivity.this.seriesFlagAdapter.getData().get(i)).name;
                if (DetailActivity.this.vodInfo != null && !DetailActivity.this.vodInfo.playFlag.equals(str)) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= DetailActivity.this.vodInfo.seriesFlags.size()) {
                            break;
                        }
                        VodInfo.VodSeriesFlag vodSeriesFlag = DetailActivity.this.vodInfo.seriesFlags.get(i2);
                        if (vodSeriesFlag.name.equals(DetailActivity.this.vodInfo.playFlag)) {
                            vodSeriesFlag.selected = false;
                            DetailActivity.this.seriesFlagAdapter.notifyItemChanged(i2);
                            break;
                        }
                        i2++;
                    }
                    DetailActivity.this.vodInfo.seriesFlags.get(i).selected = true;
                    if (DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).size() > DetailActivity.this.vodInfo.playIndex) {
                        DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).get(DetailActivity.this.vodInfo.playIndex).selected = false;
                    }
                    DetailActivity.this.vodInfo.playFlag = str;
                    DetailActivity.this.seriesFlagAdapter.notifyItemChanged(i);
                    DetailActivity.this.refreshList();
                }
                DetailActivity.this.seriesFlagFocus = view;
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                refresh(view, i);
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                refresh(view, i);
            }
        });
        this.seriesAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass10 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                if (DetailActivity.this.vodInfo != null && DetailActivity.this.vodInfo.seriesMap.get(DetailActivity.this.vodInfo.playFlag).size() > 0) {
                    boolean z = false;
                    boolean z2 = true;
                    if (DetailActivity.this.vodInfo.playIndex != i) {
                        ((VodInfo.VodSeries) DetailActivity.this.seriesAdapter.getData().get(DetailActivity.this.vodInfo.playIndex)).selected = false;
                        DetailActivity.this.seriesAdapter.notifyItemChanged(DetailActivity.this.vodInfo.playIndex);
                        ((VodInfo.VodSeries) DetailActivity.this.seriesAdapter.getData().get(i)).selected = true;
                        DetailActivity.this.seriesAdapter.notifyItemChanged(i);
                        DetailActivity.this.vodInfo.playIndex = i;
                        z = true;
                    }
                    if (DetailActivity.this.vodInfo.playFlag.equals(DetailActivity.this.preFlag)) {
                        z2 = z;
                    }
                    if (DetailActivity.this.showPreview && !DetailActivity.this.fullWindows) {
                        DetailActivity.this.toggleFullPreview();
                    }
                    if (z2 || !DetailActivity.this.showPreview) {
                        DetailActivity.this.jumpToPlay();
                    }
                }
            }
        });
        setLoadSir(this.llLayout);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void jumpToPlay() {
        VodInfo vodInfo2 = this.vodInfo;
        if (vodInfo2 != null && vodInfo2.seriesMap.get(this.vodInfo.playFlag).size() > 0) {
            this.preFlag = this.vodInfo.playFlag;
            Bundle bundle = new Bundle();
            insertVod(this.sourceKey, this.vodInfo);
            bundle.putString("sourceKey", this.sourceKey);
            bundle.putSerializable("VodInfo", this.vodInfo);
            if (this.showPreview) {
                if (this.previewVodInfo == null) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                        objectOutputStream.writeObject(this.vodInfo);
                        objectOutputStream.flush();
                        objectOutputStream.close();
                        this.previewVodInfo = (VodInfo) new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                VodInfo vodInfo3 = this.previewVodInfo;
                if (vodInfo3 != null) {
                    vodInfo3.playerCfg = this.vodInfo.playerCfg;
                    this.previewVodInfo.playFlag = this.vodInfo.playFlag;
                    this.previewVodInfo.playIndex = this.vodInfo.playIndex;
                    this.previewVodInfo.seriesMap = this.vodInfo.seriesMap;
                    bundle.putSerializable("VodInfo", this.previewVodInfo);
                }
                playFragment.setData(bundle);
                return;
            }
            jumpActivity(PlayActivity.class, bundle);
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshList() {
        boolean z;
        if (this.vodInfo.seriesMap.get(this.vodInfo.playFlag).size() <= this.vodInfo.playIndex) {
            this.vodInfo.playIndex = 0;
        }
        int i = 1;
        if (this.vodInfo.seriesMap.get(this.vodInfo.playFlag) != null) {
            int i2 = 0;
            while (true) {
                if (i2 >= this.vodInfo.seriesMap.get(this.vodInfo.playFlag).size()) {
                    z = true;
                    break;
                } else if (this.vodInfo.seriesMap.get(this.vodInfo.playFlag).get(i2).selected) {
                    z = false;
                    break;
                } else {
                    i2++;
                }
            }
            if (z) {
                this.vodInfo.seriesMap.get(this.vodInfo.playFlag).get(this.vodInfo.playIndex).selected = true;
            }
        }
        Paint paint = new Paint();
        List<VodInfo.VodSeries> list = this.vodInfo.seriesMap.get(this.vodInfo.playFlag);
        int size = list.size();
        for (int i3 = 0; i3 < size; i3++) {
            String str = list.get(i3).name;
            if (i < ((int) paint.measureText(str))) {
                i = (int) paint.measureText(str);
            }
        }
        int width = (getWindowManager().getDefaultDisplay().getWidth() / 3) / (i + 32);
        if (width <= 2) {
            width = 2;
        }
        if (width > 6) {
            width = 6;
        }
        this.mGridViewLayoutMgr.setSpanCount(width);
        this.seriesAdapter.setNewData(this.vodInfo.seriesMap.get(this.vodInfo.playFlag));
        this.mGridView.postDelayed(new Runnable() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass11 */

            public void run() {
                DetailActivity.this.mGridView.scrollToPosition(DetailActivity.this.vodInfo.playIndex);
            }
        }, 100);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTextShow(TextView textView, String str, String str2) {
        if (str2 == null || str2.trim().isEmpty()) {
            textView.setVisibility(8);
            return;
        }
        textView.setVisibility(0);
        textView.setText(Html.fromHtml(getHtml(str, str2)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String removeHtmlTag(String str) {
        return str == null ? "" : str.replaceAll("\\<.*?\\>", "").replaceAll("\\s", "");
    }

    private void initViewModel() {
        SourceViewModel sourceViewModel2 = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
        this.sourceViewModel = sourceViewModel2;
        sourceViewModel2.detailResult.observe(this, new Observer<AbsXml>() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass12 */

            public void onChanged(AbsXml absXml) {
                if (absXml == null || absXml.movie == null || absXml.movie.videoList == null || absXml.movie.videoList.size() <= 0) {
                    DetailActivity.this.showEmpty();
                    DetailActivity.this.llPlayerFragmentContainer.setVisibility(8);
                    DetailActivity.this.llPlayerFragmentContainerBlock.setVisibility(8);
                    return;
                }
                DetailActivity.this.showSuccess();
                DetailActivity.this.mVideo = absXml.movie.videoList.get(0);
                DetailActivity.this.vodInfo = new VodInfo();
                DetailActivity.this.vodInfo.setVideo(DetailActivity.this.mVideo);
                DetailActivity.this.vodInfo.sourceKey = DetailActivity.this.mVideo.sourceKey;
                DetailActivity.this.tvName.setText(DetailActivity.this.mVideo.name);
                DetailActivity detailActivity = DetailActivity.this;
                detailActivity.setTextShow(detailActivity.tvSite, DetailActivity.this.getString(R.string.det_source), ApiConfig.get().getSource(DetailActivity.this.mVideo.sourceKey).getName());
                DetailActivity detailActivity2 = DetailActivity.this;
                detailActivity2.setTextShow(detailActivity2.tvYear, DetailActivity.this.getString(R.string.det_year), DetailActivity.this.mVideo.year == 0 ? "" : String.valueOf(DetailActivity.this.mVideo.year));
                DetailActivity detailActivity3 = DetailActivity.this;
                detailActivity3.setTextShow(detailActivity3.tvArea, DetailActivity.this.getString(R.string.det_area), DetailActivity.this.mVideo.area);
                DetailActivity detailActivity4 = DetailActivity.this;
                detailActivity4.setTextShow(detailActivity4.tvLang, DetailActivity.this.getString(R.string.det_lang), DetailActivity.this.mVideo.lang);
                DetailActivity detailActivity5 = DetailActivity.this;
                detailActivity5.setTextShow(detailActivity5.tvType, DetailActivity.this.getString(R.string.det_type), DetailActivity.this.mVideo.type);
                DetailActivity detailActivity6 = DetailActivity.this;
                detailActivity6.setTextShow(detailActivity6.tvActor, DetailActivity.this.getString(R.string.det_actor), DetailActivity.this.mVideo.actor);
                DetailActivity detailActivity7 = DetailActivity.this;
                detailActivity7.setTextShow(detailActivity7.tvDirector, DetailActivity.this.getString(R.string.det_dir), DetailActivity.this.mVideo.director);
                DetailActivity detailActivity8 = DetailActivity.this;
                TextView textView = detailActivity8.tvDes;
                String string = DetailActivity.this.getString(R.string.det_des);
                DetailActivity detailActivity9 = DetailActivity.this;
                detailActivity8.setTextShow(textView, string, detailActivity9.removeHtmlTag(detailActivity9.mVideo.des));
                if (!TextUtils.isEmpty(DetailActivity.this.mVideo.pic)) {
                    RequestCreator load = Picasso.get().load(DefaultConfig.checkReplaceProxy(DetailActivity.this.mVideo.pic));
                    load.transform(new RoundTransformation(MD5.string2MD5(DetailActivity.this.mVideo.pic + DetailActivity.this.mVideo.name)).centerCorp(true).override(AutoSizeUtils.mm2px(DetailActivity.this.mContext, 300.0f), AutoSizeUtils.mm2px(DetailActivity.this.mContext, 400.0f)).roundRadius(AutoSizeUtils.mm2px(DetailActivity.this.mContext, 15.0f), 0)).placeholder(R.drawable.img_loading_placeholder).error(R.drawable.img_loading_placeholder).into(DetailActivity.this.ivThumb);
                } else {
                    DetailActivity.this.ivThumb.setImageResource(R.drawable.img_loading_placeholder);
                }
                if (DetailActivity.this.vodInfo.seriesMap == null || DetailActivity.this.vodInfo.seriesMap.size() <= 0) {
                    DetailActivity.this.mGridViewFlag.setVisibility(8);
                    DetailActivity.this.mGridView.setVisibility(8);
                    DetailActivity.this.tvPlay.setVisibility(8);
                    DetailActivity.this.mEmptyPlayList.setVisibility(0);
                    return;
                }
                DetailActivity.this.mGridViewFlag.setVisibility(0);
                DetailActivity.this.mGridView.setVisibility(0);
                DetailActivity.this.mEmptyPlayList.setVisibility(8);
                VodInfo vodInfo = RoomDataManger.getVodInfo(DetailActivity.this.sourceKey, DetailActivity.this.vodId);
                if (vodInfo != null) {
                    DetailActivity.this.vodInfo.playIndex = Math.max(vodInfo.playIndex, 0);
                    DetailActivity.this.vodInfo.playFlag = vodInfo.playFlag;
                    DetailActivity.this.vodInfo.playerCfg = vodInfo.playerCfg;
                    DetailActivity.this.vodInfo.reverseSort = vodInfo.reverseSort;
                } else {
                    DetailActivity.this.vodInfo.playIndex = 0;
                    DetailActivity.this.vodInfo.playFlag = null;
                    DetailActivity.this.vodInfo.playerCfg = "";
                    DetailActivity.this.vodInfo.reverseSort = false;
                }
                if (DetailActivity.this.vodInfo.reverseSort) {
                    DetailActivity.this.vodInfo.reverse();
                }
                if (DetailActivity.this.vodInfo.playFlag == null || !DetailActivity.this.vodInfo.seriesMap.containsKey(DetailActivity.this.vodInfo.playFlag)) {
                    DetailActivity.this.vodInfo.playFlag = (String) DetailActivity.this.vodInfo.seriesMap.keySet().toArray()[0];
                }
                int i = 0;
                for (int i2 = 0; i2 < DetailActivity.this.vodInfo.seriesFlags.size(); i2++) {
                    VodInfo.VodSeriesFlag vodSeriesFlag = DetailActivity.this.vodInfo.seriesFlags.get(i2);
                    if (vodSeriesFlag.name.equals(DetailActivity.this.vodInfo.playFlag)) {
                        vodSeriesFlag.selected = true;
                        i = i2;
                    } else {
                        vodSeriesFlag.selected = false;
                    }
                }
                DetailActivity.this.seriesFlagAdapter.setNewData(DetailActivity.this.vodInfo.seriesFlags);
                DetailActivity.this.mGridViewFlag.scrollToPosition(i);
                DetailActivity.this.refreshList();
                if (DetailActivity.this.showPreview) {
                    DetailActivity.this.jumpToPlay();
                    DetailActivity.this.llPlayerFragmentContainer.setVisibility(0);
                    DetailActivity.this.llPlayerFragmentContainerBlock.setVisibility(0);
                    DetailActivity.this.llPlayerFragmentContainerBlock.requestFocus();
                    DetailActivity.this.toggleSubtitleTextSize();
                }
            }
        });
    }

    private String getHtml(String str, String str2) {
        if (str2 == null) {
            str2 = "";
        }
        if (str.length() > 0) {
            str = str + ": ";
        }
        return str + "<font color=\"#FFFFFF\">" + str2 + "</font>";
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            loadDetail(extras.getString("id", null), extras.getString("sourceKey", ""));
        }
    }

    private void loadDetail(String str, String str2) {
        if (str != null) {
            this.vodId = str;
            this.sourceKey = str2;
            showLoading();
            this.sourceViewModel.getDetail(this.sourceKey, this.vodId);
            if (RoomDataManger.isVodCollect(this.sourceKey, this.vodId)) {
                this.tvCollect.setText(getString(R.string.det_fav_star));
            } else {
                this.tvCollect.setText(getString(R.string.det_fav_unstar));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 0) {
            if (refreshEvent.obj == null) {
                return;
            }
            if (refreshEvent.obj instanceof Integer) {
                int intValue = ((Integer) refreshEvent.obj).intValue();
                if (intValue != this.vodInfo.playIndex) {
                    ((VodInfo.VodSeries) this.seriesAdapter.getData().get(this.vodInfo.playIndex)).selected = false;
                    this.seriesAdapter.notifyItemChanged(this.vodInfo.playIndex);
                    ((VodInfo.VodSeries) this.seriesAdapter.getData().get(intValue)).selected = true;
                    this.seriesAdapter.notifyItemChanged(intValue);
                    this.mGridView.setSelection(intValue);
                    this.vodInfo.playIndex = intValue;
                    insertVod(this.sourceKey, this.vodInfo);
                }
            } else if (refreshEvent.obj instanceof JSONObject) {
                this.vodInfo.playerCfg = refreshEvent.obj.toString();
                insertVod(this.sourceKey, this.vodInfo);
            }
        } else if (refreshEvent.type == 3) {
            if (refreshEvent.obj != null) {
                Movie.Video video = (Movie.Video) refreshEvent.obj;
                loadDetail(video.id, video.sourceKey);
            }
        } else if (refreshEvent.type == 5) {
            if (refreshEvent.obj != null) {
                switchSearchWord((String) refreshEvent.obj);
            }
        } else if (refreshEvent.type == 7) {
            try {
                searchData(refreshEvent.obj == null ? null : (AbsXml) refreshEvent.obj);
            } catch (Exception unused) {
                searchData(null);
            }
        }
    }

    private void switchSearchWord(String str) {
        OkGo.getInstance().cancelTag("quick_search");
        this.quickSearchData.clear();
        this.searchTitle = str;
        searchResult();
    }

    private void initCheckedSourcesForSearch() {
        this.mCheckSources = SearchHelper.getSourcesForSearch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startQuickSearch() {
        initCheckedSourcesForSearch();
        if (!this.hadQuickStart) {
            this.hadQuickStart = true;
            OkGo.getInstance().cancelTag("quick_search");
            this.quickSearchWord.clear();
            this.searchTitle = this.mVideo.name;
            this.quickSearchData.clear();
            this.quickSearchWord.add(this.searchTitle);
            ((GetRequest) OkGo.get("http://api.pullword.com/get.php?source=" + URLEncoder.encode(this.searchTitle) + "&param1=0&param2=0&json=1").tag("fenci")).execute(new AbsCallback<String>() {
                /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass13 */

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
                    DetailActivity.this.quickSearchWord.clear();
                    try {
                        Iterator<JsonElement> it = ((JsonArray) new Gson().fromJson(body, JsonArray.class)).iterator();
                        while (it.hasNext()) {
                            DetailActivity.this.quickSearchWord.add(it.next().getAsJsonObject().get(an.aI).getAsString());
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    DetailActivity.this.quickSearchWord.add(DetailActivity.this.searchTitle);
                    EventBus.getDefault().post(new RefreshEvent(4, DetailActivity.this.quickSearchWord));
                }

                @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                public void onError(com.lzy.okgo.model.Response<String> response) {
                    super.onError(response);
                }
            });
            searchResult();
        }
    }

    private void searchResult() {
        try {
            ExecutorService executorService = this.searchExecutorService;
            if (executorService != null) {
                executorService.shutdownNow();
                this.searchExecutorService = null;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.searchExecutorService = Executors.newFixedThreadPool(5);
        ArrayList<SourceBean> arrayList = new ArrayList();
        arrayList.addAll(ApiConfig.get().getSourceBeanList());
        SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
        arrayList.remove(homeSourceBean);
        arrayList.add(0, homeSourceBean);
        ArrayList arrayList2 = new ArrayList();
        for (SourceBean sourceBean : arrayList) {
            if (sourceBean.isSearchable() && sourceBean.isQuickSearch()) {
                HashMap<String, String> hashMap = this.mCheckSources;
                if (hashMap == null || hashMap.containsKey(sourceBean.getKey())) {
                    arrayList2.add(sourceBean.getKey());
                }
            }
        }
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            final String str = (String) it.next();
            this.searchExecutorService.execute(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass14 */

                public void run() {
                    DetailActivity.this.sourceViewModel.getQuickSearch(str, DetailActivity.this.searchTitle);
                }
            });
        }
    }

    private void searchData(AbsXml absXml) {
        if (!(absXml == null || absXml.movie == null || absXml.movie.videoList == null || absXml.movie.videoList.size() <= 0)) {
            ArrayList arrayList = new ArrayList();
            for (Movie.Video video : absXml.movie.videoList) {
                if (!video.sourceKey.equals(this.sourceKey) || !video.id.equals(this.vodId)) {
                    arrayList.add(video);
                }
            }
            this.quickSearchData.addAll(arrayList);
            EventBus.getDefault().post(new RefreshEvent(2, arrayList));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void insertVod(String str, VodInfo vodInfo2) {
        try {
            vodInfo2.playNote = vodInfo2.seriesMap.get(vodInfo2.playFlag).get(vodInfo2.playIndex).name;
        } catch (Throwable unused) {
            vodInfo2.playNote = "";
        }
        RoomDataManger.insertVodRecord(str, vodInfo2);
        EventBus.getDefault().post(new RefreshEvent(1));
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        try {
            ExecutorService executorService = this.searchExecutorService;
            if (executorService != null) {
                executorService.shutdownNow();
                this.searchExecutorService = null;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        OkGo.getInstance().cancelTag("fenci");
        OkGo.getInstance().cancelTag("detail");
        OkGo.getInstance().cancelTag("quick_search");
        EventBus.getDefault().unregister(this);
    }

    public void onUserLeaveHint() {
        Rational rational;
        if (supportsPiPMode() && this.showPreview && !playFragment.extPlay && this.PiPON) {
            int i = playFragment.mVideoView.getVideoSize()[0];
            int i2 = playFragment.mVideoView.getVideoSize()[1];
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
            arrayList.add(generateRemoteAction(17301540, 1, "Play", "Play/Pause"));
            arrayList.add(generateRemoteAction(17301538, 2, "Next", "Play Next"));
            PictureInPictureParams build = new PictureInPictureParams.Builder().setAspectRatio(rational).setActions(arrayList).build();
            if (!this.fullWindows) {
                toggleFullPreview();
            }
            enterPictureInPictureMode(build);
            playFragment.getVodController().hideBottom();
        }
        super.onUserLeaveHint();
    }

    private RemoteAction generateRemoteAction(int i, int i2, String str, String str2) {
        return new RemoteAction(Icon.createWithResource(this, i), str, str2, PendingIntent.getBroadcast(this, i2, new Intent("PIP_VOD_CONTROL").putExtra("action", i2), 0));
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onPictureInPictureModeChanged(boolean z) {
        super.onPictureInPictureModeChanged(z);
        if (!supportsPiPMode() || !z) {
            unregisterReceiver(this.pipActionReceiver);
            this.pipActionReceiver = null;
            return;
        }
        AnonymousClass15 r3 = new BroadcastReceiver() {
            /* class com.github.tvbox.osc.ui.activity.DetailActivity.AnonymousClass15 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals("PIP_VOD_CONTROL") && DetailActivity.playFragment.getVodController() != null) {
                    int intExtra = intent.getIntExtra("action", 1);
                    if (intExtra == 0) {
                        DetailActivity.playFragment.playPrevious();
                    } else if (intExtra == 1) {
                        DetailActivity.playFragment.getVodController().togglePlay();
                    } else if (intExtra == 2) {
                        DetailActivity.playFragment.playNext(false);
                    }
                }
            }
        };
        this.pipActionReceiver = r3;
        registerReceiver(r3, new IntentFilter("PIP_VOD_CONTROL"));
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        View view;
        if (this.fullWindows) {
            if (!playFragment.onBackPressed()) {
                toggleFullPreview();
                this.mGridView.requestFocus();
            }
        } else if (!this.seriesSelect || (view = this.seriesFlagFocus) == null || view.isFocused()) {
            super.onBackPressed();
        } else {
            this.seriesFlagFocus.requestFocus();
        }
    }

    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        PlayFragment playFragment2;
        if (keyEvent == null || (playFragment2 = playFragment) == null || !this.fullWindows || !playFragment2.dispatchKeyEvent(keyEvent)) {
            return super.dispatchKeyEvent(keyEvent);
        }
        return true;
    }

    public void toggleFullPreview() {
        if (this.windowsPreview == null) {
            this.windowsPreview = this.llPlayerFragmentContainer.getLayoutParams();
        }
        if (this.windowsFull == null) {
            this.windowsFull = new FrameLayout.LayoutParams(-1, -1);
        }
        boolean z = !this.fullWindows;
        this.fullWindows = z;
        this.llPlayerFragmentContainer.setLayoutParams(z ? this.windowsFull : this.windowsPreview);
        int i = 8;
        this.llPlayerFragmentContainerBlock.setVisibility(this.fullWindows ? 8 : 0);
        this.mGridView.setVisibility(this.fullWindows ? 8 : 0);
        TvRecyclerView tvRecyclerView = this.mGridViewFlag;
        if (!this.fullWindows) {
            i = 0;
        }
        tvRecyclerView.setVisibility(i);
        this.tvPlay.setFocusable(!this.fullWindows);
        this.tvSort.setFocusable(!this.fullWindows);
        this.tvCollect.setFocusable(!this.fullWindows);
        this.tvQuickSearch.setFocusable(!this.fullWindows);
        toggleSubtitleTextSize();
        if (this.fullWindows) {
            hideSystemUI(false);
        } else {
            showSystemUI();
        }
    }

    /* access modifiers changed from: package-private */
    public void toggleSubtitleTextSize() {
        int textSize = SubtitleHelper.getTextSize(this);
        if (!this.fullWindows) {
            double d = (double) textSize;
            Double.isNaN(d);
            textSize = (int) (d * 0.5d);
        }
        EventBus.getDefault().post(new RefreshEvent(12, Integer.valueOf(textSize)));
    }
}
