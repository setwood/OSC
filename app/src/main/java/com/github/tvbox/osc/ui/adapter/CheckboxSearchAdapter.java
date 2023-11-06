package com.github.tvbox.osc.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.util.SearchHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CheckboxSearchAdapter extends ListAdapter<SourceBean, ViewHolder> {
    private final ArrayList<SourceBean> data = new ArrayList<>();
    public HashMap<String, String> mCheckedSources = new HashMap<>();

    public CheckboxSearchAdapter(DiffUtil.ItemCallback<SourceBean> itemCallback) {
        super(itemCallback);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dialog_checkbox_search, viewGroup, false));
    }

    private void setCheckedSource(HashMap<String, String> hashMap) {
        this.mCheckedSources = hashMap;
    }

    public void setData(List<SourceBean> list, HashMap<String, String> hashMap) {
        this.data.clear();
        this.data.addAll(list);
        setCheckedSource(hashMap);
        notifyDataSetChanged();
    }

    public void setMCheckedSources() {
        SearchHelper.putCheckedSources(this.mCheckedSources, this.data.size() == this.mCheckedSources.size());
    }

    @Override // androidx.recyclerview.widget.ListAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.data.size();
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        final SourceBean sourceBean = this.data.get(adapterPosition);
        viewHolder.oneSearchSource.setText(sourceBean.getName());
        viewHolder.oneSearchSource.setOnCheckedChangeListener(null);
        if (this.mCheckedSources != null) {
            viewHolder.oneSearchSource.setChecked(this.mCheckedSources.containsKey(sourceBean.getKey()));
        }
        viewHolder.oneSearchSource.setTag(sourceBean);
        viewHolder.oneSearchSource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.github.tvbox.osc.ui.adapter.CheckboxSearchAdapter.AnonymousClass1 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    CheckboxSearchAdapter.this.mCheckedSources.put(sourceBean.getKey(), "1");
                } else {
                    CheckboxSearchAdapter.this.mCheckedSources.remove(sourceBean.getKey());
                }
                CheckboxSearchAdapter.this.notifyItemChanged(adapterPosition);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox oneSearchSource;

        public ViewHolder(View view) {
            super(view);
            this.oneSearchSource = (CheckBox) view.findViewById(R.id.oneSearchSource);
        }
    }
}
