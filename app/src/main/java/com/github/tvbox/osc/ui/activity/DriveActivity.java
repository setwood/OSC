package com.github.tvbox.osc.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.StorageDrive;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.DriveAdapter;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.AlistDriveDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.WebdavDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.StorageDriveType;
import com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel;
import com.github.tvbox.osc.viewmodel.drive.AlistDriveViewModel;
import com.github.tvbox.osc.viewmodel.drive.LocalDriveViewModel;
import com.github.tvbox.osc.viewmodel.drive.WebDAVDriveViewModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hjq.permissions.Permission;
import com.lzy.okgo.OkGo;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DriveActivity extends BaseActivity {
    private DriveAdapter adapter = new DriveAdapter();
    private AtomicInteger allSearchCount = new AtomicInteger(0);
    private AbstractDriveViewModel backupViewModel = null;
    private ImageButton btnAddServer;
    private ImageButton btnRemoveServer;
    private ImageButton btnSort;
    private int currentSelected = 0;
    private boolean delMode = false;
    private List<DriveFolderFile> drives = null;
    private View footLoading;
    private boolean isInSearch = false;
    private boolean isRight;
    private TvRecyclerView mGridView;
    private Handler mHandler = new Handler();
    private ExecutorService searchExecutorService = null;
    List<DriveFolderFile> searchResult = null;
    private boolean sortChange = false;
    private Comparator<DriveFolderFile> sortComparator = new Comparator<DriveFolderFile>() {
        /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass7 */

        public int compare(DriveFolderFile driveFolderFile, DriveFolderFile driveFolderFile2) {
            long longValue;
            long longValue2;
            int i = DriveActivity.this.sortType;
            if (i == 1) {
                return Collator.getInstance(Locale.CHINESE).compare(driveFolderFile2.name.toUpperCase(Locale.CHINESE), driveFolderFile.name.toUpperCase(Locale.CHINESE));
            }
            if (i == 2) {
                longValue = driveFolderFile.lastModifiedDate.longValue();
                longValue2 = driveFolderFile2.lastModifiedDate.longValue();
            } else if (i != 3) {
                return Collator.getInstance(Locale.CHINESE).compare(driveFolderFile.name.toUpperCase(Locale.CHINESE), driveFolderFile2.name.toUpperCase(Locale.CHINESE));
            } else {
                longValue = driveFolderFile2.lastModifiedDate.longValue();
                longValue2 = driveFolderFile.lastModifiedDate.longValue();
            }
            return (longValue > longValue2 ? 1 : (longValue == longValue2 ? 0 : -1));
        }
    };
    public View sortFocusView = null;
    private int sortFocused = 0;
    private int sortType = 0;
    private TextView txtTitle;
    private AbstractDriveViewModel viewModel = null;

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public int getLayoutResID() {
        return R.layout.activity_drive;
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseActivity
    public void init() {
        initView();
        initData();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        this.searchExecutorService = Executors.newFixedThreadPool(5);
        this.txtTitle = (TextView) findViewById(R.id.textView);
        this.btnAddServer = (ImageButton) findViewById(R.id.btnAddServer);
        this.mGridView = (TvRecyclerView) findViewById(R.id.mGridView);
        this.btnRemoveServer = (ImageButton) findViewById(R.id.btnRemoveServer);
        this.btnSort = (ImageButton) findViewById(R.id.btnSort);
        View inflate = getLayoutInflater().inflate(R.layout.item_search_lite, (ViewGroup) null);
        this.footLoading = inflate;
        inflate.findViewById(R.id.tvName).setVisibility(8);
        this.btnRemoveServer.setColorFilter(ContextCompat.getColor(this.mContext, 2131034178));
        this.btnRemoveServer.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass1 */

            public void onClick(View view) {
                DriveActivity.this.toggleDelMode();
            }
        });
        findViewById(R.id.btnHome).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass2 */

            public void onClick(View view) {
                DriveActivity.super.onBackPressed();
            }
        });
        this.btnSort.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                DriveActivity.this.openSortDialog();
            }
        });
        this.btnAddServer.setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                StorageDriveType.TYPE[] values = StorageDriveType.TYPE.values();
                final SelectDialog selectDialog = new SelectDialog(DriveActivity.this);
                selectDialog.setTip("请选择存盘类型");
                selectDialog.setItemCheckDisplay(false);
                final String[] typeNames = StorageDriveType.getTypeNames();
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<StorageDriveType.TYPE>() {
                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass4.AnonymousClass1 */

                    public void click(StorageDriveType.TYPE type, int i) {
                        if (type == StorageDriveType.TYPE.LOCAL) {
                            if (Build.VERSION.SDK_INT < 23 || App.getInstance().checkSelfPermission(Permission.WRITE_EXTERNAL_STORAGE) == 0) {
                                DriveActivity.this.openFilePicker();
                                selectDialog.dismiss();
                                return;
                            }
                            ActivityCompat.requestPermissions(DriveActivity.this, new String[]{Permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else if (type == StorageDriveType.TYPE.WEBDAV) {
                            DriveActivity.this.openWebdavDialog(null);
                            selectDialog.dismiss();
                        } else if (type == StorageDriveType.TYPE.ALISTWEB) {
                            DriveActivity.this.openAlistDriveDialog(null);
                            selectDialog.dismiss();
                        }
                    }

                    public String getDisplay(StorageDriveType.TYPE type) {
                        return typeNames[type.ordinal()];
                    }
                }, new DiffUtil.ItemCallback<StorageDriveType.TYPE>() {
                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass4.AnonymousClass2 */

                    public boolean areItemsTheSame(StorageDriveType.TYPE type, StorageDriveType.TYPE type2) {
                        return type.equals(type2);
                    }

                    public boolean areContentsTheSame(StorageDriveType.TYPE type, StorageDriveType.TYPE type2) {
                        return type.equals(type2);
                    }
                }, Arrays.asList(values), 0);
                selectDialog.show();
            }
        });
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        this.mGridView.setSpacingWithMargins(AutoSizeUtils.mm2px(this.mContext, 10.0f), 0);
        this.mGridView.setAdapter(this.adapter);
        this.adapter.bindToRecyclerView(this.mGridView);
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass5 */

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (i >= 0) {
                    ((DriveFolderFile) DriveActivity.this.adapter.getData().get(i)).isSelected = false;
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int i) {
                if (i >= 0) {
                    ((DriveFolderFile) DriveActivity.this.adapter.getData().get(i)).isSelected = true;
                }
            }

            @Override // com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
            public void onItemClick(TvRecyclerView tvRecyclerView, View view, int i) {
                if (DriveActivity.this.delMode) {
                    RoomDataManger.deleteDrive(((DriveFolderFile) DriveActivity.this.drives.get(i)).getDriveData().getId());
                    EventBus.getDefault().post(new RefreshEvent(10));
                    return;
                }
                DriveActivity.this.btnAddServer.setVisibility(8);
                DriveActivity.this.btnRemoveServer.setVisibility(8);
                DriveFolderFile driveFolderFile = (DriveFolderFile) DriveActivity.this.adapter.getItem(i);
                if ((driveFolderFile == driveFolderFile.parentFolder || driveFolderFile.parentFolder == null) && driveFolderFile.name == null) {
                    DriveActivity.this.returnPreviousFolder();
                    return;
                }
                if (DriveActivity.this.viewModel == null) {
                    if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.LOCAL) {
                        DriveActivity.this.viewModel = new LocalDriveViewModel();
                    } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
                        DriveActivity.this.viewModel = new WebDAVDriveViewModel();
                    } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.ALISTWEB) {
                        DriveActivity.this.viewModel = new AlistDriveViewModel();
                    }
                    DriveActivity.this.viewModel.setCurrentDrive(driveFolderFile);
                    if (!driveFolderFile.isFile) {
                        DriveActivity.this.loadDriveData();
                        return;
                    }
                }
                if (!driveFolderFile.isFile) {
                    DriveActivity.this.viewModel.setCurrentDriveNote(driveFolderFile);
                    DriveActivity.this.loadDriveData();
                } else if (StorageDriveType.isVideoType(driveFolderFile.fileType)) {
                    DriveFolderFile currentDrive = DriveActivity.this.viewModel.getCurrentDrive();
                    if (currentDrive.getDriveType() == StorageDriveType.TYPE.LOCAL) {
                        DriveActivity.this.playFile(currentDrive.name + driveFolderFile.getAccessingPathStr() + driveFolderFile.name);
                    } else if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
                        JsonObject config = currentDrive.getConfig();
                        DriveActivity driveActivity = DriveActivity.this;
                        driveActivity.playFile(config.get("url").getAsString() + (driveFolderFile.getAccessingPathStr() + driveFolderFile.name));
                    } else if (currentDrive.getDriveType() == StorageDriveType.TYPE.ALISTWEB) {
                        ((AlistDriveViewModel) DriveActivity.this.viewModel).loadFile(driveFolderFile, new AlistDriveViewModel.LoadFileCallback() {
                            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass5.AnonymousClass1 */

                            @Override // com.github.tvbox.osc.viewmodel.drive.AlistDriveViewModel.LoadFileCallback
                            public void callback(final String str) {
                                DriveActivity.this.mHandler.post(new Runnable() {
                                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass5.AnonymousClass1.AnonymousClass1 */

                                    public void run() {
                                        DriveActivity.this.playFile(str);
                                    }
                                });
                            }

                            @Override // com.github.tvbox.osc.viewmodel.drive.AlistDriveViewModel.LoadFileCallback
                            public void fail(final String str) {
                                DriveActivity.this.mHandler.post(new Runnable() {
                                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass5.AnonymousClass1.AnonymousClass2 */

                                    public void run() {
                                        Toast.makeText(DriveActivity.this.mContext, str, 0).show();
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Toast.makeText(DriveActivity.this, "Media Unsupported", 0).show();
                }
            }
        });
        setLoadSir(findViewById(R.id.mLayout));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playFile(String str) {
        String webDAVBase64Credential;
        VodInfo vodInfo = new VodInfo();
        vodInfo.name = "存储";
        vodInfo.playFlag = "drive";
        DriveFolderFile currentDrive = this.viewModel.getCurrentDrive();
        if (currentDrive.getDriveType() == StorageDriveType.TYPE.WEBDAV && (webDAVBase64Credential = currentDrive.getWebDAVBase64Credential()) != null) {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(JsonParser.parseString("{ \"name\": \"authorization\", \"value\": \"Basic " + webDAVBase64Credential + "\" }"));
            jsonObject.add("headers", jsonArray);
            vodInfo.playerCfg = jsonObject.toString();
        }
        vodInfo.seriesFlags = new ArrayList<>();
        vodInfo.seriesFlags.add(new VodInfo.VodSeriesFlag("drive"));
        vodInfo.seriesMap = new LinkedHashMap<>();
        VodInfo.VodSeries vodSeries = new VodInfo.VodSeries(str, "tvbox-drive://" + str);
        ArrayList arrayList = new ArrayList();
        arrayList.add(vodSeries);
        vodInfo.seriesMap.put("drive", arrayList);
        Bundle bundle = new Bundle();
        bundle.putBoolean("newSource", true);
        bundle.putString("sourceKey", "_drive");
        bundle.putSerializable("VodInfo", vodInfo);
        jumpActivity(PlayActivity.class, bundle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openSortDialog() {
        List asList = Arrays.asList("按名字升序", "按名字降序", "按修改时间升序", "按修改时间降序");
        int intValue = ((Integer) Hawk.get(HawkConfig.STORAGE_DRIVE_SORT, 0)).intValue();
        final SelectDialog selectDialog = new SelectDialog(this);
        selectDialog.setTip("请选择列表排序方式");
        selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<String>() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass6 */

            public String getDisplay(String str) {
                return str;
            }

            public void click(String str, int i) {
                DriveActivity.this.sortType = i;
                Hawk.put(HawkConfig.STORAGE_DRIVE_SORT, Integer.valueOf(i));
                selectDialog.dismiss();
                DriveActivity.this.loadDriveData();
            }
        }, null, asList, intValue);
        selectDialog.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openFilePicker() {
        if (this.delMode) {
            toggleDelMode();
        }
        new ChooserDialog(this.mContext, 2131820792).withStringResources("选择一个文件夹", "确定", "取消").titleFollowsDir(true).displayPath(true).enableDpad(true).withFilter(true, true, new String[0]).withChosenListener(new ChooserDialog.Result() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass8 */

            @Override // com.obsez.android.lib.filechooser.ChooserDialog.Result
            public void onChoosePath(String str, File file) {
                String absolutePath = file.getAbsolutePath();
                for (DriveFolderFile driveFolderFile : DriveActivity.this.drives) {
                    if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.LOCAL && absolutePath.equals(driveFolderFile.getDriveData().name)) {
                        Toast.makeText(DriveActivity.this.mContext, "此文件夹之前已被添加到空间列表！", 0).show();
                        return;
                    }
                }
                RoomDataManger.insertDriveRecord(absolutePath, StorageDriveType.TYPE.LOCAL, null);
                EventBus.getDefault().post(new RefreshEvent(10));
            }
        }).show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openWebdavDialog(StorageDrive storageDrive) {
        new WebdavDialog(this.mContext, storageDrive).show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openAlistDriveDialog(StorageDrive storageDrive) {
        new AlistDriveDialog(this.mContext, storageDrive).show();
    }

    public void toggleDelMode() {
        boolean z = !this.delMode;
        this.delMode = z;
        if (z) {
            this.btnRemoveServer.setColorFilter(ContextCompat.getColor(this.mContext, R.color.color_theme));
        } else {
            this.btnRemoveServer.setColorFilter(ContextCompat.getColor(this.mContext, 2131034178));
        }
        this.adapter.toggleDelMode(this.delMode);
    }

    private void initData() {
        this.txtTitle.setText(getString(R.string.act_drive));
        this.sortType = ((Integer) Hawk.get(HawkConfig.STORAGE_DRIVE_SORT, 0)).intValue();
        this.btnSort.setVisibility(8);
        if (this.drives == null) {
            this.drives = new ArrayList();
            for (StorageDrive storageDrive : RoomDataManger.getAllDrives()) {
                DriveFolderFile driveFolderFile = new DriveFolderFile(storageDrive);
                if (this.delMode) {
                    driveFolderFile.isDelMode = true;
                }
                this.drives.add(driveFolderFile);
            }
        }
        this.adapter.setNewData(this.drives);
        setSelectedItem(this.drives);
        this.btnAddServer.setVisibility(0);
        this.btnRemoveServer.setVisibility(0);
        showSuccess();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSelectedItem(List<DriveFolderFile> list) {
        for (final int i = 0; i < list.size(); i++) {
            if (list.get(i).isSelected) {
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass9 */

                    public void run() {
                        DriveActivity.this.mGridView.setSelection(i);
                    }
                }, 50);
                return;
            }
        }
        this.mGridView.setSelection(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadDriveData() {
        this.viewModel.setSortType(this.sortType);
        this.btnSort.setVisibility(0);
        showLoading();
        this.txtTitle.setText(this.viewModel.loadData(new AbstractDriveViewModel.LoadDataCallback() {
            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass10 */

            @Override // com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel.LoadDataCallback
            public void callback(List<DriveFolderFile> list, final boolean z) {
                DriveActivity.this.mHandler.post(new Runnable() {
                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass10.AnonymousClass1 */

                    public void run() {
                        DriveActivity.this.showSuccess();
                        if (z) {
                            DriveActivity.this.adapter.setNewData(DriveActivity.this.viewModel.getCurrentDriveNote().getChildren());
                            DriveActivity.this.setSelectedItem(DriveActivity.this.viewModel.getCurrentDriveNote().getChildren());
                            return;
                        }
                        DriveActivity.this.adapter.setNewData(DriveActivity.this.viewModel.getCurrentDriveNote().getChildren());
                        DriveActivity.this.mHandler.postDelayed(new Runnable() {
                            /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass10.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                DriveActivity.this.mGridView.setSelection(0);
                            }
                        }, 50);
                    }
                });
            }

            @Override // com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel.LoadDataCallback
            public void fail(final String str) {
                DriveActivity.this.showSuccess();
                DriveActivity.this.mHandler.post(new Runnable() {
                    /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass10.AnonymousClass2 */

                    public void run() {
                        Toast.makeText(DriveActivity.this.mContext, str, 0).show();
                    }
                });
            }
        }));
    }

    private void search(String str) {
        AbstractDriveViewModel abstractDriveViewModel;
        this.isInSearch = true;
        this.backupViewModel = this.viewModel;
        this.viewModel = null;
        this.btnSort.setVisibility(8);
        showLoading();
        ArrayList<AbstractDriveViewModel> arrayList = new ArrayList();
        AbstractDriveViewModel abstractDriveViewModel2 = this.viewModel;
        if (abstractDriveViewModel2 != null) {
            arrayList.add(abstractDriveViewModel2);
        } else {
            for (DriveFolderFile driveFolderFile : this.drives) {
                if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.LOCAL) {
                    abstractDriveViewModel = new LocalDriveViewModel();
                } else if (driveFolderFile.getDriveType() == StorageDriveType.TYPE.WEBDAV) {
                    abstractDriveViewModel = new WebDAVDriveViewModel();
                } else {
                    abstractDriveViewModel = driveFolderFile.getDriveType() == StorageDriveType.TYPE.ALISTWEB ? new AlistDriveViewModel() : null;
                }
                if (abstractDriveViewModel != null) {
                    this.allSearchCount.incrementAndGet();
                    abstractDriveViewModel.setCurrentDrive(driveFolderFile);
                    arrayList.add(abstractDriveViewModel);
                }
            }
        }
        this.searchResult = new ArrayList();
        DriveFolderFile driveFolderFile2 = new DriveFolderFile(null, null, false, null, null);
        driveFolderFile2.parentFolder = driveFolderFile2;
        this.searchResult.add(0, driveFolderFile2);
        this.adapter.setNewData(this.searchResult);
        this.adapter.setFooterView(this.footLoading);
        final Object obj = new Object();
        for (AbstractDriveViewModel abstractDriveViewModel3 : arrayList) {
            this.searchExecutorService.execute(abstractDriveViewModel3.search(str, new AbstractDriveViewModel.LoadDataCallback() {
                /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass11 */

                @Override // com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel.LoadDataCallback
                public void callback(final List<DriveFolderFile> list, boolean z) {
                    DriveActivity.this.mHandler.post(new Runnable() {
                        /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass11.AnonymousClass1 */

                        public void run() {
                            synchronized (obj) {
                                try {
                                    DriveActivity.this.showSuccess();
                                    if (DriveActivity.this.allSearchCount.decrementAndGet() <= 0) {
                                        DriveActivity.this.adapter.removeFooterView(DriveActivity.this.footLoading);
                                    }
                                    if (list != null) {
                                        DriveActivity.this.searchResult.addAll(list);
                                        DriveActivity.this.adapter.notifyDataSetChanged();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

                @Override // com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel.LoadDataCallback
                public void fail(final String str) {
                    DriveActivity.this.showSuccess();
                    DriveActivity.this.mHandler.post(new Runnable() {
                        /* class com.github.tvbox.osc.ui.activity.DriveActivity.AnonymousClass11.AnonymousClass2 */

                        public void run() {
                            Toast.makeText(DriveActivity.this.mContext, str, 0).show();
                        }
                    });
                }
            }));
        }
        this.txtTitle.setText("搜索结果");
    }

    private void cancel() {
        OkGo.getInstance().cancelTag("drive");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void returnPreviousFolder() {
        if (!this.isInSearch || this.viewModel != null) {
            this.viewModel.getCurrentDriveNote().setChildren(null);
            AbstractDriveViewModel abstractDriveViewModel = this.viewModel;
            abstractDriveViewModel.setCurrentDriveNote(abstractDriveViewModel.getCurrentDriveNote().parentFolder);
            if (this.viewModel.getCurrentDriveNote() != null) {
                loadDriveData();
            } else if (this.isInSearch) {
                this.txtTitle.setText("搜索结果");
                this.adapter.setNewData(this.searchResult);
                this.viewModel = null;
            } else {
                this.viewModel = null;
                initData();
            }
        } else {
            this.isInSearch = false;
            AbstractDriveViewModel abstractDriveViewModel2 = this.backupViewModel;
            this.viewModel = abstractDriveViewModel2;
            this.backupViewModel = null;
            if (abstractDriveViewModel2 == null) {
                initData();
            } else {
                loadDriveData();
            }
        }
    }

    @Override // androidx.activity.ComponentActivity
    public void onBackPressed() {
        if (this.viewModel != null) {
            cancel();
            TvRecyclerView tvRecyclerView = this.mGridView;
            tvRecyclerView.onClick(tvRecyclerView.getChildAt(0));
        } else if (!this.delMode) {
            super.onBackPressed();
        } else {
            toggleDelMode();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent refreshEvent) {
        if (refreshEvent.type == 10) {
            this.drives = null;
            initData();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, com.github.tvbox.osc.base.BaseActivity
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
