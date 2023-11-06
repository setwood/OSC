package com.github.tvbox.osc.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.github.tvbox.osc.R;
import java.util.ArrayList;
import java.util.List;

public class SelectDialogAdapter<T> extends ListAdapter<T, SelectViewHolder> {
    public static DiffUtil.ItemCallback<String> stringDiff = new DiffUtil.ItemCallback<String>() {
        /* class com.github.tvbox.osc.ui.adapter.SelectDialogAdapter.AnonymousClass1 */

        public boolean areItemsTheSame(String str, String str2) {
            return str.equals(str2);
        }

        public boolean areContentsTheSame(String str, String str2) {
            return str.equals(str2);
        }
    };
    private ArrayList<T> data;
    private SelectDialogInterface dialogInterface;
    private boolean muteCheck;
    private int select;

    public interface SelectDialogInterface<T> {
        void click(T t, int i);

        String getDisplay(T t);
    }

    /* access modifiers changed from: package-private */
    public class SelectViewHolder extends RecyclerView.ViewHolder {
        public SelectViewHolder(View view) {
            super(view);
        }
    }

    public SelectDialogAdapter(SelectDialogInterface selectDialogInterface, DiffUtil.ItemCallback itemCallback) {
        this(selectDialogInterface, itemCallback, false);
    }

    public SelectDialogAdapter(SelectDialogInterface selectDialogInterface, DiffUtil.ItemCallback itemCallback, boolean z) {
        super(itemCallback);
        this.muteCheck = false;
        this.data = new ArrayList<>();
        this.select = 0;
        this.dialogInterface = null;
        this.dialogInterface = selectDialogInterface;
        this.muteCheck = z;
    }

    public void setData(List<T> list, int i) {
        this.data.clear();
        this.data.addAll(list);
        this.select = i;
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.ListAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.data.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public SelectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dialog_select, viewGroup, false));
    }

    public void onBindViewHolder(SelectViewHolder selectViewHolder, final int i) {
        final T t = this.data.get(i);
        String display = this.dialogInterface.getDisplay(t);
        if (!this.muteCheck && i == this.select) {
            display = "√ " + display;
        }
        ((TextView) selectViewHolder.itemView.findViewById(R.id.tvName)).setText(display);
        selectViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.adapter.SelectDialogAdapter.AnonymousClass2 */

            /* JADX DEBUG: Multi-variable search result rejected for r3v7, resolved type: com.github.tvbox.osc.ui.adapter.SelectDialogAdapter$SelectDialogInterface */
            /* JADX WARN: Multi-variable type inference failed */
            public void onClick(View view) {
                if (SelectDialogAdapter.this.muteCheck || i != SelectDialogAdapter.this.select) {
                    SelectDialogAdapter selectDialogAdapter = SelectDialogAdapter.this;
                    selectDialogAdapter.notifyItemChanged(selectDialogAdapter.select);
                    SelectDialogAdapter.this.select = i;
                    SelectDialogAdapter selectDialogAdapter2 = SelectDialogAdapter.this;
                    selectDialogAdapter2.notifyItemChanged(selectDialogAdapter2.select);
                    SelectDialogAdapter.this.dialogInterface.click(t, i);
                }
            }
        });
    }
}
