package com.github.tvbox.osc.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.SourceUtil;
import java.util.ArrayList;
import java.util.List;

public class ApiHistoryDialogAdapter extends ListAdapter<String, SelectViewHolder> {
    private ArrayList<String> data = new ArrayList<>();
    private SelectDialogInterface dialogInterface = null;
    private String select = "";

    public interface SelectDialogInterface {
        void click(String str);

        void del(String str, ArrayList<String> arrayList);
    }

    /* access modifiers changed from: package-private */
    public class SelectViewHolder extends RecyclerView.ViewHolder {
        public SelectViewHolder(View view) {
            super(view);
        }
    }

    public ApiHistoryDialogAdapter(SelectDialogInterface selectDialogInterface) {
        super(new DiffUtil.ItemCallback<String>() {
            /* class com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.AnonymousClass1 */

            public boolean areItemsTheSame(String str, String str2) {
                return str.equals(str2);
            }

            public boolean areContentsTheSame(String str, String str2) {
                return str.equals(str2);
            }
        });
        this.dialogInterface = selectDialogInterface;
    }

    public void setData(List<String> list, int i) {
        this.data.clear();
        this.data.addAll(list);
        this.select = this.data.get(i);
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.ListAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.data.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public SelectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dialog_api_history, viewGroup, false));
    }

    public void onBindViewHolder(SelectViewHolder selectViewHolder, int i) {
        final String str = this.data.get(i);
        String apiName = SourceUtil.getApiName(str);
        if (this.select.equals(str)) {
            apiName = "√ " + apiName;
        }
        ((TextView) selectViewHolder.itemView.findViewById(R.id.tvName)).setText(apiName);
        selectViewHolder.itemView.findViewById(R.id.tvName).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.AnonymousClass2 */

            public void onClick(View view) {
                if (!ApiHistoryDialogAdapter.this.select.equals(str)) {
                    ApiHistoryDialogAdapter apiHistoryDialogAdapter = ApiHistoryDialogAdapter.this;
                    apiHistoryDialogAdapter.notifyItemChanged(apiHistoryDialogAdapter.data.indexOf(ApiHistoryDialogAdapter.this.select));
                    ApiHistoryDialogAdapter.this.select = str;
                    ApiHistoryDialogAdapter apiHistoryDialogAdapter2 = ApiHistoryDialogAdapter.this;
                    apiHistoryDialogAdapter2.notifyItemChanged(apiHistoryDialogAdapter2.data.indexOf(str));
                    ApiHistoryDialogAdapter.this.dialogInterface.click(str);
                }
            }
        });
        selectViewHolder.itemView.findViewById(R.id.tvDel).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.AnonymousClass3 */

            public void onClick(View view) {
                if (!ApiHistoryDialogAdapter.this.select.equals(str)) {
                    ApiHistoryDialogAdapter apiHistoryDialogAdapter = ApiHistoryDialogAdapter.this;
                    apiHistoryDialogAdapter.notifyItemRemoved(apiHistoryDialogAdapter.data.indexOf(str));
                    ApiHistoryDialogAdapter.this.data.remove(str);
                    ApiHistoryDialogAdapter.this.dialogInterface.del(str, ApiHistoryDialogAdapter.this.data);
                }
            }
        });
    }
}
