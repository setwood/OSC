package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.ui.adapter.GridFilterKVAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.util.ArrayList;
import java.util.Iterator;

public class GridFilterDialog extends BaseDialog {
    private LinearLayout filterRoot;
    private boolean selectChange = false;

    public interface Callback {
        void change();
    }

    public GridFilterDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setContentView(R.layout.dialog_grid_filter);
        this.filterRoot = (LinearLayout) findViewById(R.id.filterRoot);
    }

    public void setOnDismiss(final Callback callback) {
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.github.tvbox.osc.ui.dialog.GridFilterDialog.AnonymousClass1 */

            public void onDismiss(DialogInterface dialogInterface) {
                if (GridFilterDialog.this.selectChange) {
                    callback.change();
                }
            }
        });
    }

    public void setData(final MovieSort.SortData sortData) {
        Iterator<MovieSort.SortFilter> it = sortData.filters.iterator();
        while (it.hasNext()) {
            MovieSort.SortFilter next = it.next();
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_grid_filter, (ViewGroup) null);
            ((TextView) inflate.findViewById(R.id.filterName)).setText(next.name);
            TvRecyclerView tvRecyclerView = (TvRecyclerView) inflate.findViewById(R.id.mFilterKv);
            tvRecyclerView.setHasFixedSize(true);
            tvRecyclerView.setLayoutManager(new V7LinearLayoutManager(getContext(), 0, false));
            GridFilterKVAdapter gridFilterKVAdapter = new GridFilterKVAdapter();
            tvRecyclerView.setAdapter(gridFilterKVAdapter);
            final String str = next.key;
            ArrayList arrayList = new ArrayList(next.values.keySet());
            final ArrayList arrayList2 = new ArrayList(next.values.values());
            gridFilterKVAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                /* class com.github.tvbox.osc.ui.dialog.GridFilterDialog.AnonymousClass2 */
                View pre = null;

                @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                    GridFilterDialog.this.selectChange = true;
                    String str = sortData.filterSelect.get(str);
                    if (str == null || !str.equals(arrayList2.get(i))) {
                        sortData.filterSelect.put(str, (String) arrayList2.get(i));
                        View view2 = this.pre;
                        if (view2 != null) {
                            TextView textView = (TextView) view2.findViewById(R.id.filterValue);
                            textView.getPaint().setFakeBoldText(false);
                            textView.setTextColor(GridFilterDialog.this.getContext().getResources().getColor(2131034178));
                        }
                        TextView textView2 = (TextView) view.findViewById(R.id.filterValue);
                        textView2.getPaint().setFakeBoldText(true);
                        textView2.setTextColor(GridFilterDialog.this.getContext().getResources().getColor(R.color.color_theme));
                        this.pre = view;
                        return;
                    }
                    sortData.filterSelect.remove(str);
                    TextView textView3 = (TextView) this.pre.findViewById(R.id.filterValue);
                    textView3.getPaint().setFakeBoldText(false);
                    textView3.setTextColor(GridFilterDialog.this.getContext().getResources().getColor(2131034178));
                    this.pre = null;
                }
            });
            gridFilterKVAdapter.setNewData(arrayList);
            this.filterRoot.addView(inflate);
        }
    }

    @Override // com.github.tvbox.osc.ui.dialog.BaseDialog
    public void show() {
        this.selectChange = false;
        super.show();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.gravity = 80;
        attributes.width = -1;
        attributes.height = -2;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(attributes);
    }
}
