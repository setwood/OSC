package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.StorageDrive;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.util.StorageDriveType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.greenrobot.eventbus.EventBus;

public class WebdavDialog extends BaseDialog {
    private StorageDrive drive = null;
    private EditText etInitPath;
    private EditText etName;
    private EditText etPassword;
    private EditText etUrl;
    private EditText etUsername;

    public WebdavDialog(Context context, StorageDrive storageDrive) {
        super(context);
        setContentView(R.layout.dialog_webdav);
        if (storageDrive != null) {
            this.drive = storageDrive;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.ui.dialog.BaseDialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.etName = (EditText) findViewById(R.id.etName);
        this.etUrl = (EditText) findViewById(R.id.etUrl);
        this.etInitPath = (EditText) findViewById(R.id.etInitPath);
        this.etUsername = (EditText) findViewById(R.id.etUsername);
        this.etPassword = (EditText) findViewById(R.id.etPassword);
        StorageDrive storageDrive = this.drive;
        if (storageDrive != null) {
            this.etName.setText(storageDrive.name);
            try {
                JsonObject asJsonObject = JsonParser.parseString(this.drive.configJson).getAsJsonObject();
                initSavedData(this.etUrl, asJsonObject, "url");
                initSavedData(this.etInitPath, asJsonObject, "initPath");
                initSavedData(this.etUsername, asJsonObject, "username");
                initSavedData(this.etPassword, asJsonObject, "password");
            } catch (Exception unused) {
            }
        }
        findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.WebdavDialog.AnonymousClass1 */

            public void onClick(View view) {
                String obj = WebdavDialog.this.etName.getText().toString();
                String obj2 = WebdavDialog.this.etUrl.getText().toString();
                String obj3 = WebdavDialog.this.etInitPath.getText().toString();
                String obj4 = WebdavDialog.this.etUsername.getText().toString();
                String obj5 = WebdavDialog.this.etPassword.getText().toString();
                if (obj == null || obj.length() == 0) {
                    Toast.makeText(WebdavDialog.this.getContext(), "请赋予一个空间名称", 0).show();
                } else if (obj2 == null || obj2.length() == 0) {
                    Toast.makeText(WebdavDialog.this.getContext(), "请务必填入WebDav地址", 0).show();
                } else {
                    if (!obj2.endsWith("/")) {
                        obj2 = obj2 + "/";
                    }
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("url", obj2);
                    if (obj3.length() > 0 && obj3.startsWith("/")) {
                        obj3 = obj3.substring(1);
                    }
                    if (obj3.length() > 0 && obj3.endsWith("/")) {
                        obj3 = obj3.substring(0, obj3.length() - 1);
                    }
                    jsonObject.addProperty("initPath", obj3);
                    jsonObject.addProperty("username", obj4);
                    jsonObject.addProperty("password", obj5);
                    if (WebdavDialog.this.drive != null) {
                        WebdavDialog.this.drive.name = obj;
                        WebdavDialog.this.drive.configJson = jsonObject.toString();
                        RoomDataManger.updateDriveRecord(WebdavDialog.this.drive);
                    } else {
                        RoomDataManger.insertDriveRecord(obj, StorageDriveType.TYPE.WEBDAV, jsonObject);
                    }
                    EventBus.getDefault().post(new RefreshEvent(10));
                    WebdavDialog.this.dismiss();
                }
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.WebdavDialog.AnonymousClass2 */

            public void onClick(View view) {
                WebdavDialog.this.dismiss();
            }
        });
    }

    private void initSavedData(EditText editText, JsonObject jsonObject, String str) {
        if (jsonObject.has(str)) {
            editText.setText(jsonObject.get(str).getAsString());
        }
    }
}
