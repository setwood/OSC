package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.ApiModel;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.SourceUtil;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.orhanobut.hawk.Hawk;
import java.util.ArrayList;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ApiDialog extends BaseDialog {
    private final EditText inputApi;
    private final EditText inputApiName;
    private final EditText inputEPG;
    private final EditText inputLive;
    private final ImageView ivQRCode;
    OnListener listener = null;
    private final TextView tvAddress;

    public interface OnListener {
        void onchange(String str);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 8) {
            ApiModel apiModel = (ApiModel) refreshEvent.obj;
            this.inputApiName.setText(apiModel.getName());
            this.inputApi.setText(apiModel.getUrl());
        }
        if (refreshEvent.type == 13) {
            this.inputLive.setText((String) refreshEvent.obj);
        }
        if (refreshEvent.type == 14) {
            this.inputEPG.setText((String) refreshEvent.obj);
        }
    }

    public ApiDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_api);
        setCanceledOnTouchOutside(true);
        this.ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
        this.tvAddress = (TextView) findViewById(R.id.tvAddress);
        EditText editText = (EditText) findViewById(R.id.input);
        this.inputApi = editText;
        editText.setText((CharSequence) Hawk.get(HawkConfig.API_URL, ""));
        EditText editText2 = (EditText) findViewById(R.id.inputApiName);
        this.inputApiName = editText2;
        editText2.setText((CharSequence) Hawk.get(HawkConfig.API_NAME, ""));
        EditText editText3 = (EditText) findViewById(R.id.input_live);
        this.inputLive = editText3;
        editText3.setText((CharSequence) Hawk.get(HawkConfig.LIVE_URL, ""));
        EditText editText4 = (EditText) findViewById(R.id.input_epg);
        this.inputEPG = editText4;
        editText4.setText((CharSequence) Hawk.get(HawkConfig.EPG_URL, ""));
        findViewById(R.id.inputSubmit).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass1 */

            public void onClick(View view) {
                String trim = ApiDialog.this.inputApi.getText().toString().trim();
                String trim2 = ApiDialog.this.inputApiName.getText().toString().trim();
                String trim3 = ApiDialog.this.inputLive.getText().toString().trim();
                String trim4 = ApiDialog.this.inputEPG.getText().toString().trim();
                if (trim.startsWith("file://")) {
                    trim = trim.replace("file://", "clan://localhost/");
                } else if (trim.startsWith("./")) {
                    trim = trim.replace("./", "clan://localhost/");
                }
                if (!trim.isEmpty()) {
                    if (trim2.isEmpty()) {
                        trim2 = trim;
                    }
                    ApiModel apiModel = new ApiModel();
                    apiModel.setUrl(trim);
                    apiModel.setName(trim2);
                    SourceUtil.setCurrentApi(apiModel);
                    SourceUtil.addHistory(apiModel);
                    ApiDialog.this.listener.onchange(trim);
                    ApiDialog.this.dismiss();
                }
                Hawk.put(HawkConfig.LIVE_URL, trim3);
                if (!trim3.isEmpty()) {
                    ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.LIVE_HISTORY, new ArrayList());
                    if (!arrayList.contains(trim3)) {
                        arrayList.add(0, trim3);
                    }
                    if (arrayList.size() > 20) {
                        arrayList.remove(20);
                    }
                    Hawk.put(HawkConfig.LIVE_HISTORY, arrayList);
                }
                Hawk.put(HawkConfig.EPG_URL, trim4);
                if (!trim4.isEmpty()) {
                    ArrayList arrayList2 = (ArrayList) Hawk.get(HawkConfig.EPG_HISTORY, new ArrayList());
                    if (!arrayList2.contains(trim4)) {
                        arrayList2.add(0, trim4);
                    }
                    if (arrayList2.size() > 20) {
                        arrayList2.remove(20);
                    }
                    Hawk.put(HawkConfig.EPG_HISTORY, arrayList2);
                }
            }
        });
        findViewById(R.id.apiHistory).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass2 */

            public void onClick(View view) {
                List<String> historyApiUrls = SourceUtil.getHistoryApiUrls();
                if (!historyApiUrls.isEmpty()) {
                    String url = SourceUtil.getCurrentApi().getUrl();
                    int i = 0;
                    if (historyApiUrls.contains(url)) {
                        i = historyApiUrls.indexOf(url);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ApiDialog.this.getContext());
                    apiHistoryDialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_list));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass2.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            ApiDialog.this.inputApiName.setText(SourceUtil.getApiName(str));
                            ApiDialog.this.inputApi.setText(str);
                            ApiDialog.this.listener.onchange(str);
                            apiHistoryDialog.dismiss();
                        }

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void del(String str, ArrayList<String> arrayList) {
                            SourceUtil.removeHistory(str);
                        }
                    }, historyApiUrls, i);
                    apiHistoryDialog.show();
                }
            }
        });
        findViewById(R.id.liveHistory).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass3 */

            public void onClick(View view) {
                ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.LIVE_HISTORY, new ArrayList());
                if (!arrayList.isEmpty()) {
                    String str = (String) Hawk.get(HawkConfig.LIVE_URL, "");
                    int i = 0;
                    if (arrayList.contains(str)) {
                        i = arrayList.indexOf(str);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ApiDialog.this.getContext());
                    apiHistoryDialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_live));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass3.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            ApiDialog.this.inputLive.setText(str);
                            Hawk.put(HawkConfig.LIVE_URL, str);
                            apiHistoryDialog.dismiss();
                        }

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void del(String str, ArrayList<String> arrayList) {
                            Hawk.put(HawkConfig.LIVE_HISTORY, arrayList);
                        }
                    }, arrayList, i);
                    apiHistoryDialog.show();
                }
            }
        });
        findViewById(2131296264).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass4 */

            public void onClick(View view) {
                ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.EPG_HISTORY, new ArrayList());
                if (!arrayList.isEmpty()) {
                    String str = (String) Hawk.get(HawkConfig.EPG_URL, "");
                    int i = 0;
                    if (arrayList.contains(str)) {
                        i = arrayList.indexOf(str);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ApiDialog.this.getContext());
                    apiHistoryDialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_epg));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass4.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            ApiDialog.this.inputEPG.setText(str);
                            Hawk.put(HawkConfig.EPG_URL, str);
                            apiHistoryDialog.dismiss();
                        }

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void del(String str, ArrayList<String> arrayList) {
                            Hawk.put(HawkConfig.EPG_HISTORY, arrayList);
                        }
                    }, arrayList, i);
                    apiHistoryDialog.show();
                }
            }
        });
        findViewById(R.id.storagePermission).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass5 */

            public void onClick(View view) {
                if (XXPermissions.isGranted(ApiDialog.this.getContext(), Permission.Group.STORAGE)) {
                    Toast.makeText(ApiDialog.this.getContext(), "已获得存储权限", 0).show();
                } else {
                    XXPermissions.with(ApiDialog.this.getContext()).permission(Permission.Group.STORAGE).request(new OnPermissionCallback() {
                        /* class com.github.tvbox.osc.ui.dialog.ApiDialog.AnonymousClass5.AnonymousClass1 */

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onGranted(List<String> list, boolean z) {
                            if (z) {
                                Toast.makeText(ApiDialog.this.getContext(), "已获得存储权限", 0).show();
                            }
                        }

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onDenied(List<String> list, boolean z) {
                            if (z) {
                                Toast.makeText(ApiDialog.this.getContext(), "获取存储权限失败,请在系统设置中开启", 0).show();
                                XXPermissions.startPermissionActivity((Activity) ApiDialog.this.getContext(), list);
                                return;
                            }
                            Toast.makeText(ApiDialog.this.getContext(), "获取存储权限失败", 0).show();
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
