package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.QuickSearchAdapter;
import com.github.tvbox.osc.ui.adapter.SearchWordAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class QuickSearchDialog extends BaseDialog {
    private TvRecyclerView mGridView;
    private TvRecyclerView mGridViewWord;
    private QuickSearchAdapter searchAdapter;
    private SearchWordAdapter searchWordAdapter;

    public QuickSearchDialog(Context context) {
        super(context, R.style.CustomDialogStyleDim);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setContentView(R.layout.dialog_quick_search);
        init(context);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 2) {
            if (refreshEvent.obj != null) {
                this.searchAdapter.addData((Collection) ((List) refreshEvent.obj));
            }
        } else if (refreshEvent.type == 4 && refreshEvent.obj != null) {
            this.searchWordAdapter.setNewData((List) refreshEvent.obj);
        }
    }

    private void init(Context context) {
        EventBus.getDefault().register(this);
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.github.tvbox.osc.ui.dialog.QuickSearchDialog.AnonymousClass1 */

            public void onDismiss(DialogInterface dialogInterface) {
                EventBus.getDefault().unregister(this);
            }
        });
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.searchAdapter = new QuickSearchAdapter();
        this.mGridView.setHasFixedSize(true);
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(getContext(), 1, false));
        this.mGridView.setAdapter(this.searchAdapter);
        this.searchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.QuickSearchDialog.AnonymousClass2 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                EventBus.getDefault().post(new RefreshEvent(3, (Movie.Video) QuickSearchDialog.this.searchAdapter.getData().get(i)));
                QuickSearchDialog.this.dismiss();
            }
        });
        this.searchAdapter.setNewData(new ArrayList());
        this.searchWordAdapter = new SearchWordAdapter();
        TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.mGridViewWord);
        this.mGridViewWord = tvRecyclerView;
        tvRecyclerView.setAdapter(this.searchWordAdapter);
        this.mGridViewWord.setLayoutManager(new V7LinearLayoutManager(context, 0, false));
        this.searchWordAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.QuickSearchDialog.AnonymousClass3 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                QuickSearchDialog.this.searchAdapter.getData().clear();
                QuickSearchDialog.this.searchAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new RefreshEvent(5, QuickSearchDialog.this.searchWordAdapter.getData().get(i)));
            }
        });
        this.searchWordAdapter.setNewData(new ArrayList());
    }
}
