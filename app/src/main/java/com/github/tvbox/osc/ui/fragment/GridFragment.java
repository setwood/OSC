package com.github.tvbox.osc.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.github.tvbox.osc.ui.adapter.GridAdapter;
import com.github.tvbox.osc.ui.dialog.GridFilterDialog;
import com.github.tvbox.osc.ui.tv.widget.LoadMoreView;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.lzy.okgo.model.Progress;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.Collection;
import java.util.Stack;
import org.greenrobot.eventbus.EventBus;

public class GridFragment extends BaseLazyFragment {
    private View focusedView = null;
    private GridAdapter gridAdapter;
    private GridFilterDialog gridFilterDialog;
    private boolean isLoad = false;
    private boolean isTop = true;
    private TvRecyclerView mGridView;
    Stack<GridInfo> mGrids = new Stack<>();
    private int maxPage = 1;
    private int page = 1;
    private MovieSort.SortData sortData = null;
    private SourceViewModel sourceViewModel;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public int getLayoutResID() {
        return R.layout.fragment_grid;
    }

    static /* synthetic */ int access$308(GridFragment gridFragment) {
        int i = gridFragment.page;
        gridFragment.page = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    public class GridInfo {
        public View focusedView;
        public GridAdapter gridAdapter;
        public boolean isLoad;
        public TvRecyclerView mGridView;
        public int maxPage;
        public int page;
        public String sortID;

        private GridInfo() {
            this.sortID = "";
            this.page = 1;
            this.maxPage = 1;
            this.isLoad = false;
            this.focusedView = null;
        }
    }

    public static GridFragment newInstance(MovieSort.SortData sortData2) {
        return new GridFragment().setArguments(sortData2);
    }

    public GridFragment setArguments(MovieSort.SortData sortData2) {
        this.sortData = sortData2;
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public void init() {
        initView();
        initViewModel();
        initData();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeView(String str) {
        initView();
        this.sortData.id = str;
        initViewModel();
        initData();
    }

    public boolean isFolederMode() {
        return getUITag() == '1';
    }

    public char getUITag() {
        MovieSort.SortData sortData2 = this.sortData;
        if (sortData2 == null || sortData2.flag == null || this.sortData.flag.length() == 0) {
            return '0';
        }
        return this.sortData.flag.charAt(0);
    }

    public boolean enableFastSearch() {
        MovieSort.SortData sortData2 = this.sortData;
        return sortData2 == null || sortData2.flag == null || this.sortData.flag.length() < 2 || this.sortData.flag.charAt(1) == '1';
    }

    private void saveCurrentView() {
        if (this.mGridView != null) {
            GridInfo gridInfo = new GridInfo();
            gridInfo.sortID = this.sortData.id;
            gridInfo.mGridView = this.mGridView;
            gridInfo.gridAdapter = this.gridAdapter;
            gridInfo.page = this.page;
            gridInfo.maxPage = this.maxPage;
            gridInfo.isLoad = this.isLoad;
            gridInfo.focusedView = this.focusedView;
            this.mGrids.push(gridInfo);
        }
    }

    public boolean restoreView() {
        if (this.mGrids.empty()) {
            return false;
        }
        showSuccess();
        ((ViewGroup) this.mGridView.getParent()).removeView(this.mGridView);
        GridInfo pop = this.mGrids.pop();
        this.sortData.id = pop.sortID;
        this.mGridView = pop.mGridView;
        this.gridAdapter = pop.gridAdapter;
        this.page = pop.page;
        this.maxPage = pop.maxPage;
        this.isLoad = pop.isLoad;
        this.focusedView = pop.focusedView;
        this.mGridView.setVisibility(0);
        TvRecyclerView tvRecyclerView = this.mGridView;
        if (tvRecyclerView == null) {
            return true;
        }
        tvRecyclerView.requestFocus();
        return true;
    }

    private void createView() {
        saveCurrentView();
        if (this.mGridView == null) {
            this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        } else {
            TvRecyclerView tvRecyclerView = new TvRecyclerView(this.mContext);
            tvRecyclerView.setSpacingWithMargins(10, 10);
            tvRecyclerView.setLayoutParams(this.mGridView.getLayoutParams());
            tvRecyclerView.setPadding(this.mGridView.getPaddingLeft(), this.mGridView.getPaddingTop(), this.mGridView.getPaddingRight(), this.mGridView.getPaddingBottom());
            tvRecyclerView.setClipToPadding(this.mGridView.getClipToPadding());
            ((ViewGroup) this.mGridView.getParent()).addView(tvRecyclerView);
            this.mGridView.setVisibility(8);
            this.mGridView = tvRecyclerView;
            tvRecyclerView.setVisibility(0);
        }
        this.mGridView.setHasFixedSize(true);
        this.gridAdapter = new GridAdapter(isFolederMode());
        this.page = 1;
        this.maxPage = 1;
        this.isLoad = false;
    }

    private void initView() {
        createView();
        this.mGridView.setAdapter(this.gridAdapter);
        if (isFolederMode()) {
            this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        } else {
            this.mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, isBaseOnWidth() ? 5 : 6));
        }
        this.gridAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.RequestLoadMoreListener
            public void onLoadMoreRequested() {
                GridFragment.this.gridAdapter.setEnableLoadMore(true);
                GridFragment.this.sourceViewModel.getList(GridFragment.this.sortData, GridFragment.this.page);
            }
        }, this.mGridView);
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass2 */

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
        this.mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass3 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
            public boolean onInBorderKeyEvent(int i, View view) {
                return false;
            }
        });
        this.gridAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass4 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Movie.Video video = (Movie.Video) GridFragment.this.gridAdapter.getData().get(i);
                if (video != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    bundle.putString("title", video.name);
                    SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
                    if ("12".indexOf(GridFragment.this.getUITag()) != -1 && video.tag.equals(Progress.FOLDER)) {
                        GridFragment.this.focusedView = view;
                        GridFragment.this.changeView(video.id);
                    } else if (video.id == null || video.id.isEmpty() || video.id.startsWith("msearch:")) {
                        GridFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                    } else if (homeSourceBean.isQuickSearch() && ((Boolean) Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)).booleanValue()) {
                        GridFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                    } else if (video.id == null || video.id.isEmpty()) {
                        GridFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                    } else {
                        GridFragment.this.jumpActivity(DetailActivity.class, bundle);
                    }
                }
            }
        });
        this.gridAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass5 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                Movie.Video video = (Movie.Video) GridFragment.this.gridAdapter.getData().get(i);
                if (video == null) {
                    return true;
                }
                Bundle bundle = new Bundle();
                bundle.putString("id", video.id);
                bundle.putString("sourceKey", video.sourceKey);
                bundle.putString("title", video.name);
                GridFragment.this.jumpActivity(FastSearchActivity.class, bundle);
                return true;
            }
        });
        this.gridAdapter.setLoadMoreView(new LoadMoreView());
        setLoadSir(this.mGridView);
    }

    private void initViewModel() {
        if (this.sourceViewModel == null) {
            SourceViewModel sourceViewModel2 = (SourceViewModel) new ViewModelProvider(this).get(SourceViewModel.class);
            this.sourceViewModel = sourceViewModel2;
            sourceViewModel2.listResult.observe(this, new Observer<AbsXml>() {
                /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass6 */

                public void onChanged(AbsXml absXml) {
                    if (absXml == null || absXml.movie == null || absXml.movie.videoList == null || absXml.movie.videoList.size() <= 0) {
                        if (GridFragment.this.page == 1) {
                            GridFragment.this.showEmpty();
                        }
                        if (GridFragment.this.page > GridFragment.this.maxPage) {
                            Toast.makeText(GridFragment.this.getContext(), "没有更多了", 0).show();
                            GridFragment.this.gridAdapter.loadMoreEnd();
                        } else {
                            GridFragment.this.gridAdapter.loadMoreComplete();
                        }
                        GridFragment.this.gridAdapter.setEnableLoadMore(false);
                        return;
                    }
                    if (GridFragment.this.page == 1) {
                        GridFragment.this.showSuccess();
                        GridFragment.this.isLoad = true;
                        GridFragment.this.gridAdapter.setNewData(absXml.movie.videoList);
                    } else {
                        GridFragment.this.gridAdapter.addData((Collection) absXml.movie.videoList);
                    }
                    GridFragment.access$308(GridFragment.this);
                    GridFragment.this.maxPage = absXml.movie.pagecount;
                    if (GridFragment.this.page > GridFragment.this.maxPage) {
                        GridFragment.this.gridAdapter.loadMoreEnd();
                        GridFragment.this.gridAdapter.setEnableLoadMore(false);
                        return;
                    }
                    GridFragment.this.gridAdapter.loadMoreComplete();
                    GridFragment.this.gridAdapter.setEnableLoadMore(true);
                }
            });
        }
    }

    public boolean isLoad() {
        return this.isLoad || !this.mGrids.empty();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initData() {
        showLoading();
        this.isLoad = false;
        scrollTop();
        toggleFilterStatus();
        this.sourceViewModel.getList(this.sortData, this.page);
    }

    private void toggleFilterStatus() {
        MovieSort.SortData sortData2 = this.sortData;
        if (sortData2 != null && sortData2.filters != null && !this.sortData.filters.isEmpty()) {
            EventBus.getDefault().post(new RefreshEvent(16, Integer.valueOf(this.sortData.filterSelectCount())));
        }
    }

    public boolean isTop() {
        return this.isTop;
    }

    public void scrollTop() {
        this.isTop = true;
        this.mGridView.scrollToPosition(0);
    }

    public void showFilter() {
        if (!this.sortData.filters.isEmpty() && this.gridFilterDialog == null) {
            GridFilterDialog gridFilterDialog2 = new GridFilterDialog(this.mContext);
            this.gridFilterDialog = gridFilterDialog2;
            gridFilterDialog2.setData(this.sortData);
            this.gridFilterDialog.setOnDismiss(new GridFilterDialog.Callback() {
                /* class com.github.tvbox.osc.ui.fragment.GridFragment.AnonymousClass7 */

                @Override // com.github.tvbox.osc.ui.dialog.GridFilterDialog.Callback
                public void change() {
                    GridFragment.this.page = 1;
                    GridFragment.this.initData();
                }
            });
        }
        GridFilterDialog gridFilterDialog3 = this.gridFilterDialog;
        if (gridFilterDialog3 != null) {
            gridFilterDialog3.show();
        }
    }

    public void forceRefresh() {
        this.page = 1;
        initData();
    }
}
