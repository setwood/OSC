package com.github.tvbox.osc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.adapter.PinyinAdapter;
import com.github.tvbox.osc.ui.adapter.SearchAdapter;
import com.github.tvbox.osc.ui.dialog.RemoteDialog;
import com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import com.github.tvbox.osc.ui.tv.widget.SearchKeyboard;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.SearchHelper;
import com.github.tvbox.osc.util.js.JSEngine;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;

public class SearchActivity extends BaseActivity {
    private static HashMap<String, String> mCheckSources;
    private final AtomicInteger allRunCount = new AtomicInteger(0);
    private EditText etSearch;
    private ImageView ivQRCode;
    private SearchKeyboard keyboard;
    private LinearLayout llLayout;
    private TvRecyclerView mGridView;
    private TvRecyclerView mGridViewWord;
    private SearchCheckboxDialog mSearchCheckboxDialog = null;
    private List<Runnable> pauseRunnable = null;
    private SearchAdapter searchAdapter;
    private ExecutorService searchExecutorService = null;
    private String searchTitle = "";
    SourceViewModel sourceViewModel;
    private TextView tvAddress;
    private TextView tvClear;
    private TextView tvSearch;
    private ImageView tvSearchCheckbox;
    private PinyinAdapter wordAdapter;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_search;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
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
        this.etSearch = (EditText) findViewById(R.id.etSearch);
        this.tvSearch = (TextView) findViewById(R.id.tvSearch);
        this.tvSearchCheckbox = (ImageView) findViewById(R.id.tvSearchCheckbox);
        this.tvClear = (TextView) findViewById(R.id.tvClear);
        this.tvAddress = (TextView) findViewById(R.id.tvAddress);
        this.ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.keyboard = (SearchKeyboard) findViewById(R.id.keyBoardRoot);
        TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.mGridViewWord);
        this.mGridViewWord = tvRecyclerView;
        tvRecyclerView.setHasFixedSize(true);
        this.mGridViewWord.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        PinyinAdapter pinyinAdapter = new PinyinAdapter();
        this.wordAdapter = pinyinAdapter;
        this.mGridViewWord.setAdapter(pinyinAdapter);
        this.wordAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                SearchActivity searchActivity = SearchActivity.this;
                searchActivity.search((String) searchActivity.wordAdapter.getItem(i));
            }
        });
        this.mGridView.setHasFixedSize(true);
        if (((Integer) Hawk.get(HawkConfig.SEARCH_VIEW, 0)).intValue() == 0) {
            this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        } else {
            this.mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, 3));
        }
        SearchAdapter searchAdapter2 = new SearchAdapter();
        this.searchAdapter = searchAdapter2;
        this.mGridView.setAdapter(searchAdapter2);
        this.searchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass2 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Movie.Video video = (Movie.Video) SearchActivity.this.searchAdapter.getData().get(i);
                if (video != null) {
                    try {
                        if (SearchActivity.this.searchExecutorService != null) {
                            SearchActivity searchActivity = SearchActivity.this;
                            searchActivity.pauseRunnable = searchActivity.searchExecutorService.shutdownNow();
                            SearchActivity.this.searchExecutorService = null;
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    SearchActivity.this.jumpActivity(DetailActivity.class, bundle);
                }
            }
        });
        this.tvSearch.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                String trim = SearchActivity.this.etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(trim)) {
                    SearchActivity.this.search(trim);
                } else {
                    Toast.makeText(SearchActivity.this.mContext, SearchActivity.this.getString(R.string.search_input), 0).show();
                }
            }
        });
        this.tvClear.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SearchActivity.this.etSearch.setText("");
            }
        });
        this.keyboard.setOnSearchKeyListener(new SearchKeyboard.OnSearchKeyListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass5 */

            @Override // com.github.tvbox.osc.ui.tv.widget.SearchKeyboard.OnSearchKeyListener
            public void onSearchKey(int i, String str) {
                if (i > 1) {
                    String str2 = SearchActivity.this.etSearch.getText().toString().trim() + str;
                    SearchActivity.this.etSearch.setText(str2);
                    if (str2.length() > 0) {
                        SearchActivity.this.loadRec(str2);
                    }
                } else if (i == 1) {
                    String trim = SearchActivity.this.etSearch.getText().toString().trim();
                    if (trim.length() > 0) {
                        trim = trim.substring(0, trim.length() - 1);
                        SearchActivity.this.etSearch.setText(trim);
                    }
                    if (trim.length() > 0) {
                        SearchActivity.this.loadRec(trim);
                    }
                } else if (i == 0) {
                    new RemoteDialog(SearchActivity.this.mContext).show();
                }
            }
        });
        this.tvSearchCheckbox.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass6 */

            public void onClick(View view) {
                if (SearchActivity.this.mSearchCheckboxDialog == null) {
                    List<SourceBean> sourceBeanList = ApiConfig.get().getSourceBeanList();
                    ArrayList arrayList = new ArrayList();
                    for (SourceBean sourceBean : sourceBeanList) {
                        if (sourceBean.isSearchable()) {
                            arrayList.add(sourceBean);
                        }
                    }
                    SearchActivity.this.mSearchCheckboxDialog = new SearchCheckboxDialog(SearchActivity.this, arrayList, SearchActivity.mCheckSources);
                }
                SearchActivity.this.mSearchCheckboxDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass6.AnonymousClass1 */

                    public void onDismiss(DialogInterface dialogInterface) {
                        dialogInterface.dismiss();
                    }
                });
                SearchActivity.this.mSearchCheckboxDialog.show();
            }
        });
        setLoadSir(this.llLayout);
    }

    private void initViewModel() {
        this.sourceViewModel = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadRec(String str) {
        ((GetRequest) ((GetRequest) ((GetRequest) ((GetRequest) ((GetRequest) OkGo.get("https://s.video.qq.com/smartbox").params("plat", 2, new boolean[0])).params("ver", 0, new boolean[0])).params("num", 10, new boolean[0])).params("otype", "json", new boolean[0])).params("query", str, new boolean[0])).execute(new AbsCallback<String>() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass7 */

            @Override // com.lzy.okgo.callback.Callback
            public void onSuccess(Response<String> response) {
                try {
                    ArrayList arrayList = new ArrayList();
                    String body = response.body();
                    Iterator<JsonElement> it = JsonParser.parseString(body.substring(body.indexOf("{"), body.lastIndexOf(StringSubstitutor.DEFAULT_VAR_END) + 1)).getAsJsonObject().get("item").getAsJsonArray().iterator();
                    while (it.hasNext()) {
                        arrayList.add(((JsonObject) it.next()).get("word").getAsString().trim());
                    }
                    SearchActivity.this.wordAdapter.setNewData(arrayList);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }

            @Override // com.lzy.okgo.convert.Converter
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return response.body().string();
            }
        });
    }

    private void initData() {
        refreshQRCode();
        initCheckedSourcesForSearch();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("title")) {
            String stringExtra = intent.getStringExtra("title");
            showLoading();
            search(stringExtra);
        }
        loadHotSearch();
    }

    private void loadHotSearch() {
        ((GetRequest) ((GetRequest) OkGo.get("https://node.video.qq.com/x/api/hot_search").params("channdlId", SessionDescription.SUPPORTED_SDP_VERSION, new boolean[0])).params("_", System.currentTimeMillis(), new boolean[0])).execute(new AbsCallback<String>() {
            /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass8 */

            @Override // com.lzy.okgo.callback.Callback
            public void onSuccess(Response<String> response) {
                try {
                    ArrayList arrayList = new ArrayList();
                    JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject().get("mapResult").getAsJsonObject();
                    for (String str : Arrays.asList(SessionDescription.SUPPORTED_SDP_VERSION, "1", ExifInterface.GPS_MEASUREMENT_2D, ExifInterface.GPS_MEASUREMENT_3D, "5")) {
                        Iterator<JsonElement> it = asJsonObject.get(str).getAsJsonObject().get("listInfo").getAsJsonArray().iterator();
                        while (it.hasNext()) {
                            String str2 = ((JsonObject) it.next()).get("title").getAsString().trim().replaceAll("<|>|《|》|-", "").split(StringUtils.SPACE)[0];
                            if (!arrayList.contains(str2)) {
                                arrayList.add(str2);
                            }
                        }
                    }
                    SearchActivity.this.wordAdapter.setNewData(arrayList);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }

            @Override // com.lzy.okgo.convert.Converter
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return response.body().string();
            }
        });
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        this.tvAddress.setText(String.format("远程搜索使用手机/电脑扫描下面二维码或者直接浏览器访问地址\n%s", address));
        this.ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, IjkMediaCodecInfo.RANK_SECURE, IjkMediaCodecInfo.RANK_SECURE));
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
        if (refreshEvent.type == 6) {
            try {
                searchData(refreshEvent.obj == null ? null : (AbsXml) refreshEvent.obj);
            } catch (Exception unused) {
                searchData(null);
            }
        }
    }

    private void initCheckedSourcesForSearch() {
        mCheckSources = SearchHelper.getSourcesForSearch();
    }

    public static void setCheckedSourcesForSearch(HashMap<String, String> hashMap) {
        mCheckSources = hashMap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void search(String str) {
        cancel();
        showLoading();
        this.searchTitle = str;
        this.mGridView.setVisibility(4);
        this.searchAdapter.setNewData(new ArrayList());
        searchResult();
    }

    private void searchResult() {
        SearchAdapter searchAdapter2;
        ArrayList arrayList;
        HashMap<String, String> hashMap;
        try {
            ExecutorService executorService = this.searchExecutorService;
            if (executorService != null) {
                executorService.shutdownNow();
                this.searchExecutorService = null;
                JSEngine.getInstance().stopAll();
            }
            searchAdapter2 = this.searchAdapter;
            arrayList = new ArrayList();
        } catch (Throwable th) {
            this.searchAdapter.setNewData(new ArrayList());
            this.allRunCount.set(0);
            throw th;
        }
        searchAdapter2.setNewData(arrayList);
        this.allRunCount.set(0);
        this.searchExecutorService = Executors.newFixedThreadPool(5);
        ArrayList<SourceBean> arrayList2 = new ArrayList();
        arrayList2.addAll(ApiConfig.get().getSourceBeanList());
        SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
        arrayList2.remove(homeSourceBean);
        arrayList2.add(0, homeSourceBean);
        ArrayList arrayList3 = new ArrayList();
        for (SourceBean sourceBean : arrayList2) {
            if (sourceBean.isSearchable() && ((hashMap = mCheckSources) == null || hashMap.containsKey(sourceBean.getKey()))) {
                arrayList3.add(sourceBean.getKey());
                this.allRunCount.incrementAndGet();
            }
        }
        if (arrayList3.size() <= 0) {
            Toast.makeText(this.mContext, getString(R.string.search_site), 0).show();
            showEmpty();
            return;
        }
        Iterator it = arrayList3.iterator();
        while (it.hasNext()) {
            final String str = (String) it.next();
            this.searchExecutorService.execute(new Runnable() {
                /* class com.github.tvbox.osc.ui.activity.SearchActivity.AnonymousClass9 */

                public void run() {
                    SearchActivity.this.sourceViewModel.getSearch(str, SearchActivity.this.searchTitle);
                }
            });
        }
    }

    private boolean matchSearchResult(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        String[] split = str2.trim().split("\\s+");
        int i = 0;
        for (String str3 : split) {
            if (str.contains(str3)) {
                i++;
            }
        }
        if (i == split.length) {
            return true;
        }
        return false;
    }

    private void searchData(AbsXml absXml) {
        if (!(absXml == null || absXml.movie == null || absXml.movie.videoList == null || absXml.movie.videoList.size() <= 0)) {
            ArrayList arrayList = new ArrayList();
            for (Movie.Video video : absXml.movie.videoList) {
                if (matchSearchResult(video.name, this.searchTitle)) {
                    arrayList.add(video);
                }
            }
            if (this.searchAdapter.getData().size() > 0) {
                this.searchAdapter.addData((Collection) arrayList);
            } else {
                showSuccess();
                this.mGridView.setVisibility(0);
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
