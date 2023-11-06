package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.data.AppDataManager;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.adapter.BackupAdapter;
import com.github.tvbox.osc.util.FileUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class BackupDialog extends BaseDialog {
    public BackupDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_backup);
        final BackupAdapter backupAdapter = new BackupAdapter();
        ((TvRecyclerView) findViewById(R.id.list)).setAdapter(backupAdapter);
        backupAdapter.setNewData(allBackup());
        backupAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.BackupDialog.AnonymousClass1 */

            @Override // com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (view.getId() == 2131296992) {
                    BackupDialog.this.restore((String) baseQuickAdapter.getItem(i));
                } else if (view.getId() == 2131296967) {
                    BackupDialog.this.delete((String) baseQuickAdapter.getItem(i));
                    baseQuickAdapter.setNewData(BackupDialog.this.allBackup());
                }
            }
        });
        findViewById(R.id.backupNow).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.BackupDialog.AnonymousClass2 */

            public void onClick(View view) {
                BackupDialog.this.backup();
                backupAdapter.setNewData(BackupDialog.this.allBackup());
            }
        });
        findViewById(R.id.storagePermission).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.dialog.BackupDialog.AnonymousClass3 */

            public void onClick(View view) {
                if (XXPermissions.isGranted(BackupDialog.this.getContext(), Permission.Group.STORAGE)) {
                    Toast.makeText(BackupDialog.this.getContext(), HomeActivity.getRes().getString(R.string.set_permission_ok), 0).show();
                } else {
                    XXPermissions.with(BackupDialog.this.getContext()).permission(Permission.Group.STORAGE).request(new OnPermissionCallback() {
                        /* class com.github.tvbox.osc.ui.dialog.BackupDialog.AnonymousClass3.AnonymousClass1 */

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onGranted(List<String> list, boolean z) {
                            if (z) {
                                backupAdapter.setNewData(BackupDialog.this.allBackup());
                                Toast.makeText(BackupDialog.this.getContext(), HomeActivity.getRes().getString(R.string.set_permission_ok), 0).show();
                            }
                        }

                        @Override // com.hjq.permissions.OnPermissionCallback
                        public void onDenied(List<String> list, boolean z) {
                            if (z) {
                                Toast.makeText(BackupDialog.this.getContext(), HomeActivity.getRes().getString(R.string.set_permission_fail2), 0).show();
                                XXPermissions.startPermissionActivity((Activity) BackupDialog.this.getContext(), list);
                                return;
                            }
                            Toast.makeText(BackupDialog.this.getContext(), HomeActivity.getRes().getString(R.string.set_permission_fail1), 0).show();
                        }
                    });
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public List<String> allBackup() {
        ArrayList arrayList = new ArrayList();
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tvbox_backup/");
            File[] listFiles = file.listFiles();
            Arrays.sort(listFiles, new Comparator<File>() {
                /* class com.github.tvbox.osc.ui.dialog.BackupDialog.AnonymousClass4 */

                public int compare(File file, File file2) {
                    if (file.isDirectory() && file2.isFile()) {
                        return -1;
                    }
                    if (!file.isFile() || !file2.isDirectory()) {
                        return file2.getName().compareTo(file.getName());
                    }
                    return 1;
                }
            });
            if (file.exists()) {
                for (File file2 : listFiles) {
                    if (arrayList.size() > 10) {
                        FileUtils.recursiveDelete(file2);
                    } else if (file2.isDirectory()) {
                        arrayList.add(file2.getName());
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public void restore(String str) {
        try {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(absolutePath + "/tvbox_backup/" + str);
            if (!file.exists()) {
                return;
            }
            if (AppDataManager.restore(new File(file, "sqlite"))) {
                byte[] readSimple = FileUtils.readSimple(new File(file, "hawk"));
                if (readSimple != null) {
                    JSONObject jSONObject = new JSONObject(new String(readSimple, "UTF-8"));
                    Iterator<String> keys = jSONObject.keys();
                    SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("Hawk2", 0);
                    while (keys.hasNext()) {
                        String next = keys.next();
                        String string = jSONObject.getString(next);
                        if (next.equals("cipher_key")) {
                            App.getInstance().getSharedPreferences("crypto.KEY_256", 0).edit().putString(next, string).commit();
                        } else {
                            sharedPreferences.edit().putString(next, string).commit();
                        }
                    }
                    Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_rest_ok), 0).show();
                    return;
                }
                Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_rest_fail_hk), 0).show();
                return;
            }
            Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_rest_fail_db), 0).show();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void backup() {
        try {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(absolutePath + "/tvbox_backup/");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
            file2.mkdirs();
            if (AppDataManager.backup(new File(file2, "sqlite"))) {
                SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("Hawk2", 0);
                JSONObject jSONObject = new JSONObject();
                for (String str : sharedPreferences.getAll().keySet()) {
                    jSONObject.put(str, sharedPreferences.getString(str, ""));
                }
                SharedPreferences sharedPreferences2 = App.getInstance().getSharedPreferences("crypto.KEY_256", 0);
                for (String str2 : sharedPreferences2.getAll().keySet()) {
                    jSONObject.put(str2, sharedPreferences2.getString(str2, ""));
                }
                if (!FileUtils.writeSimple(jSONObject.toString().getBytes("UTF-8"), new File(file2, "hawk"))) {
                    file2.delete();
                    Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_bkup_fail_hk), 0).show();
                    return;
                }
                Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_bkup_ok), 0).show();
                return;
            }
            Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_bkup_fail_db), 0).show();
            file2.delete();
        } catch (Throwable th) {
            th.printStackTrace();
            Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_bkup_fail), 0).show();
        }
    }

    /* access modifiers changed from: package-private */
    public void delete(String str) {
        try {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileUtils.recursiveDelete(new File(absolutePath + "/tvbox_backup/" + str));
            Toast.makeText(getContext(), HomeActivity.getRes().getString(R.string.set_bkup_del), 0).show();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
