package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.thegrizzlylabs.sardineandroid.util.SardineUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchKeyboard extends FrameLayout {
    private View.OnFocusChangeListener focusChangeListener;
    private List<Keyboard> keyboardList;
    private List<String> keys;
    private RecyclerView mRecyclerView;
    private OnSearchKeyListener searchKeyListener;

    public interface OnSearchKeyListener {
        void onSearchKey(int i, String str);
    }

    public SearchKeyboard(Context context) {
        this(context, null);
    }

    public SearchKeyboard(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SearchKeyboard(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.keys = Arrays.asList(HomeActivity.getRes().getString(R.string.act_search_rem), HomeActivity.getRes().getString(R.string.act_search_del), ExifInterface.GPS_MEASUREMENT_IN_PROGRESS, "B", "C", SardineUtil.DEFAULT_NAMESPACE_PREFIX, ExifInterface.LONGITUDE_EAST, "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", ExifInterface.LATITUDE_SOUTH, ExifInterface.GPS_DIRECTION_TRUE, "U", ExifInterface.GPS_MEASUREMENT_INTERRUPTED, ExifInterface.LONGITUDE_WEST, "X", "Y", "Z", "1", ExifInterface.GPS_MEASUREMENT_2D, ExifInterface.GPS_MEASUREMENT_3D, "4", "5", "6", "7", "8", "9", SessionDescription.SUPPORTED_SDP_VERSION);
        this.keyboardList = new ArrayList();
        this.focusChangeListener = new View.OnFocusChangeListener() {
            /* class com.github.tvbox.osc.ui.tv.widget.SearchKeyboard.AnonymousClass1 */

            public void onFocusChange(View view, boolean z) {
                if (view != null && view != SearchKeyboard.this.mRecyclerView) {
                    view.setSelected(z);
                }
            }
        };
        initView();
    }

    private void initView() {
        this.mRecyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_keyborad, this).findViewById(R.id.mRecyclerView);
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        this.mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            /* class com.github.tvbox.osc.ui.tv.widget.SearchKeyboard.AnonymousClass2 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
            public void onChildViewDetachedFromWindow(View view) {
            }

            @Override // androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
            public void onChildViewAttachedToWindow(View view) {
                if (view.isFocusable() && view.getOnFocusChangeListener() == null) {
                    view.setOnFocusChangeListener(SearchKeyboard.this.focusChangeListener);
                }
            }
        });
        int size = this.keys.size();
        for (int i = 0; i < size; i++) {
            this.keyboardList.add(new Keyboard(1, this.keys.get(i)));
        }
        KeyboardAdapter keyboardAdapter = new KeyboardAdapter(this.keyboardList);
        this.mRecyclerView.setAdapter(keyboardAdapter);
        keyboardAdapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            /* class com.github.tvbox.osc.ui.tv.widget.SearchKeyboard.AnonymousClass3 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.SpanSizeLookup
            public int getSpanSize(GridLayoutManager gridLayoutManager, int i) {
                return (i == 0 || i == 1) ? 3 : 1;
            }
        });
        keyboardAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            /* class com.github.tvbox.osc.ui.tv.widget.SearchKeyboard.AnonymousClass4 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Keyboard keyboard = (Keyboard) baseQuickAdapter.getItem(i);
                if (SearchKeyboard.this.searchKeyListener != null) {
                    SearchKeyboard.this.searchKeyListener.onSearchKey(i, keyboard.getKey());
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public static class Keyboard implements MultiItemEntity {
        private int itemType;
        private String key;

        private Keyboard(int i, String str) {
            this.itemType = i;
            this.key = str;
        }

        @Override // com.chad.library.adapter.base.entity.MultiItemEntity
        public int getItemType() {
            return this.itemType;
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String str) {
            this.key = str;
        }
    }

    /* access modifiers changed from: private */
    public static class KeyboardAdapter extends BaseMultiItemQuickAdapter<Keyboard, BaseViewHolder> {
        private KeyboardAdapter(List<Keyboard> list) {
            super(list);
            addItemType(1, R.layout.item_keyboard);
        }

        /* access modifiers changed from: protected */
        public void convert(BaseViewHolder baseViewHolder, Keyboard keyboard) {
            if (baseViewHolder.getItemViewType() == 1) {
                baseViewHolder.setText(R.id.keyName, keyboard.key);
            }
        }
    }

    public void setOnSearchKeyListener(OnSearchKeyListener onSearchKeyListener) {
        this.searchKeyListener = onSearchKeyListener;
    }
}
