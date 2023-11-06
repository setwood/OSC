package com.github.tvbox.osc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.ui.adapter.FastListAdapter;
import com.github.tvbox.osc.ui.adapter.FastSearchAdapter;
import com.github.tvbox.osc.ui.adapter.SearchWordAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.SearchHelper;
import com.github.tvbox.osc.util.js.JSEngine;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.GetRequest;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.umeng.analytics.pro.an;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FastSearchActivity extends BaseActivity {
    private final AtomicInteger allRunCount = new AtomicInteger(0);
    private int finishedCount = 0;
    private final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass1 */

        public void onFocusChange(View view, boolean z) {
            if (!z) {
                try {
                    FastSearchActivity.this.spListAdapter.onLostFocus(view);
                } catch (Exception e) {
                    Toast.makeText(FastSearchActivity.this, e.toString(), 0).show();
                }
            } else if (FastSearchActivity.this.spListAdapter.onSetFocus(view) >= 0) {
                FastSearchActivity.this.filterResult(((TextView) view).getText().toString());
            }
        }
    };
    private boolean isFilterMode = false;
    private LinearLayout llLayout;
    private HashMap<String, String> mCheckSources = null;
    private TvRecyclerView mGridView;
    private TvRecyclerView mGridViewFilter;
    private TvRecyclerView mGridViewWord;
    private TvRecyclerView mGridViewWordFenci;
    private TextView mSearchTitle;
    private List<Runnable> pauseRunnable = null;
    private final List<String> quickSearchWord = new ArrayList();
    private HashMap<String, ArrayList<Movie.Video>> resultVods;
    private FastSearchAdapter searchAdapter;
    private FastSearchAdapter searchAdapterFilter;
    private ExecutorService searchExecutorService = null;
    private String searchFilterKey = "";
    private String searchTitle = "";
    private SearchWordAdapter searchWordAdapter;
    SourceViewModel sourceViewModel;
    private FastListAdapter spListAdapter;
    private HashMap<String, String> spNames;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_fast_search;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        this.spNames = new HashMap<>();
        this.resultVods = new HashMap<>();
        initView();
        initViewModel();
        initData();
    }

    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onResume() {
        super.onResume();
        List<Runnable> list = this.pauseRunnable;
        if (list != null && list.size() > 0) {
            this.searchExecutorService = Executors.newFixedThreadPool(5);
            this.allRunCount.set(this.pauseRunnable.size());
            for (Runnable runnable : this.pauseRunnable) {
                this.searchExecutorService.execute(runnable);
            }
            this.pauseRunnable.clear();
            this.pauseRunnable = null;
        }
    }

    private void initView() {
        EventBus.getDefault().register(this);
        this.llLayout = (LinearLayout) findViewById(R.id.llLayout);
        this.mSearchTitle = (TextView) findViewById(R.id.mSearchTitle);
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.mGridViewWord = (TvRecyclerView) findViewById(R.id.mGridViewWord);
        this.mGridViewFilter = (TvRecyclerView) findViewById(R.id.mGridViewFilter);
        this.mGridViewWord.setHasFixedSize(true);
        this.mGridViewWord.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        FastListAdapter fastListAdapter = new FastListAdapter();
        this.spListAdapter = fastListAdapter;
        this.mGridViewWord.setAdapter(fastListAdapter);
        this.mGridViewWord.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass2 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
            public void onChildViewAttachedToWindow(View view) {
                view.setFocusable(true);
                view.setOnFocusChangeListener(FastSearchActivity.this.focusChangeListener);
                TextView textView = (TextView) view;
                if (textView.getText() == FastSearchActivity.this.getString(R.string.fs_show_all)) {
                    textView.requestFocus();
                }
            }

            @Override // androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
            public void onChildViewDetachedFromWindow(View view) {
                view.setOnFocusChangeListener(null);
            }
        });
        this.spListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass3 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastSearchActivity.this.filterResult((String) FastSearchActivity.this.spListAdapter.getItem(i));
            }
        });
        int i = 4;
        this.mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, isBaseOnWidth() ? 4 : 5));
        FastSearchAdapter fastSearchAdapter = new FastSearchAdapter();
        this.searchAdapter = fastSearchAdapter;
        this.mGridView.setAdapter(fastSearchAdapter);
        this.searchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass4 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Movie.Video video = (Movie.Video) FastSearchActivity.this.searchAdapter.getData().get(i);
                if (video != null) {
                    try {
                        if (FastSearchActivity.this.searchExecutorService != null) {
                            FastSearchActivity fastSearchActivity = FastSearchActivity.this;
                            fastSearchActivity.pauseRunnable = fastSearchActivity.searchExecutorService.shutdownNow();
                            FastSearchActivity.this.searchExecutorService = null;
                            JSEngine.getInstance().stopAll();
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    FastSearchActivity.this.jumpActivity(DetailActivity.class, bundle);
                }
            }
        });
        TvRecyclerView tvRecyclerView = this.mGridViewFilter;
        Context context = this.mContext;
        if (!isBaseOnWidth()) {
            i = 5;
        }
        tvRecyclerView.setLayoutManager(new V7GridLayoutManager(context, i));
        FastSearchAdapter fastSearchAdapter2 = new FastSearchAdapter();
        this.searchAdapterFilter = fastSearchAdapter2;
        this.mGridViewFilter.setAdapter(fastSearchAdapter2);
        this.searchAdapterFilter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass5 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Movie.Video video = (Movie.Video) FastSearchActivity.this.searchAdapterFilter.getData().get(i);
                if (video != null) {
                    try {
                        if (FastSearchActivity.this.searchExecutorService != null) {
                            FastSearchActivity fastSearchActivity = FastSearchActivity.this;
                            fastSearchActivity.pauseRunnable = fastSearchActivity.searchExecutorService.shutdownNow();
                            FastSearchActivity.this.searchExecutorService = null;
                            JSEngine.getInstance().stopAll();
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    FastSearchActivity.this.jumpActivity(DetailActivity.class, bundle);
                }
            }
        });
        setLoadSir(this.llLayout);
        this.searchWordAdapter = new SearchWordAdapter();
        TvRecyclerView tvRecyclerView2 = (TvRecyclerView) findViewById(R.id.mGridViewWordFenci);
        this.mGridViewWordFenci = tvRecyclerView2;
        tvRecyclerView2.setAdapter(this.searchWordAdapter);
        this.mGridViewWordFenci.setLayoutManager(new V7LinearLayoutManager(this.mContext, 0, false));
        this.searchWordAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass6 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastSearchActivity.this.search((String) FastSearchActivity.this.searchWordAdapter.getData().get(i));
            }
        });
        this.searchWordAdapter.setNewData(new ArrayList());
    }

    private void initViewModel() {
        this.sourceViewModel = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void filterResult(String str) {
        if (str == getString(R.string.fs_show_all)) {
            this.mGridView.setVisibility(0);
            this.mGridViewFilter.setVisibility(8);
            return;
        }
        String str2 = this.spNames.get(str);
        if (!str2.isEmpty() && this.searchFilterKey != str2) {
            this.searchFilterKey = str2;
            this.searchAdapterFilter.setNewData(this.resultVods.get(str2));
            this.mGridView.setVisibility(8);
            this.mGridViewFilter.setVisibility(0);
        }
    }

    private void fenci() {
        if (this.quickSearchWord.isEmpty()) {
            ((GetRequest) OkGo.get("http://api.pullword.com/get.php?source=" + URLEncoder.encode(this.searchTitle) + "&param1=0&param2=0&json=1").tag("fenci")).execute(new AbsCallback<String>() {
                /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass7 */

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
                    FastSearchActivity.this.quickSearchWord.clear();
                    try {
                        Iterator<JsonElement> it = ((JsonArray) new Gson().fromJson(body, JsonArray.class)).iterator();
                        while (it.hasNext()) {
                            FastSearchActivity.this.quickSearchWord.add(it.next().getAsJsonObject().get(an.aI).getAsString());
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    FastSearchActivity.this.quickSearchWord.add(FastSearchActivity.this.searchTitle);
                    EventBus.getDefault().post(new RefreshEvent(4, FastSearchActivity.this.quickSearchWord));
                }

                @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                public void onError(com.lzy.okgo.model.Response<String> response) {
                    super.onError(response);
                }
            });
        }
    }

    private void initCheckedSourcesForSearch() {
        this.mCheckSources = SearchHelper.getSourcesForSearch();
    }

    private void initData() {
        initCheckedSourcesForSearch();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("title")) {
            String stringExtra = intent.getStringExtra("title");
            showLoading();
            search(stringExtra);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent serverEvent) {
        if (serverEvent.type == 2) {
            showLoading();
            search((String) serverEvent.obj);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (this.mSearchTitle != null) {
            this.finishedCount = this.searchAdapter.getData().size();
            TextView textView = this.mSearchTitle;
            textView.setText(String.format(getString(R.string.fs_results) + " : %d", Integer.valueOf(this.finishedCount)));
        }
        if (refreshEvent.type == 6) {
            try {
                searchData(refreshEvent.obj == null ? null : (AbsXml) refreshEvent.obj);
            } catch (Exception unused) {
                searchData(null);
            }
        } else if (refreshEvent.type == 4 && refreshEvent.obj != null) {
            this.searchWordAdapter.setNewData((List) refreshEvent.obj);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void search(String str) {
        cancel();
        showLoading();
        this.searchTitle = str;
        fenci();
        this.mGridView.setVisibility(4);
        this.mGridViewFilter.setVisibility(8);
        this.searchAdapter.setNewData(new ArrayList());
        this.searchAdapterFilter.setNewData(new ArrayList());
        this.spListAdapter.reset();
        this.resultVods.clear();
        this.searchFilterKey = "";
        this.isFilterMode = false;
        this.spNames.clear();
        this.finishedCount = 0;
        searchResult();
    }

    private void searchResult() {
        FastSearchAdapter fastSearchAdapter;
        ArrayList arrayList;
        HashMap<String, String> hashMap;
        try {
            ExecutorService executorService = this.searchExecutorService;
            if (executorService != null) {
                executorService.shutdownNow();
                this.searchExecutorService = null;
                JSEngine.getInstance().stopAll();
            }
            this.searchAdapter.setNewData(new ArrayList());
            fastSearchAdapter = this.searchAdapterFilter;
            arrayList = new ArrayList();
        } catch (Throwable th) {
            this.searchAdapter.setNewData(new ArrayList());
            this.searchAdapterFilter.setNewData(new ArrayList());
            this.allRunCount.set(0);
            throw th;
        }
        fastSearchAdapter.setNewData(arrayList);
        this.allRunCount.set(0);
        this.searchExecutorService = Executors.newFixedThreadPool(5);
        ArrayList<SourceBean> arrayList2 = new ArrayList();
        arrayList2.addAll(ApiConfig.get().getSourceBeanList());
        SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
        arrayList2.remove(homeSourceBean);
        arrayList2.add(0, homeSourceBean);
        ArrayList arrayList3 = new ArrayList();
        this.spListAdapter.setNewData(new ArrayList());
        this.spListAdapter.addData(getString(R.string.fs_show_all));
        for (SourceBean sourceBean : arrayList2) {
            if (sourceBean.isSearchable() && ((hashMap = this.mCheckSources) == null || hashMap.containsKey(sourceBean.getKey()))) {
                arrayList3.add(sourceBean.getKey());
                this.spNames.put(sourceBean.getName(), sourceBean.getKey());
                this.allRunCount.incrementAndGet();
            }
        }
        Iterator it = arrayList3.iterator();
        while (it.hasNext()) {
            final String str = (String) it.next();
            this.searchExecutorService.execute(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.FastSearchActivity.AnonymousClass8 */

                public void run() {
                    try {
                        FastSearchActivity.this.sourceViewModel.getSearch(str, FastSearchActivity.this.searchTitle);
                    } catch (Exception unused) {
                    }
                }
            });
        }
    }

    private String addWordAdapterIfNeed(String str) {
        try {
            String str2 = "";
            for (String str3 : this.spNames.keySet()) {
                if (this.spNames.get(str3) == str) {
                    str2 = str3;
                }
            }
            if (str2 == "") {
                return str;
            }
            List data = this.spListAdapter.getData();
            for (int i = 0; i < data.size(); i++) {
                if (str2 == data.get(i)) {
                    return str;
                }
            }
            this.spListAdapter.addData(str2);
            return str;
        } catch (Exception unused) {
        }
    }

    private void searchData(AbsXml absXml) {
        if (!(absXml == null || absXml.movie == null || absXml.movie.videoList == null || absXml.movie.videoList.size() <= 0)) {
            ArrayList arrayList = new ArrayList();
            String str = "";
            for (Movie.Video video : absXml.movie.videoList) {
                if (video.name.contains(this.searchTitle)) {
                    arrayList.add(video);
                    if (!this.resultVods.containsKey(video.sourceKey)) {
                        this.resultVods.put(video.sourceKey, new ArrayList<>());
                    }
                    this.resultVods.get(video.sourceKey).add(video);
                    if (video.sourceKey != str) {
                        str = addWordAdapterIfNeed(video.sourceKey);
                    }
                }
            }
            if (this.searchAdapter.getData().size() > 0) {
                this.searchAdapter.addData((Collection) arrayList);
            } else {
                showSuccess();
                if (!this.isFilterMode) {
                    this.mGridView.setVisibility(0);
                }
                this.searchAdapter.setNewData(arrayList);
            }
        }
        if (this.allRunCount.decrementAndGet() <= 0) {
            if (this.searchAdapter.getData().size() <= 0) {
                showEmpty();
            }
            cancel();
        }
    }

    private void cancel() {
        OkGo.getInstance().cancelTag("search");
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        cancel();
        try {
            ExecutorService executorService = this.searchExecutorService;
            if (executorService != null) {
                executorService.shutdownNow();
                this.searchExecutorService = null;
                JSEngine.getInstance().stopAll();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }
}
