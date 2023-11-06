package com.github.tvbox.osc.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.ui.dialog.AlistDriveDialog;
import com.github.tvbox.osc.ui.dialog.WebdavDialog;
import com.github.tvbox.osc.util.StorageDriveType;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import java.util.ArrayList;

public class DriveAdapter extends BaseQuickAdapter<DriveFolderFile, BaseViewHolder> {
    public DriveAdapter() {
        super(R.layout.item_drive, new ArrayList());
    }

    public void toggleDelMode(boolean z) {
        for (int i = 0; i < getItemCount(); i++) {
            View viewByPosition = getViewByPosition(i, R.id.delDrive);
            int i2 = 8;
            if (viewByPosition != null) {
                viewByPosition.setVisibility(z ? 0 : 8);
            }
            DriveFolderFile driveFolderFile = (DriveFolderFile) getItem(i);
            driveFolderFile.isDelMode = z;
            if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.WEBDAV || driveFolderFile.getDriveType() == StorageDriveType.TYPE.ALISTWEB) {
                View viewByPosition2 = getViewByPosition(i, R.id.imgConfig);
                if (!z) {
                    i2 = 0;
                }
                viewByPosition2.setVisibility(i2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void convert(final BaseViewHolder baseViewHolder, final DriveFolderFile driveFolderFile) {
        TextView textView = (TextView) baseViewHolder.getView(R.id.txtItemName);
        if (driveFolderFile.name == null && driveFolderFile.parentFolder == driveFolderFile) {
            textView.setText(" . . ");
        } else {
            textView.setText(driveFolderFile.name);
        }
        ImageView imageView = (ImageView) baseViewHolder.getView(R.id.imgItem);
        final TextView textView2 = (TextView) baseViewHolder.getView(R.id.txtMediaName);
        int i = 8;
        textView2.setVisibility(8);
        TextView textView3 = (TextView) baseViewHolder.getView(R.id.txtModifiedDate);
        textView3.setVisibility(8);
        ImageView imageView2 = (ImageView) baseViewHolder.getView(R.id.imgConfig);
        imageView2.setVisibility(8);
        LinearLayout linearLayout = (LinearLayout) baseViewHolder.getView(R.id.mItemLayout);
        baseViewHolder.setGone(R.id.delDrive, driveFolderFile.isDelMode);
        linearLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            /* class com.github.tvbox.osc.ui.adapter.DriveAdapter.AnonymousClass1 */

            public void onFocusChange(View view, boolean z) {
                textView2.setSelected(z);
                ((TvRecyclerView) baseViewHolder.itemView.getParent()).onFocusChange(baseViewHolder.itemView, z);
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.adapter.DriveAdapter.AnonymousClass2 */

            public void onClick(View view) {
                ((TvRecyclerView) baseViewHolder.itemView.getParent()).onClick(baseViewHolder.itemView);
            }
        });
        if (!driveFolderFile.isDrive()) {
            textView3.setText(driveFolderFile.getFormattedLastModified());
            textView3.setVisibility(0);
            if (driveFolderFile.isFile) {
                if (driveFolderFile.fileType != null) {
                    textView2.setText(driveFolderFile.fileType);
                    textView2.setVisibility(0);
                }
                if (StorageDriveType.isVideoType(driveFolderFile.fileType)) {
                    imageView.setImageResource(R.drawable.icon_film);
                } else {
                    imageView.setImageResource(R.drawable.icon_file);
                }
            } else {
                imageView.setImageResource(R.drawable.icon_folder);
            }
        } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.LOCAL) {
            imageView.setImageResource(R.drawable.icon_sdcard);
        } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
            imageView.setImageResource(R.drawable.icon_circle_node);
            if (!driveFolderFile.isDelMode) {
                i = 0;
            }
            imageView2.setVisibility(i);
            imageView2.setOnClickListener(new View.OnClickListener() {
                /* class com.github.tvbox.osc.ui.adapter.DriveAdapter.AnonymousClass3 */

                public void onClick(View view) {
                    new WebdavDialog(DriveAdapter.this.mContext, driveFolderFile.getDriveData()).show();
                }
            });
        } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.ALISTWEB) {
            imageView.setImageResource(R.drawable.icon_alist);
            if (!driveFolderFile.isDelMode) {
                i = 0;
            }
            imageView2.setVisibility(i);
            imageView2.setOnClickListener(new View.OnClickListener() {
                /* class com.github.tvbox.osc.ui.adapter.DriveAdapter.AnonymousClass4 */

                public void onClick(View view) {
                    new AlistDriveDialog(DriveAdapter.this.mContext, driveFolderFile.getDriveData()).show();
                }
            });
        }
    }
}
