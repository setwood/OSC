package com.github.tvbox.osc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.VodCollect;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.CollectAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CollectActivity extends BaseActivity {
    private CollectAdapter collectAdapter;
    private boolean delMode = false;
    private TvRecyclerView mGridView;
    private TextView tvDel;
    private TextView tvDelTip;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_collect;
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
        CollectAdapter collectAdapter2 = new CollectAdapter();
        this.collectAdapter = collectAdapter2;
        this.mGridView.setAdapter(collectAdapter2);
        this.tvDel.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.CollectActivity.AnonymousClass1 */

            public void onClick(View view) {
                CollectActivity.this.toggleDelMode();
            }
        });
        this.mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            /* class com.github.tvbox.osc.ui.activity.CollectActivity.AnonymousClass2 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
            public boolean onInBorderKeyEvent(int i, View view) {
                if (i != 33) {
                    return false;
                }
                CollectActivity.this.tvDel.setFocusable(true);
                CollectActivity.this.tvDel.requestFocus();
                return false;
            }
        });
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.CollectActivity.AnonymousClass3 */

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
        this.collectAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.activity.CollectActivity.AnonymousClass4 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                VodCollect vodCollect = (VodCollect) CollectActivity.this.collectAdapter.getData().get(i);
                if (vodCollect == null) {
                    return;
                }
                if (CollectActivity.this.delMode) {
                    CollectActivity.this.collectAdapter.remove(i);
                    RoomDataManger.deleteVodCollect(vodCollect.getId());
                } else if (ApiConfig.get().getSource(vodCollect.sourceKey) != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", vodCollect.vodId);
                    bundle.putString("sourceKey", vodCollect.sourceKey);
                    CollectActivity.this.jumpActivity(DetailActivity.class, bundle);
                } else {
                    Intent intent = new Intent(CollectActivity.this.mContext, FastSearchActivity.class);
                    intent.putExtra("title", vodCollect.name);
                    intent.setFlags(335544320);
                    CollectActivity.this.startActivity(intent);
                }
            }
        });
        this.collectAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            /* class com.github.tvbox.osc.ui.activity.CollectActivity.AnonymousClass5 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                FastClickCheckUtil.check(view);
                CollectActivity.this.collectAdapter.remove(i);
                RoomDataManger.deleteVodCollect(((VodCollect) CollectActivity.this.collectAdapter.getData().get(i)).getId());
                return true;
            }
        });
    }

    private void initData() {
        List<VodCollect> allVodCollect = RoomDataManger.getAllVodCollect();
        ArrayList arrayList = new ArrayList();
        for (VodCollect vodCollect : allVodCollect) {
            arrayList.add(vodCollect);
        }
        this.collectAdapter.setNewData(arrayList);
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
