package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.DiffUtil;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.adapter.CheckboxSearchAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import java.util.HashMap;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class SearchCheckboxDialog extends BaseDialog {
    LinearLayout checkAll;
    private CheckboxSearchAdapter checkboxSearchAdapter;
    LinearLayout clearAll;
    public HashMap<String, String> mCheckSourcees;
    private TvRecyclerView mGridView;
    private final List<SourceBean> mSourceList;

    public SearchCheckboxDialog(Context context, List<SourceBean> list, HashMap<String, String> hashMap) {
        super(context);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        this.mSourceList = list;
        this.mCheckSourcees = hashMap;
        setContentView(R.layout.dialog_checkbox_search);
        initView(context);
    }

    public void dismiss() {
        this.checkboxSearchAdapter.setMCheckedSources();
        super.dismiss();
    }

    /* access modifiers changed from: protected */
    public void initView(Context context) {
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.checkAll = (LinearLayout) findViewById(R.id.checkAll);
        this.clearAll = (LinearLayout) findViewById(R.id.clearAll);
        this.checkboxSearchAdapter = new CheckboxSearchAdapter(new DiffUtil.ItemCallback<SourceBean>() {
            /* class com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog.AnonymousClass1 */

            public boolean areItemsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                return sourceBean.getKey().equals(sourceBean2.getKey());
            }

            public boolean areContentsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                return sourceBean.getName().equals(sourceBean2.getName());
            }
        });
        this.mGridView.setHasFixedSize(true);
        int floor = (int) Math.floor((double) (this.mSourceList.size() / 10));
        if (floor <= 1) {
            floor = 2;
        }
        if (floor >= 3) {
            floor = 3;
        }
        this.mGridView.setLayoutManager(new V7GridLayoutManager(getContext(), floor));
        findViewById(2131296783).getLayoutParams().width = AutoSizeUtils.mm2px(getContext(), (float) (((floor - 1) * 260) + 400));
        this.mGridView.setAdapter(this.checkboxSearchAdapter);
        this.checkboxSearchAdapter.setData(this.mSourceList, this.mCheckSourcees);
        final int i = 0;
        if (this.mSourceList != null && this.mCheckSourcees != null) {
            int i2 = 0;
            while (true) {
                if (i2 >= this.mSourceList.size()) {
                    break;
                }
                if (this.mCheckSourcees.containsKey(this.mSourceList.get(i2).getKey())) {
                    i = i2;
                    break;
                }
                i2++;
            }
        }
        this.mGridView.post(new Runnable() {
            /* class com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog.AnonymousClass2 */

            public void run() {
                SearchCheckboxDialog.this.mGridView.smoothScrollToPosition(i);
            }
        });
        this.checkAll.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog.AnonymousClass3 */
            static final /* synthetic */ boolean $assertionsDisabled = false;

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SearchCheckboxDialog.this.mCheckSourcees = new HashMap<>();
                for (SourceBean sourceBean : SearchCheckboxDialog.this.mSourceList) {
                    SearchCheckboxDialog.this.mCheckSourcees.put(sourceBean.getKey(), "1");
                }
                SearchCheckboxDialog.this.checkboxSearchAdapter.setData(SearchCheckboxDialog.this.mSourceList, SearchCheckboxDialog.this.mCheckSourcees);
            }
        });
        this.clearAll.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SearchCheckboxDialog.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SearchCheckboxDialog.this.mCheckSourcees = new HashMap<>();
                SearchCheckboxDialog.this.checkboxSearchAdapter.setData(SearchCheckboxDialog.this.mSourceList, SearchCheckboxDialog.this.mCheckSourcees);
            }
        });
    }
}
