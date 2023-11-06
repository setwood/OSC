package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.viewpager.widget.ViewPager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.ui.adapter.SettingMenuAdapter;
import com.github.tvbox.osc.ui.adapter.SettingPageAdapter;
import com.github.tvbox.osc.ui.fragment.ModelSettingFragment;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity {
    public static DevModeCallback callback;
    private String currentApi;
    private String currentLive;
    private int defaultSelected = 0;
    String devMode = "";
    private int dnsOpt;
    private List<BaseLazyFragment> fragments = new ArrayList();
    private int homeRec;
    private String homeSourceKey;
    private final Runnable mDataRunnable = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.SettingActivity.AnonymousClass3 */

        public void run() {
            if (SettingActivity.this.sortChange) {
                SettingActivity.this.sortChange = false;
                if (SettingActivity.this.sortFocused != SettingActivity.this.defaultSelected) {
                    SettingActivity settingActivity = SettingActivity.this;
                    settingActivity.defaultSelected = settingActivity.sortFocused;
                    SettingActivity.this.mViewPager.setCurrentItem(SettingActivity.this.sortFocused, false);
                }
            }
        }
    };
    private final Runnable mDevModeRun = new Runnable() {
        /* class com.github.tvbox.osc.ui.activity.SettingActivity.AnonymousClass4 */

        public void run() {
            SettingActivity.this.devMode = "";
        }
    };
    private TvRecyclerView mGridView;
    private Handler mHandler = new Handler();
    private ViewPager mViewPager;
    private SettingPageAdapter pageAdapter;
    private SettingMenuAdapter sortAdapter;
    private boolean sortChange = false;
    private int sortFocused = 0;

    public interface DevModeCallback {
        void onChange();
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_setting;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        initView();
        initData();
    }

    private void initView() {
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        SettingMenuAdapter settingMenuAdapter = new SettingMenuAdapter();
        this.sortAdapter = settingMenuAdapter;
        this.mGridView.setAdapter(settingMenuAdapter);
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        this.sortAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SettingActivity.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (view.getId() == 2131296992 && view.getParent() != null) {
                    ((ViewGroup) view.getParent()).requestFocus();
                    SettingActivity.this.sortFocused = i;
                    if (SettingActivity.this.sortFocused != SettingActivity.this.defaultSelected) {
                        SettingActivity settingActivity = SettingActivity.this;
                        settingActivity.defaultSelected = settingActivity.sortFocused;
                        SettingActivity.this.mViewPager.setCurrentItem(SettingActivity.this.sortFocused, false);
                    }
                }
            }
        });
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.SettingActivity.AnonymousClass2 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (view != null) {
                    ((TextView) view.findViewById(R.id.tvName)).setTextColor(SettingActivity.this.getResources().getColor(R.color.color_FFFFFF_70));
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (view != null) {
                    SettingActivity.this.sortChange = true;
                    SettingActivity.this.sortFocused = i;
                    ((TextView) view.findViewById(R.id.tvName)).setTextColor(-1);
                }
            }
        });
    }

    private void initData() {
        this.currentApi = (String) Hawk.get(HawkConfig.API_URL, "");
        this.currentLive = (String) Hawk.get(HawkConfig.LIVE_URL, "");
        this.homeSourceKey = ApiConfig.get().getHomeSourceBean().getKey();
        this.homeRec = ((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue();
        this.dnsOpt = ((Integer) Hawk.get(HawkConfig.DOH_URL, 0)).intValue();
        ArrayList arrayList = new ArrayList();
        arrayList.add("设置其他");
        this.sortAdapter.setNewData(arrayList);
        initViewPager();
    }

    private void initViewPager() {
        this.fragments.add(ModelSettingFragment.newInstance());
        SettingPageAdapter settingPageAdapter = new SettingPageAdapter(getSupportFragmentManager(), this.fragments);
        this.pageAdapter = settingPageAdapter;
        this.mViewPager.setAdapter(settingPageAdapter);
        this.mViewPager.setCurrentItem(0);
    }

    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        DevModeCallback devModeCallback;
        if (keyEvent.getAction() == 0) {
            this.mHandler.removeCallbacks(this.mDataRunnable);
            if (keyEvent.getKeyCode() == 7) {
                this.mHandler.removeCallbacks(this.mDevModeRun);
                this.devMode += SessionDescription.SUPPORTED_SDP_VERSION;
                this.mHandler.postDelayed(this.mDevModeRun, 200);
                if (this.devMode.length() >= 4 && (devModeCallback = callback) != null) {
                    devModeCallback.onChange();
                }
            }
        } else if (keyEvent.getAction() == 1) {
            this.mHandler.postDelayed(this.mDataRunnable, 200);
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        String str = this.homeSourceKey;
        if ((str == null || str.equals(Hawk.get(HawkConfig.HOME_API, ""))) && this.currentApi.equals(Hawk.get(HawkConfig.API_URL, "")) && this.currentLive.equals(Hawk.get(HawkConfig.LIVE_URL, "")) && this.homeRec == ((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() && this.dnsOpt == ((Integer) Hawk.get(HawkConfig.DOH_URL, 0)).intValue()) {
            super.onBackPressed();
            return;
        }
        AppManager.getInstance().finishAllActivity();
        if (this.currentApi.equals(Hawk.get(HawkConfig.API_URL, "")) && this.currentLive.equals(Hawk.get(HawkConfig.LIVE_URL, ""))) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("useCache", true);
            jumpActivity(HomeActivity.class, bundle);
            return;
        }
        jumpActivity(HomeActivity.class);
    }
}
