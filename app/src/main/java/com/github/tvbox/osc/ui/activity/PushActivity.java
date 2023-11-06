package com.github.tvbox.osc.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class PushActivity extends BaseActivity {
    private ImageView ivQRCode;
    private TextView tvAddress;

    private void initData() {
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_push;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        initView();
        initData();
    }

    private void initView() {
        this.ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
        this.tvAddress = (TextView) findViewById(R.id.tvAddress);
        refreshQRCode();
        findViewById(R.id.pushLocal).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.PushActivity.AnonymousClass1 */

            public void onClick(View view) {
                try {
                    ClipboardManager clipboardManager = (ClipboardManager) PushActivity.this.getSystemService("clipboard");
                    if (clipboardManager != null && clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip() != null && clipboardManager.getPrimaryClip().getItemCount() > 0) {
                        ClipData.Item itemAt = clipboardManager.getPrimaryClip().getItemAt(0);
                        Intent intent = new Intent(PushActivity.this.mContext, DetailActivity.class);
                        intent.putExtra("id", itemAt.getText().toString().trim());
                        intent.putExtra("sourceKey", "push_agent");
                        intent.setFlags(335544320);
                        PushActivity.this.startActivity(intent);
                    }
                } catch (Throwable unused) {
                }
            }
        });
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        this.tvAddress.setText(String.format("手机/电脑扫描上方二维码或者直接浏览器访问地址\n%s", address));
        this.ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, AutoSizeUtils.mm2px(this, 300.0f), AutoSizeUtils.mm2px(this, 300.0f), 4));
    }
}
