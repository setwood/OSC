package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import me.jessyan.autosize.utils.AutoSizeUtils;

public class RemoteDialog extends BaseDialog {
    private ImageView ivQRCode = ((ImageView) findViewById(R.id.ivQRCode));
    private TextView tvAddress = ((TextView) findViewById(R.id.tvAddress));

    public RemoteDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_remote);
        setCanceledOnTouchOutside(false);
        refreshQRCode();
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        this.tvAddress.setText(String.format("手机/电脑扫描上方二维码或者直接浏览器访问地址\n%s", address));
        this.ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, AutoSizeUtils.mm2px(getContext(), 240.0f), AutoSizeUtils.mm2px(getContext(), 240.0f)));
    }
}
