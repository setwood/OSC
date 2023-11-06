package com.github.tvbox.osc.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.ui.activity.CollectActivity;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.DriveActivity;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.github.tvbox.osc.ui.activity.HistoryActivity;
import com.github.tvbox.osc.ui.activity.LivePlayActivity;
import com.github.tvbox.osc.ui.activity.PushActivity;
import com.github.tvbox.osc.ui.activity.SearchActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.adapter.HomeHotVodAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class UserFragment extends BaseLazyFragment implements View.OnClickListener {
    private final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass6 */

        public void onFocusChange(View view, boolean z) {
            if (z) {
                view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            } else {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }
        }
    };
    private HomeHotVodAdapter homeHotVodAdapter;
    private List<Movie.Video> homeSourceRec;
    private LinearLayout tvCollect;
    private LinearLayout tvDrive;
    private LinearLayout tvHistory;
    TvRecyclerView tvHotListForGrid;
    TvRecyclerView tvHotListForLine;
    private LinearLayout tvLive;
    private LinearLayout tvPush;
    private LinearLayout tvSearch;
    private LinearLayout tvSetting;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public int getLayoutResID() {
        return R.layout.fragment_user;
    }

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(List<Movie.Video> list) {
        return new UserFragment().setArguments(list);
    }

    public UserFragment setArguments(List<Movie.Video> list) {
        this.homeSourceRec = list;
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public void onFragmentResume() {
        if (((Boolean) Hawk.get(HawkConfig.HOME_REC_STYLE, true)).booleanValue()) {
            this.tvHotListForGrid.setVisibility(0);
            this.tvHotListForLine.setVisibility(8);
            this.tvHotListForGrid.setHasFixedSize(true);
            this.tvHotListForGrid.setLayoutManager(new V7GridLayoutManager(this.mContext, 5));
        } else {
            this.tvHotListForGrid.setVisibility(8);
            this.tvHotListForLine.setVisibility(0);
        }
        super.onFragmentResume();
        if (((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() == 2) {
            List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord(20);
            ArrayList arrayList = new ArrayList();
            for (VodInfo vodInfo : allVodRecord) {
                Movie.Video video = new Movie.Video();
                video.id = vodInfo.id;
                video.sourceKey = vodInfo.sourceKey;
                video.name = vodInfo.name;
                video.pic = vodInfo.pic;
                if (vodInfo.playNote != null && !vodInfo.playNote.isEmpty()) {
                    video.note = "上次看到" + vodInfo.playNote;
                }
                arrayList.add(video);
            }
            this.homeHotVodAdapter.setNewData(arrayList);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public void init() {
        EventBus.getDefault().register(this);
        this.tvDrive = (LinearLayout) findViewById(R.id.tvDrive);
        this.tvLive = (LinearLayout) findViewById(R.id.tvLive);
        this.tvSearch = (LinearLayout) findViewById(R.id.tvSearch);
        this.tvSetting = (LinearLayout) findViewById(R.id.tvSetting);
        this.tvCollect = (LinearLayout) findViewById(R.id.tvFavorite);
        this.tvHistory = (LinearLayout) findViewById(R.id.tvHistory);
        this.tvPush = (LinearLayout) findViewById(R.id.tvPush);
        this.tvDrive.setOnClickListener(this);
        this.tvLive.setOnClickListener(this);
        this.tvSearch.setOnClickListener(this);
        this.tvSetting.setOnClickListener(this);
        this.tvHistory.setOnClickListener(this);
        this.tvPush.setOnClickListener(this);
        this.tvCollect.setOnClickListener(this);
        this.tvDrive.setOnFocusChangeListener(this.focusChangeListener);
        this.tvLive.setOnFocusChangeListener(this.focusChangeListener);
        this.tvSearch.setOnFocusChangeListener(this.focusChangeListener);
        this.tvSetting.setOnFocusChangeListener(this.focusChangeListener);
        this.tvHistory.setOnFocusChangeListener(this.focusChangeListener);
        this.tvPush.setOnFocusChangeListener(this.focusChangeListener);
        this.tvCollect.setOnFocusChangeListener(this.focusChangeListener);
        this.tvHotListForGrid = (TvRecyclerView) findViewById(R.id.tvHotListForGrid);
        this.tvHotListForLine = (TvRecyclerView) findViewById(R.id.tvHotListForLine);
        HomeHotVodAdapter homeHotVodAdapter2 = new HomeHotVodAdapter();
        this.homeHotVodAdapter = homeHotVodAdapter2;
        homeHotVodAdapter2.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (!ApiConfig.get().getSourceBeanList().isEmpty()) {
                    Movie.Video video = (Movie.Video) baseQuickAdapter.getItem(i);
                    if (video.id == null || video.id.isEmpty()) {
                        Intent intent = new Intent(UserFragment.this.mContext, FastSearchActivity.class);
                        intent.putExtra("title", video.name);
                        intent.setFlags(335544320);
                        UserFragment.this.mActivity.startActivity(intent);
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    if (video.id.startsWith("msearch:")) {
                        bundle.putString("title", video.name);
                        UserFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                    } else if (((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() != 1 || !((Boolean) Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)).booleanValue()) {
                        UserFragment.this.jumpActivity(DetailActivity.class, bundle);
                    } else {
                        bundle.putString("title", video.name);
                        UserFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                    }
                }
            }
        });
        this.homeHotVodAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass2 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (ApiConfig.get().getSourceBeanList().isEmpty()) {
                    return false;
                }
                Movie.Video video = (Movie.Video) baseQuickAdapter.getItem(i);
                if (video.id == null || video.id.isEmpty() || ((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() != 2) {
                    Intent intent = new Intent(UserFragment.this.mContext, SearchActivity.class);
                    intent.putExtra("title", video.name);
                    intent.setFlags(335544320);
                    UserFragment.this.mActivity.startActivity(intent);
                    return true;
                }
                Intent intent2 = new Intent(UserFragment.this.mContext, FastSearchActivity.class);
                intent2.putExtra("title", video.name);
                intent2.setFlags(335544320);
                UserFragment.this.mActivity.startActivity(intent2);
                return true;
            }
        });
        this.tvHotListForGrid.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass3 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }
        });
        this.tvHotListForGrid.setAdapter(this.homeHotVodAdapter);
        this.tvHotListForLine.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass4 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }
        });
        this.tvHotListForLine.setAdapter(this.homeHotVodAdapter);
        initHomeHotVod(this.homeHotVodAdapter);
    }

    private void initHomeHotVod(final HomeHotVodAdapter homeHotVodAdapter2) {
        if (((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() == 1) {
            List<Movie.Video> list = this.homeSourceRec;
            if (list != null) {
                homeHotVodAdapter2.setNewData(list);
            }
        } else if (((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue() != 2) {
            try {
                Calendar instance = Calendar.getInstance();
                int i = instance.get(1);
                final String format = String.format("%d%d%d", Integer.valueOf(i), Integer.valueOf(instance.get(2) + 1), Integer.valueOf(instance.get(5)));
                if (((String) Hawk.get("home_hot_day", "")).equals(format)) {
                    String str = (String) Hawk.get("home_hot", "");
                    if (!str.isEmpty()) {
                        homeHotVodAdapter2.setNewData(loadHots(str));
                        return;
                    }
                }
                OkGo.get("https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&playable=1&start=0&year_range=" + i + "," + i).execute(new AbsCallback<String>() {
                    /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass5 */

                    @Override // com.lzy.okgo.callback.Callback
                    public void onSuccess(Response<String> response) {
                        final String body = response.body();
                        Hawk.put("home_hot_day", format);
                        Hawk.put("home_hot", body);
                        UserFragment.this.mActivity.runOnUiThread(new Runnable() {
                            /* class com.github.tvbox.osc.ui.fragment.UserFragment.AnonymousClass5.AnonymousClass1 */

                            public void run() {
                                homeHotVodAdapter2.setNewData(UserFragment.this.loadHots(body));
                            }
                        });
                    }

                    @Override // com.lzy.okgo.convert.Converter
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }
                });
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Movie.Video> loadHots(String str) {
        ArrayList<Movie.Video> arrayList = new ArrayList<>();
        try {
            Iterator<JsonElement> it = ((JsonObject) new Gson().fromJson(str, JsonObject.class)).getAsJsonArray("data").iterator();
            while (it.hasNext()) {
                JsonObject jsonObject = (JsonObject) it.next();
                Movie.Video video = new Movie.Video();
                video.name = jsonObject.get("title").getAsString();
                video.note = jsonObject.get("rate").getAsString();
                video.pic = jsonObject.get("cover").getAsString();
                arrayList.add(video);
            }
        } catch (Throwable unused) {
        }
        return arrayList;
    }

    public void onClick(View view) {
        FastClickCheckUtil.check(view);
        if (view.getId() == 2131296988) {
            jumpActivity(LivePlayActivity.class);
        } else if (view.getId() == 2131297006) {
            jumpActivity(SearchActivity.class);
        } else if (view.getId() == 2131297013) {
            jumpActivity(SettingActivity.class);
        } else if (view.getId() == 2131296978) {
            jumpActivity(HistoryActivity.class);
        } else if (view.getId() == 2131297000) {
            jumpActivity(PushActivity.class);
        } else if (view.getId() == 2131296975) {
            jumpActivity(CollectActivity.class);
        } else if (view.getId() == 2131296972) {
            jumpActivity(DriveActivity.class);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent serverEvent) {
        int i = serverEvent.type;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
