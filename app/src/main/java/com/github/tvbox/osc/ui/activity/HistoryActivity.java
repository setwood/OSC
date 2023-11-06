package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.HistoryAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HistoryActivity extends BaseActivity {
    private boolean delMode = false;
    private HistoryAdapter historyAdapter;
    private TvRecyclerView mGridView;
    private TextView tvDel;
    private TextView tvDelTip;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_history;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        initView();
        initData();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void toggleDelMode() {
        boolean z = !this.delMode;
        this.delMode = z;
        this.tvDelTip.setVisibility(z ? 0 : 8);
        this.tvDel.setTextColor(this.delMode ? getResources().getColor(R.color.color_theme) : -1);
    }

    private void initView() {
        EventBus.getDefault().register(this);
        this.tvDel = (TextView) findViewById(R.id.tvDel);
        this.tvDelTip = (TextView) findViewById(R.id.tvDelTip);
        TvRecyclerView tvRecyclerView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.mGridView = tvRecyclerView;
        tvRecyclerView.setHasFixedSize(true);
        this.mGridView.setLayoutManager(new V7GridLayoutManager(this.mContext, isBaseOnWidth() ? 5 : 6));
        HistoryAdapter historyAdapter2 = new HistoryAdapter();
        this.historyAdapter = historyAdapter2;
        this.mGridView.setAdapter(historyAdapter2);
        this.tvDel.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HistoryActivity.AnonymousClass1 */

            public void onClick(View view) {
                HistoryActivity.this.toggleDelMode();
            }
        });
        this.mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            /* class com.github.tvbox.osc.ui.activity.HistoryActivity.AnonymousClass2 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
            public boolean onInBorderKeyEvent(int i, View view) {
                if (i != 33) {
                    return false;
                }
                HistoryActivity.this.tvDel.setFocusable(true);
                HistoryActivity.this.tvDel.requestFocus();
                return false;
            }
        });
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.HistoryActivity.AnonymousClass3 */

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
        this.historyAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HistoryActivity.AnonymousClass4 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                VodInfo vodInfo = (VodInfo) HistoryActivity.this.historyAdapter.getData().get(i);
                if (vodInfo == null) {
                    return;
                }
                if (HistoryActivity.this.delMode) {
                    HistoryActivity.this.historyAdapter.remove(i);
                    RoomDataManger.deleteVodRecord(vodInfo.sourceKey, vodInfo);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("id", vodInfo.id);
                bundle.putString("sourceKey", vodInfo.sourceKey);
                HistoryActivity.this.jumpActivity(DetailActivity.class, bundle);
            }
        });
        this.historyAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            /* class com.github.tvbox.osc.ui.activity.HistoryActivity.AnonymousClass5 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                VodInfo vodInfo = (VodInfo) HistoryActivity.this.historyAdapter.getData().get(i);
                HistoryActivity.this.historyAdapter.remove(i);
                RoomDataManger.deleteVodRecord(vodInfo.sourceKey, vodInfo);
                return true;
            }
        });
    }

    private void initData() {
        List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord(100);
        ArrayList arrayList = new ArrayList();
        for (VodInfo vodInfo : allVodRecord) {
            if (vodInfo.playNote != null && !vodInfo.playNote.isEmpty()) {
                vodInfo.note = "上次看到" + vodInfo.playNote;
            }
            arrayList.add(vodInfo);
        }
        this.historyAdapter.setNewData(arrayList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 1) {
            initData();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        if (this.delMode) {
            toggleDelMode();
        } else {
            super.onBackPressed();
        }
    }
}
