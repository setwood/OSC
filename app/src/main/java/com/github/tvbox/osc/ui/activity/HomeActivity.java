package com.github.tvbox.osc.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewpager.widget.ViewPager;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.adapter.HomePageAdapter;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.adapter.SortAdapter;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.TipDialog;
import com.github.tvbox.osc.ui.fragment.GridFragment;
import com.github.tvbox.osc.ui.fragment.UserFragment;
import com.github.tvbox.osc.ui.tv.widget.DefaultTransformer;
import com.github.tvbox.osc.ui.tv.widget.FixedSpeedScroller;
import com.github.tvbox.osc.ui.tv.widget.NoScrollViewPager;
import com.github.tvbox.osc.ui.tv.widget.ViewObj;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.SourceUtil;
import com.github.tvbox.osc.util.ToastHelper;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.hjq.permissions.Permission;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;

public class HomeActivity extends BaseActivity {
    private static Resources res;
    boolean HomeShow = ((Boolean) Hawk.get(HawkConfig.HOME_SHOW_SOURCE, false)).booleanValue();
    private LinearLayout contentLayout;
    private int currentSelected = 0;
    private View currentView;
    private boolean dataInitOk = false;
    private final List<BaseLazyFragment> fragments = new ArrayList();
    private boolean isDownOrUp = false;
    private boolean jarInitOk = false;
    private final Runnable mDataRunnable = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass15 */

