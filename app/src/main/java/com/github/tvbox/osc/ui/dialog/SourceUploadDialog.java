package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SourceUploadDialog extends BaseDialog {
    private final EditText inputSourceUrl;
    private final ImageView ivQRCode;
    OnListener listener = null;
    private final TextView tvAddress;

    public interface OnListener {
        void onAdd(String str);

        void onReplace(String str);

        void onReset();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 15) {
            this.inputSourceUrl.setText((String) refreshEvent.obj);
        }
    }

    public SourceUploadDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_source_upload);
        setCanceledOnTouchOutside(true);
        this.ivQRCode = (ImageView) findViewById(R.id.sourceIvQRCode);
        this.tvAddress = (TextView) findViewById(R.id.sourceTvAddress);
        this.inputSourceUrl = (EditText) findViewById(R.id.inputSourceUrl);
        if (StringUtils.isNotEmpty(HomeActivity.getRes().getString(R.string.app_source))) {
            findViewById(R.id.sourceResetSubmit).setVisibility(0);
        }
        findViewById(R.id.sourceAddSubmit).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SourceUploadDialog.AnonymousClass1 */

            public void onClick(View view) {
                String trim = SourceUploadDialog.this.inputSourceUrl.getText().toString().trim();
                if (!trim.isEmpty()) {
                    SourceUploadDialog.this.listener.onAdd(trim);
                    SourceUploadDialog.this.dismiss();
                }
            }
        });
        findViewById(R.id.sourceReplaceSubmit).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SourceUploadDialog.AnonymousClass2 */

            public void onClick(View view) {
                String trim = SourceUploadDialog.this.inputSourceUrl.getText().toString().trim();
                if (!trim.isEmpty()) {
                    SourceUploadDialog.this.listener.onReplace(trim);
                    SourceUploadDialog.this.dismiss();
                }
            }
        });
        findViewById(R.id.sourceResetSubmit).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SourceUploadDialog.AnonymousClass3 */

            public void onClick(View view) {
                SourceUploadDialog.this.listener.onReset();
                SourceUploadDialog.this.dismiss();
            }
        });
        findViewById(R.id.sourceStoragePermission).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.SourceUploadDialog.AnonymousClass4 */

            public void onClick(View view) {
                if (XXPermissions.isGranted(SourceUploadDialog.this.getContext(), Permission.Group.STORAGE)) {
                    Toast.makeText(SourceUploadDialog.this.getContext(), "已获得存储权限", 0).show();
                } else {
                    XXPermissions.with(SourceUploadDialog.this.getContext()).permission(Permission.Group.STORAGE).request(new OnPermissionCallback() {
                        /* class com.github.tvbox.osc.ui.dialog.SourceUploadDialog.AnonymousClass4.AnonymousClass1 */

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onGranted(List<String> list, boolean z) {
                            if (z) {
                                Toast.makeText(SourceUploadDialog.this.getContext(), "已获得存储权限", 0).show();
                            }
                        }

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onDenied(List<String> list, boolean z) {
                            if (z) {
                                Toast.makeText(SourceUploadDialog.this.getContext(), "获取存储权限失败,请在系统设置中开启", 0).show();
                                XXPermissions.startPermissionActivity((Activity) SourceUploadDialog.this.getContext(), list);
                                return;
                            }
                            Toast.makeText(SourceUploadDialog.this.getContext(), "获取存储权限失败", 0).show();
                        }
                    });
                }
            }
        });
        refreshQRCode();
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        this.tvAddress.setText(String.format("手机/电脑扫描上方二维码或者直接浏览器访问地址\n%s", address));
        this.ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, AutoSizeUtils.mm2px(getContext(), 300.0f), AutoSizeUtils.mm2px(getContext(), 300.0f)));
    }

    public void setOnListener(OnListener onListener) {
        this.listener = onListener;
    }
}
