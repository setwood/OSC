package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Subtitle;
import com.github.tvbox.osc.bean.SubtitleData;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.adapter.SearchSubtitleAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.viewmodel.SubtitleViewModel;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class SearchSubtitleDialog extends BaseDialog {
    private boolean isSearchPag = true;
    private ProgressBar loadingBar;
    private Context mContext;
    private TvRecyclerView mGridView;
    private SubtitleLoader mSubtitleLoader;
    private int maxPage = 5;
    private int page = 1;
    private SearchSubtitleAdapter searchAdapter;
    private String searchWord = "";
    private TextView subtitleSearchBtn;
    private EditText subtitleSearchEt;
    private SubtitleViewModel subtitleViewModel;
    private List<Subtitle> zipSubtitles = new ArrayList();

    public interface SubtitleLoader {
        void loadSubtitle(Subtitle subtitle);
    }

    static /* synthetic */ int access$808(SearchSubtitleDialog searchSubtitleDialog) {
        int i = searchSubtitleDialog.page;
        searchSubtitleDialog.page = i + 1;
        return i;
    }

    public SearchSubtitleDialog(Context context) {
        super(context);
        this.mContext = context;
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
        setContentView(R.layout.dialog_search_subtitle);
        initView(context);
        initViewModel();
    }

    /* access modifiers changed from: protected */
    public void initView(Context context) {
        this.loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.subtitleSearchEt = (EditText) findViewById(R.id.input);
        TextView textView = (TextView) findViewById(R.id.inputSubmit);
        this.subtitleSearchBtn = textView;
        textView.setText(HomeActivity.getRes().getString(R.string.vod_sub_search));
        this.searchAdapter = new SearchSubtitleAdapter();
        this.mGridView.setHasFixedSize(true);
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(getContext(), 1, false));
        this.mGridView.setAdapter(this.searchAdapter);
        this.searchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Subtitle subtitle = (Subtitle) SearchSubtitleDialog.this.searchAdapter.getData().get(i);
                if (SearchSubtitleDialog.this.mSubtitleLoader == null) {
                    return;
                }
                if (subtitle.getIsZip()) {
                    SearchSubtitleDialog.this.isSearchPag = false;
                    SearchSubtitleDialog.this.loadingBar.setVisibility(0);
                    SearchSubtitleDialog.this.mGridView.setVisibility(8);
                    SearchSubtitleDialog.this.subtitleViewModel.getSearchResultSubtitleUrls(subtitle);
                    return;
                }
                SearchSubtitleDialog.this.loadSubtitle(subtitle);
                SearchSubtitleDialog.this.dismiss();
            }
        });
        this.searchAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            /* class com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.AnonymousClass2 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener
            public void onLoadMoreRequested() {
                if (((Subtitle) SearchSubtitleDialog.this.searchAdapter.getData().get(0)).getIsZip()) {
                    SearchSubtitleDialog.this.subtitleViewModel.searchResult(SearchSubtitleDialog.this.searchWord, SearchSubtitleDialog.this.page);
                }
            }
        }, this.mGridView);
        this.subtitleSearchBtn.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SearchSubtitleDialog.this.search(SearchSubtitleDialog.this.subtitleSearchEt.getText().toString().trim());
            }
        });
        this.searchAdapter.setNewData(new ArrayList());
    }

    public void setSearchWord(String str) {
        String replaceAll = str.replaceAll("(?:（|\\(|\\[|【|\\.mp4|\\.mkv|\\.avi|\\.MP4|\\.MKV|\\.AVI)", "").replaceAll("(?:：|\\:|）|\\)|\\]|】|\\.)", StringUtils.SPACE);
        int length = replaceAll.length();
        if (length >= 36) {
            length = 36;
        }
        String trim = replaceAll.substring(0, length).trim();
        this.subtitleSearchEt.setText(trim);
        this.subtitleSearchEt.setSelection(trim.length());
        this.subtitleSearchEt.requestFocus();
    }

    public void search(String str) {
        this.isSearchPag = true;
        this.searchAdapter.setNewData(new ArrayList());
        if (!TextUtils.isEmpty(str)) {
            this.loadingBar.setVisibility(0);
            this.mGridView.setVisibility(8);
            this.searchWord = str;
            SubtitleViewModel subtitleViewModel2 = this.subtitleViewModel;
            this.page = 1;
            subtitleViewModel2.searchResult(str, 1);
            return;
        }
        Toast.makeText(getContext(), "输入内容不能为空", 0).show();
    }

    private void initViewModel() {
        SubtitleViewModel subtitleViewModel2 = (SubtitleViewModel) new ViewModelProvider((ViewModelStoreOwner) this.mContext).get(SubtitleViewModel.class);
        this.subtitleViewModel = subtitleViewModel2;
        subtitleViewModel2.searchResult.observe((LifecycleOwner) this.mContext, new Observer<SubtitleData>() {
            /* class com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.AnonymousClass4 */

            public void onChanged(SubtitleData subtitleData) {
                List<Subtitle> subtitleList = subtitleData.getSubtitleList();
                SearchSubtitleDialog.this.loadingBar.setVisibility(8);
                SearchSubtitleDialog.this.mGridView.setVisibility(0);
                if (subtitleList == null) {
                    SearchSubtitleDialog.this.mGridView.post(new Runnable() {
                        /* class com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog.AnonymousClass4.AnonymousClass1 */

                        public void run() {
                            Toast.makeText(SearchSubtitleDialog.this.getContext(), "未查询到匹配字幕", 0).show();
                        }
                    });
                } else if (subtitleList.size() > 0) {
                    SearchSubtitleDialog.this.mGridView.requestFocus();
                    if (subtitleData.getIsZip().booleanValue()) {
                        if (subtitleData.getIsNew().booleanValue()) {
                            SearchSubtitleDialog.this.searchAdapter.setNewData(subtitleList);
                            SearchSubtitleDialog.this.zipSubtitles = subtitleList;
                        } else {
                            SearchSubtitleDialog.this.searchAdapter.addData((Collection) subtitleList);
                            SearchSubtitleDialog.this.zipSubtitles.addAll(subtitleList);
                        }
                        SearchSubtitleDialog.access$808(SearchSubtitleDialog.this);
                        if (SearchSubtitleDialog.this.page > SearchSubtitleDialog.this.maxPage) {
                            SearchSubtitleDialog.this.searchAdapter.loadMoreEnd();
                            SearchSubtitleDialog.this.searchAdapter.setEnableLoadMore(false);
                            return;
                        }
                        SearchSubtitleDialog.this.searchAdapter.loadMoreComplete();
                        SearchSubtitleDialog.this.searchAdapter.setEnableLoadMore(true);
                        return;
                    }
                    SearchSubtitleDialog.this.searchAdapter.loadMoreComplete();
                    SearchSubtitleDialog.this.searchAdapter.setNewData(subtitleList);
                    SearchSubtitleDialog.this.searchAdapter.setEnableLoadMore(false);
                } else {
                    if (SearchSubtitleDialog.this.page > SearchSubtitleDialog.this.maxPage) {
                        SearchSubtitleDialog.this.searchAdapter.loadMoreEnd();
                    } else {
                        SearchSubtitleDialog.this.searchAdapter.loadMoreComplete();
                    }
                    SearchSubtitleDialog.this.searchAdapter.setEnableLoadMore(false);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadSubtitle(Subtitle subtitle) {
        this.subtitleViewModel.getSubtitleUrl(subtitle, this.mSubtitleLoader);
    }

    public void setSubtitleLoader(SubtitleLoader subtitleLoader) {
        this.mSubtitleLoader = subtitleLoader;
    }

    public void onBackPressed() {
        if (!this.isSearchPag) {
            boolean z = true;
            this.isSearchPag = true;
            this.loadingBar.setVisibility(8);
            this.mGridView.setVisibility(0);
            this.searchAdapter.setNewData(this.zipSubtitles);
            SearchSubtitleAdapter searchSubtitleAdapter = this.searchAdapter;
            if (this.page >= this.maxPage) {
                z = false;
            }
            searchSubtitleAdapter.setEnableLoadMore(z);
            return;
        }
        dismiss();
    }
}