        public void run() {
            if (HomeActivity.this.sortChange) {
                boolean z = false;
                HomeActivity.this.sortChange = false;
                if (HomeActivity.this.sortFocused != HomeActivity.this.currentSelected) {
                    HomeActivity homeActivity = HomeActivity.this;
                    homeActivity.currentSelected = homeActivity.sortFocused;
                    HomeActivity.this.mViewPager.setCurrentItem(HomeActivity.this.sortFocused, false);
                    HomeActivity homeActivity2 = HomeActivity.this;
                    if (homeActivity2.sortFocused != 0) {
                        z = true;
                    }
                    homeActivity2.changeTop(z);
                }
            }
        }
    };
    private long mExitTime = 0;
    private TvRecyclerView mGridView;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass1 */

        public void run() {
            Date date = new Date();
            HomeActivity.this.tvDate.setText(new SimpleDateFormat(HomeActivity.this.getString(R.string.hm_date1) + ", " + HomeActivity.this.getString(R.string.hm_date2)).format(date));
            HomeActivity.this.mHandler.postDelayed(this, 1000);
        }
    };
    private NoScrollViewPager mViewPager;
    private HomePageAdapter pageAdapter;
    private SortAdapter sortAdapter;
    private boolean sortChange = false;
    public View sortFocusView = null;
    private int sortFocused = 0;
    private SourceViewModel sourceViewModel;
    byte topHide = 0;
    private LinearLayout topLayout;
    private ImageView tvApiHistory;
    private ImageView tvClearCache;
    private TextView tvDate;
    private ImageView tvFind;
    private ImageView tvMenu;
    private TextView tvName;
    private ImageView tvWifi;
    boolean useCacheConfig = false;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_home;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        res = getResources();
        EventBus.getDefault().register(this);
        ControlManager.get().startServer();
        initView();
        initViewModel();
        this.useCacheConfig = false;
        Intent intent = getIntent();
        if (!(intent == null || intent.getExtras() == null)) {
            this.useCacheConfig = intent.getExtras().getBoolean("useCache", false);
        }
        initData();
    }

    public static Resources getRes() {
        return res;
    }

    private void initView() {
        this.topLayout = (LinearLayout) findViewById(R.id.topLayout);
        this.tvName = (TextView) findViewById(R.id.tvName);
        this.tvWifi = (ImageView) findViewById(R.id.tvWifi);
        this.tvFind = (ImageView) findViewById(R.id.tvFind);
        this.tvMenu = (ImageView) findViewById(R.id.tvMenu);
        this.tvApiHistory = (ImageView) findViewById(R.id.tvApiHistory);
        this.tvClearCache = (ImageView) findViewById(R.id.tvClearCache);
        this.tvDate = (TextView) findViewById(R.id.tvDate);
        this.contentLayout = (LinearLayout) findViewById(R.id.contentLayout);
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridViewCategory);
        this.mViewPager = (NoScrollViewPager) findViewById(R.id.mViewPager);
        this.sortAdapter = new SortAdapter();
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 0, false));
        this.mGridView.setSpacingWithMargins(0, AutoSizeUtils.dp2px(this.mContext, 10.0f));
        this.mGridView.setAdapter(this.sortAdapter);
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass2 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (view != null && !HomeActivity.this.isDownOrUp) {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start();
                    TextView textView = (TextView) view.findViewById(R.id.tvTitle);
                    textView.getPaint().setFakeBoldText(false);
                    textView.setTextColor(HomeActivity.this.getResources().getColor(R.color.color_FFFFFF_70));
                    textView.invalidate();
                    view.findViewById(R.id.tvFilter).setVisibility(8);
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (view != null) {
                    HomeActivity.this.currentView = view;
                    HomeActivity.this.isDownOrUp = false;
                    HomeActivity.this.sortChange = true;
                    view.animate().scaleX(1.1f).scaleY(1.1f).setInterpolator(new BounceInterpolator()).setDuration(250).start();
                    TextView textView = (TextView) view.findViewById(R.id.tvTitle);
                    textView.getPaint().setFakeBoldText(true);
                    textView.setTextColor(HomeActivity.this.getResources().getColor(2131034178));
                    textView.invalidate();
                    MovieSort.SortData sortData = (MovieSort.SortData) HomeActivity.this.sortAdapter.getItem(i);
                    if (!sortData.filters.isEmpty()) {
                        HomeActivity.this.showFilterIcon(sortData.filterSelectCount());
                    }
                    HomeActivity.this.sortFocusView = view;
                    HomeActivity.this.sortFocused = i;
                    HomeActivity.this.mHandler.removeCallbacks(HomeActivity.this.mDataRunnable);
                    HomeActivity.this.mHandler.postDelayed(HomeActivity.this.mDataRunnable, 200);
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                if (view != null && HomeActivity.this.currentSelected == i) {
                    BaseLazyFragment baseLazyFragment = (BaseLazyFragment) HomeActivity.this.fragments.get(HomeActivity.this.currentSelected);
                    if ((baseLazyFragment instanceof GridFragment) && !((MovieSort.SortData) HomeActivity.this.sortAdapter.getItem(i)).filters.isEmpty()) {
                        ((GridFragment) baseLazyFragment).showFilter();
                    } else if (baseLazyFragment instanceof UserFragment) {
                        HomeActivity.this.showSiteSwitch();
                    }
                }
            }
        });
        this.mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass3 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
            public final boolean onInBorderKeyEvent(int i, View view) {
                if (i == 33) {
                    BaseLazyFragment baseLazyFragment = (BaseLazyFragment) HomeActivity.this.fragments.get(HomeActivity.this.sortFocused);
                    if (baseLazyFragment instanceof GridFragment) {
                        ((GridFragment) baseLazyFragment).forceRefresh();
                    }
                }
                if (i != 130) {
                    return false;
                }
                BaseLazyFragment baseLazyFragment2 = (BaseLazyFragment) HomeActivity.this.fragments.get(HomeActivity.this.sortFocused);
                if (!(baseLazyFragment2 instanceof GridFragment)) {
                    return false;
                }
                return !((GridFragment) baseLazyFragment2).isLoad();
            }
        });
        this.tvClearCache.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                File file = new File(FileUtils.getCachePath());
                if (!file.exists()) {
                    HomeActivity homeActivity = HomeActivity.this;
                    Toast.makeText(homeActivity, homeActivity.getString(R.string.hm_no_cache), 1).show();
                    return;
                }
                try {
                    new Thread(new Runnable(file) {
                        /* class com.github.tvbox.osc.ui.activity.$$Lambda$HomeActivity$4$7b3lqYqTdEhdId1fCK_hdxVNzI */
                        public final /* synthetic */ File f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void run() {
                            HomeActivity.AnonymousClass4.lambda$onClick$0(this.f$0);
                        }
                    }).start();
                    HomeActivity homeActivity2 = HomeActivity.this;
                    Toast.makeText(homeActivity2, homeActivity2.getString(R.string.hm_cache_clear), 1).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    HomeActivity homeActivity3 = HomeActivity.this;
                    Toast.makeText(homeActivity3, homeActivity3.getString(R.string.hm_cache_clear_error), 1).show();
                }
            }

            static /* synthetic */ void lambda$onClick$0(File file) {
                try {
                    FileUtils.cleanDirectory(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.tvClearCache.setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass5 */

            public boolean onLongClick(View view) {
                Intent intent = new Intent(HomeActivity.this.getApplicationContext(), HomeActivity.class);
                intent.setFlags(32768);
                Bundle bundle = new Bundle();
                bundle.putBoolean("useCache", true);
                intent.putExtras(bundle);
                HomeActivity.this.startActivity(intent);
                return true;
            }
        });
        this.tvApiHistory.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass6 */

            public void onClick(View view) {
                HomeActivity.this.showApiSwitch();
            }
        });
        this.tvWifi.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass7 */

            public void onClick(View view) {
                HomeActivity.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        });
        this.tvFind.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass8 */

            public void onClick(View view) {
                HomeActivity.this.jumpActivity(SearchActivity.class);
            }
        });
        this.tvMenu.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass9 */

            public void onClick(View view) {
                HomeActivity.this.jumpActivity(SettingActivity.class);
            }
        });
        this.tvMenu.setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass10 */

            public boolean onLongClick(View view) {
                HomeActivity.this.startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", HomeActivity.this.getPackageName(), null)));
                return true;
            }
        });
        this.tvDate.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass11 */

            public void onClick(View view) {
                HomeActivity.this.startActivity(new Intent("android.settings.DATE_SETTINGS"));
            }
        });
        setLoadSir(this.contentLayout);
    }

    private void initViewModel() {
        SourceViewModel sourceViewModel2 = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
        this.sourceViewModel = sourceViewModel2;
        sourceViewModel2.sortResult.observe(this, new Observer<AbsSortXml>() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass12 */

            public void onChanged(AbsSortXml absSortXml) {
                HomeActivity.this.showSuccess();
                if (absSortXml == null || absSortXml.classes == null || absSortXml.classes.sortList == null) {
                    HomeActivity.this.sortAdapter.setNewData(DefaultConfig.adjustSort(ApiConfig.get().getHomeSourceBean().getKey(), new ArrayList(), true));
                } else {
                    HomeActivity.this.sortAdapter.setNewData(DefaultConfig.adjustSort(ApiConfig.get().getHomeSourceBean().getKey(), absSortXml.classes.sortList, true));
                }
                HomeActivity.this.initViewPager(absSortXml);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initData() {
        SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
        if (this.HomeShow && homeSourceBean != null && homeSourceBean.getName() != null && !homeSourceBean.getName().isEmpty()) {
            this.tvName.setText(homeSourceBean.getName());
        }
        if (isNetworkAvailable()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
            if (connectivityManager.getActiveNetworkInfo().getType() == 1) {
                this.tvWifi.setImageDrawable(res.getDrawable(R.drawable.hm_wifi));
            } else if (connectivityManager.getActiveNetworkInfo().getType() == 0) {
                this.tvWifi.setImageDrawable(res.getDrawable(R.drawable.hm_mobile));
            } else if (connectivityManager.getActiveNetworkInfo().getType() == 9) {
                this.tvWifi.setImageDrawable(res.getDrawable(R.drawable.hm_lan));
            }
        }
        this.mGridView.requestFocus();
        if (!this.dataInitOk || !this.jarInitOk) {
            showLoading();
            if (!this.dataInitOk || this.jarInitOk) {
                ApiConfig.get().loadConfig(this.useCacheConfig, new ApiConfig.LoadConfigCallback() {
                    /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14 */
                    TipDialog dialog = null;

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void retry() {
                        HomeActivity.this.mHandler.post(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass1 */

                            public void run() {
                                HomeActivity.this.initData();
                            }
                        });
                    }

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void success() {
                        HomeActivity.this.dataInitOk = true;
                        if (ApiConfig.get().getSpider().isEmpty()) {
                            HomeActivity.this.jarInitOk = true;
                        }
                        HomeActivity.this.mHandler.postDelayed(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass2 */

                            public void run() {
                                HomeActivity.this.initData();
                            }
                        }, 50);
                    }

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void error(final String str) {
                        if (str.equalsIgnoreCase("-1")) {
                            HomeActivity.this.mHandler.post(new Runnable() {
                                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass3 */

                                public void run() {
                                    String string = HomeActivity.getRes().getString(R.string.app_source);
                                    if (StringUtils.isNotEmpty(string)) {
                                        SourceUtil.replaceAllSource(string, new SourceUtil.Callback<String>() {
                                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass3.AnonymousClass1 */

                                            public void success(String str) {
                                                HomeActivity.this.initData();
                                            }

                                            @Override // com.github.tvbox.osc.util.SourceUtil.Callback
                                            public void error(String str) {
                                                ToastHelper.showToast(HomeActivity.this, str);
                                            }
                                        });
                                        return;
                                    }
                                    HomeActivity.this.dataInitOk = true;
                                    HomeActivity.this.jarInitOk = true;
                                    HomeActivity.this.initData();
                                }
                            });
                        } else {
                            HomeActivity.this.mHandler.post(new Runnable() {
                                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass4 */

                                public void run() {
                                    if (AnonymousClass14.this.dialog == null) {
                                        AnonymousClass14.this.dialog = new TipDialog(HomeActivity.this, str, HomeActivity.this.getString(R.string.hm_retry), HomeActivity.this.getString(R.string.hm_cancel), new TipDialog.OnListener() {
                                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass4.AnonymousClass1 */

                                            @Override // com.github.tvbox.osc.ui.dialog.TipDialog.OnListener
                                            public void left() {
                                                HomeActivity.this.mHandler.post(new Runnable() {
                                                    /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass4.AnonymousClass1.AnonymousClass1 */

                                                    public void run() {
                                                        HomeActivity.this.initData();
                                                        AnonymousClass14.this.dialog.hide();
                                                    }
                                                });
                                            }

                                            @Override // com.github.tvbox.osc.ui.dialog.TipDialog.OnListener
                                            public void right() {
                                                HomeActivity.this.dataInitOk = true;
                                                HomeActivity.this.jarInitOk = true;
                                                HomeActivity.this.mHandler.post(new Runnable() {
                                                    /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass4.AnonymousClass1.AnonymousClass2 */

                                                    public void run() {
                                                        HomeActivity.this.initData();
                                                        AnonymousClass14.this.dialog.hide();
                                                    }
                                                });
                                            }

                                            @Override // com.github.tvbox.osc.ui.dialog.TipDialog.OnListener
                                            public void cancel() {
                                                HomeActivity.this.dataInitOk = true;
                                                HomeActivity.this.jarInitOk = true;
                                                HomeActivity.this.mHandler.post(new Runnable() {
                                                    /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass14.AnonymousClass4.AnonymousClass1.AnonymousClass3 */

                                                    public void run() {
                                                        HomeActivity.this.initData();
                                                        AnonymousClass14.this.dialog.hide();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    if (!AnonymousClass14.this.dialog.isShowing()) {
                                        AnonymousClass14.this.dialog.show();
                                    }
                                }
                            });
                        }
                    }
                }, this);
            } else if (!ApiConfig.get().getSpider().isEmpty()) {
                ApiConfig.get().loadJar(this.useCacheConfig, ApiConfig.get().getSpider(), new ApiConfig.LoadConfigCallback() {
                    /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass13 */

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void retry() {
                    }

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void success() {
                        HomeActivity.this.jarInitOk = true;
                        HomeActivity.this.mHandler.postDelayed(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass13.AnonymousClass1 */

                            public void run() {
                                if (!HomeActivity.this.useCacheConfig) {
                                    Toast.makeText(HomeActivity.this, HomeActivity.this.getString(R.string.hm_ok), 0).show();
                                }
                                HomeActivity.this.initData();
                            }
                        }, 50);
                    }

                    @Override // com.github.tvbox.osc.api.ApiConfig.LoadConfigCallback
                    public void error(String str) {
                        HomeActivity.this.jarInitOk = true;
                        HomeActivity.this.mHandler.post(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass13.AnonymousClass2 */

                            public void run() {
                                Toast.makeText(HomeActivity.this, HomeActivity.this.getString(R.string.hm_notok), 0).show();
                                HomeActivity.this.initData();
                            }
                        });
                    }
                });
            }
        } else {
            showLoading();
            this.sourceViewModel.getSort(ApiConfig.get().getHomeSourceBean().getKey());
            if (hasPermission(Permission.WRITE_EXTERNAL_STORAGE)) {
                LOG.e("有");
            } else {
                LOG.e("无");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initViewPager(AbsSortXml absSortXml) {
        if (this.sortAdapter.getData().size() > 0) {
            for (MovieSort.SortData sortData : this.sortAdapter.getData()) {
                if (!sortData.id.equals("my0")) {
                    this.fragments.add(GridFragment.newInstance(sortData));
                } else if (((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() != 1 || absSortXml == null || absSortXml.videoList == null || absSortXml.videoList.size() <= 0) {
                    this.fragments.add(UserFragment.newInstance(null));
                } else {
                    this.fragments.add(UserFragment.newInstance(absSortXml.videoList));
                }
            }
            this.pageAdapter = new HomePageAdapter(getSupportFragmentManager(), this.fragments);
            try {
                Field declaredField = ViewPager.class.getDeclaredField("mScroller");
                declaredField.setAccessible(true);
                FixedSpeedScroller fixedSpeedScroller = new FixedSpeedScroller(this.mContext, new AccelerateInterpolator());
                declaredField.set(this.mViewPager, fixedSpeedScroller);
                fixedSpeedScroller.setmDuration(IjkMediaCodecInfo.RANK_SECURE);
            } catch (Exception unused) {
            }
            this.mViewPager.setPageTransformer(true, new DefaultTransformer());
            this.mViewPager.setAdapter(this.pageAdapter);
            this.mViewPager.setCurrentItem(this.currentSelected, false);
        }
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        int i;
        if (this.fragments.size() <= 0 || this.sortFocused >= this.fragments.size() || (i = this.sortFocused) < 0) {
            exit();
            return;
        }
        BaseLazyFragment baseLazyFragment = this.fragments.get(i);
        if (baseLazyFragment instanceof GridFragment) {
            View view = this.sortFocusView;
            if (!((GridFragment) baseLazyFragment).restoreView()) {
                if (view != null && !view.isFocused()) {
                    this.sortFocusView.requestFocus();
                } else if (this.sortFocused != 0) {
                    this.mGridView.setSelection(0);
                } else {
                    exit();
                }
            }
        } else {
            exit();
        }
    }

    private void exit() {
        if (System.currentTimeMillis() - this.mExitTime < SimpleExoPlayer.DEFAULT_DETACH_SURFACE_TIMEOUT_MS) {
            EventBus.getDefault().unregister(this);
            AppManager.getInstance().appExit(0);
            ControlManager.get().stopServer();
            finish();
            super.onBackPressed();
            return;
        }
        this.mExitTime = System.currentTimeMillis();
        Toast.makeText(this.mContext, getString(R.string.hm_exit), 0).show();
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onResume() {
        super.onResume();
        this.mHandler.post(this.mRunnable);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity
    public void onPause() {
        super.onPause();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 9) {
            if (ApiConfig.get().getSource("push_agent") != null) {
                Intent intent = new Intent(this.mContext, DetailActivity.class);
                intent.putExtra("id", (String) refreshEvent.obj);
                intent.putExtra("sourceKey", "push_agent");
                intent.setFlags(335544320);
                startActivity(intent);
            }
        } else if (refreshEvent.type == 16 && this.currentView != null) {
            showFilterIcon(((Integer) refreshEvent.obj).intValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showFilterIcon(int i) {
        boolean z = i > 0;
        this.currentView.findViewById(R.id.tvFilter).setVisibility(0);
        ((ImageView) this.currentView.findViewById(R.id.tvFilter)).setColorFilter(z ? getResources().getColor(R.color.color_theme) : -1);
    }

    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.topHide < 0) {
            return false;
        }
        if (keyEvent.getAction() != 0) {
            keyEvent.getAction();
        } else if (keyEvent.getKeyCode() == 82) {
            showSiteSwitch();
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeTop(final boolean z) {
        LinearLayout linearLayout = this.topLayout;
        ViewObj viewObj = new ViewObj(linearLayout, (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() {
            /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass16 */

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                HomeActivity.this.topHide = z ? (byte) 1 : 0;
            }
        });
        if (z && this.topHide == 0) {
            animatorSet.playTogether(ObjectAnimator.ofObject(viewObj, "marginTop", new IntEvaluator(), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 20.0f)), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 0.0f))), ObjectAnimator.ofObject(viewObj, "height", new IntEvaluator(), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 50.0f)), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 1.0f))), ObjectAnimator.ofFloat(this.topLayout, "alpha", 1.0f, 0.0f));
            animatorSet.setDuration(250L);
            animatorSet.start();
            this.tvName.setFocusable(false);
            this.tvWifi.setFocusable(false);
            this.tvFind.setFocusable(false);
            this.tvMenu.setFocusable(false);
            this.tvClearCache.setFocusable(false);
        } else if (!z && this.topHide == 1) {
            animatorSet.playTogether(ObjectAnimator.ofObject(viewObj, "marginTop", new IntEvaluator(), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 0.0f)), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 20.0f))), ObjectAnimator.ofObject(viewObj, "height", new IntEvaluator(), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 1.0f)), Integer.valueOf(AutoSizeUtils.mm2px(this.mContext, 50.0f))), ObjectAnimator.ofFloat(this.topLayout, "alpha", 0.0f, 1.0f));
            animatorSet.setDuration(250L);
            animatorSet.start();
            this.tvName.setFocusable(false);
            this.tvWifi.setFocusable(true);
            this.tvFind.setFocusable(true);
            this.tvMenu.setFocusable(true);
            this.tvClearCache.setFocusable(true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        AppManager.getInstance().appExit(0);
        ControlManager.get().stopServer();
    }

    /* access modifiers changed from: package-private */
    public void showApiSwitch() {
        List<String> historyApiUrls = SourceUtil.getHistoryApiUrls();
        if (!historyApiUrls.isEmpty()) {
            String url = SourceUtil.getCurrentApi().getUrl();
            int i = 0;
            if (historyApiUrls.contains(url)) {
                i = historyApiUrls.indexOf(url);
            }
            ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(this);
            apiHistoryDialog.setTip(getString(R.string.dia_history_list));
            apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass17 */

                @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                public void click(String str) {
                    SourceUtil.setCurrentApi(str);
                    ApiConfig.get().clearSourceBeanList();
                    Intent intent = new Intent(HomeActivity.this.getApplicationContext(), HomeActivity.class);
                    intent.setFlags(32768);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("useCache", true);
                    intent.putExtras(bundle);
                    HomeActivity.this.startActivity(intent);
                }

                @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                public void del(String str, ArrayList<String> arrayList) {
                    SourceUtil.removeHistory(str);
                }
            }, historyApiUrls, i);
            apiHistoryDialog.show();
        }
    }

    /* access modifiers changed from: package-private */
    public void showSiteSwitch() {
        List<SourceBean> sourceBeanList = ApiConfig.get().getSourceBeanList();
        if (sourceBeanList.size() > 0) {
            SelectDialog selectDialog = new SelectDialog(this);
            int floor = (int) Math.floor((double) (sourceBeanList.size() / 10));
            if (floor <= 1) {
                floor = 1;
            }
            if (floor >= 3) {
                floor = 3;
            }
            ((TvRecyclerView) selectDialog.findViewById(R.id.list)).setLayoutManager(new V7GridLayoutManager(selectDialog.getContext(), floor));
            ViewGroup.LayoutParams layoutParams = ((LinearLayout) selectDialog.findViewById(R.id.cl_root)).getLayoutParams();
            if (floor != 1) {
                layoutParams.width = AutoSizeUtils.mm2px(selectDialog.getContext(), (float) (((floor - 1) * 260) + 400));
            }
            selectDialog.setTip(getString(R.string.dia_source));
            selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<SourceBean>() {
                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass18 */

                public void click(SourceBean sourceBean, int i) {
                    ApiConfig.get().setSourceBean(sourceBean);
                    Intent intent = new Intent(HomeActivity.this.getApplicationContext(), HomeActivity.class);
                    intent.setFlags(32768);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("useCache", true);
                    intent.putExtras(bundle);
                    HomeActivity.this.startActivity(intent);
                }

                public String getDisplay(SourceBean sourceBean) {
                    return sourceBean.getName();
                }
            }, new DiffUtil.ItemCallback<SourceBean>() {
                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass19 */

                public boolean areItemsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                    return sourceBean == sourceBean2;
                }

                public boolean areContentsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                    return sourceBean.getKey().equals(sourceBean2.getKey());
                }
            }, sourceBeanList, sourceBeanList.indexOf(ApiConfig.get().getHomeSourceBean()));
            selectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.github.tvbox.osc.ui.activity.HomeActivity.AnonymousClass20 */

                public void onDismiss(DialogInterface dialogInterface) {
                }
            });
            selectDialog.show();
        }
    }
}
