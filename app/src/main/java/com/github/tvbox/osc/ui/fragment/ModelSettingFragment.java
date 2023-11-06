package com.github.tvbox.osc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.DiffUtil;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.AboutDialog;
import com.github.tvbox.osc.ui.dialog.ApiDialog;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.BackupDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.SourceUploadDialog;
import com.github.tvbox.osc.ui.dialog.XWalkInitDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.HistoryHelper;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.SourceUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class ModelSettingFragment extends BaseLazyFragment {
    private TextView tvApi;
    private TextView tvDebugOpen;
    private TextView tvDns;
    private TextView tvEpg;
    private TextView tvFastSearchView;
    private TextView tvHomeApi;
    private TextView tvHomeNum;
    private TextView tvHomeRec;
    private TextView tvHomeShow;
    private TextView tvIjkCachePlay;
    private TextView tvLive;
    private TextView tvLocale;
    private TextView tvMediaCodec;
    private TextView tvPIP;
    private TextView tvParseWebView;
    private TextView tvPlay;
    private TextView tvRecStyleText;
    private TextView tvRender;
    private TextView tvScale;
    private TextView tvSearchView;
    private TextView tvShowPreviewText;

    /* access modifiers changed from: package-private */
    public String getHomeRecName(int i) {
        return i == 1 ? "站点推荐" : i == 2 ? "观看历史" : "豆瓣热播";
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public int getLayoutResID() {
        return R.layout.fragment_model;
    }

    /* access modifiers changed from: package-private */
    public String getLocaleView(int i) {
        return i == 0 ? "中文" : "英文";
    }

    /* access modifiers changed from: package-private */
    public String getSearchView(int i) {
        return i == 0 ? "文字列表" : "缩略图";
    }

    public ModelSettingFragment setArguments() {
        return this;
    }

    public static ModelSettingFragment newInstance() {
        return new ModelSettingFragment().setArguments();
    }

    /* access modifiers changed from: protected */
    @Override // com.github.tvbox.osc.base.BaseLazyFragment
    public void init() {
        String str;
        String str2;
        String str3;
        TextView textView = (TextView) findViewById(R.id.tvPIP);
        this.tvPIP = textView;
        String str4 = "开启";
        textView.setText(((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, false)).booleanValue() ? str4 : "关闭");
        TextView textView2 = (TextView) findViewById(R.id.showRecStyleText);
        this.tvRecStyleText = textView2;
        textView2.setText(((Boolean) Hawk.get(HawkConfig.HOME_REC_STYLE, true)).booleanValue() ? "是" : "否");
        TextView textView3 = (TextView) findViewById(R.id.tvLocale);
        this.tvLocale = textView3;
        textView3.setText(getLocaleView(((Integer) Hawk.get("language", 0)).intValue()));
        TextView textView4 = (TextView) findViewById(R.id.tvHomeShow);
        this.tvHomeShow = textView4;
        if (((Boolean) Hawk.get(HawkConfig.HOME_SHOW_SOURCE, false)).booleanValue()) {
            str = str4;
        } else {
            str = "关闭";
        }
        textView4.setText(str);
        TextView textView5 = (TextView) findViewById(R.id.showPreviewText);
        this.tvShowPreviewText = textView5;
        if (((Boolean) Hawk.get(HawkConfig.SHOW_PREVIEW, true)).booleanValue()) {
            str2 = str4;
        } else {
            str2 = "关闭";
        }
        textView5.setText(str2);
        this.tvDebugOpen = (TextView) findViewById(R.id.tvDebugOpen);
        this.tvParseWebView = (TextView) findViewById(R.id.tvParseWebView);
        this.tvMediaCodec = (TextView) findViewById(R.id.tvMediaCodec);
        this.tvPlay = (TextView) findViewById(R.id.tvPlay);
        this.tvRender = (TextView) findViewById(R.id.tvRenderType);
        this.tvScale = (TextView) findViewById(R.id.tvScaleType);
        this.tvApi = (TextView) findViewById(R.id.tvApi);
        this.tvLive = (TextView) findViewById(R.id.tvLive);
        this.tvEpg = (TextView) findViewById(R.id.tvEpg);
        this.tvHomeApi = (TextView) findViewById(R.id.tvHomeApi);
        this.tvDns = (TextView) findViewById(R.id.tvDns);
        this.tvHomeRec = (TextView) findViewById(R.id.tvHomeRec);
        this.tvHomeNum = (TextView) findViewById(R.id.tvHomeNum);
        this.tvSearchView = (TextView) findViewById(R.id.tvSearchView);
        this.tvFastSearchView = (TextView) findViewById(R.id.tvFastSearchView);
        this.tvMediaCodec.setText((CharSequence) Hawk.get(HawkConfig.IJK_CODEC, ""));
        TextView textView6 = this.tvDebugOpen;
        if (((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue()) {
            str3 = str4;
        } else {
            str3 = "关闭";
        }
        textView6.setText(str3);
        this.tvParseWebView.setText(((Boolean) Hawk.get(HawkConfig.PARSE_WEBVIEW, true)).booleanValue() ? "系统自带" : "XWalkView");
        this.tvApi.setText(SourceUtil.getCurrentApi().getName());
        this.tvLive.setText((CharSequence) Hawk.get(HawkConfig.LIVE_URL, ""));
        this.tvEpg.setText((CharSequence) Hawk.get(HawkConfig.EPG_URL, ""));
        this.tvDns.setText(OkGoHelper.dnsHttpsList.get(((Integer) Hawk.get(HawkConfig.DOH_URL, 0)).intValue()));
        this.tvHomeRec.setText(getHomeRecName(((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue()));
        this.tvHomeNum.setText(HistoryHelper.getHomeRecName(((Integer) Hawk.get(HawkConfig.HOME_NUM, 0)).intValue()));
        this.tvSearchView.setText(getSearchView(((Integer) Hawk.get(HawkConfig.SEARCH_VIEW, 0)).intValue()));
        this.tvFastSearchView.setText(((Boolean) Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)).booleanValue() ? "已开启" : "已关闭");
        this.tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());
        this.tvScale.setText(PlayerHelper.getScaleName(((Integer) Hawk.get(HawkConfig.PLAY_SCALE, 0)).intValue()));
        this.tvPlay.setText(PlayerHelper.getPlayerName(((Integer) Hawk.get(HawkConfig.PLAY_TYPE, 0)).intValue()));
        this.tvRender.setText(PlayerHelper.getRenderName(((Integer) Hawk.get(HawkConfig.PLAY_RENDER, 0)).intValue()));
        TextView textView7 = (TextView) findViewById(R.id.tvIjkCachePlay);
        this.tvIjkCachePlay = textView7;
        if (!((Boolean) Hawk.get(HawkConfig.IJK_CACHE_PLAY, false)).booleanValue()) {
            str4 = "关闭";
        }
        textView7.setText(str4);
        findViewById(R.id.llHomeApi).requestFocus();
        findViewById(R.id.llDebug).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass1 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.DEBUG_OPEN, Boolean.valueOf(!((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue()));
                ModelSettingFragment.this.tvDebugOpen.setText(((Boolean) Hawk.get(HawkConfig.DEBUG_OPEN, false)).booleanValue() ? "开启" : "关闭");
            }
        });
        findViewById(R.id.llApi).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass2 */

            public void onClick(View view) {
                List<String> historyApiUrls = SourceUtil.getHistoryApiUrls();
                if (!historyApiUrls.isEmpty()) {
                    String url = SourceUtil.getCurrentApi().getUrl();
                    int i = 0;
                    if (historyApiUrls.contains(url)) {
                        i = historyApiUrls.indexOf(url);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ModelSettingFragment.this.getContext());
                    apiHistoryDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_history_list));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass2.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            SourceUtil.setCurrentApi(str);
                            ModelSettingFragment.this.tvApi.setText(SourceUtil.getCurrentApi().getName());
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
        findViewById(R.id.llApiAdd).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass3 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                ApiDialog apiDialog = new ApiDialog(ModelSettingFragment.this.mActivity);
                EventBus.getDefault().register(apiDialog);
                apiDialog.setOnListener(new ApiDialog.OnListener() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass3.AnonymousClass1 */

                    @Override // com.github.tvbox.osc.ui.dialog.ApiDialog.OnListener
                    public void onchange(String str) {
                        SourceUtil.setCurrentApi(str);
                        ModelSettingFragment.this.tvApi.setText(SourceUtil.getCurrentApi().getName());
                    }
                });
                apiDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass3.AnonymousClass2 */

                    public void onDismiss(DialogInterface dialogInterface) {
                        ((BaseActivity) ModelSettingFragment.this.mActivity).hideSystemUI(true);
                        EventBus.getDefault().unregister(dialogInterface);
                    }
                });
                apiDialog.show();
            }
        });
        findViewById(R.id.llApiUpload).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                SourceUploadDialog sourceUploadDialog = new SourceUploadDialog(ModelSettingFragment.this.mActivity);
                EventBus.getDefault().register(sourceUploadDialog);
                sourceUploadDialog.setOnListener(new SourceUploadDialog.OnListener() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4.AnonymousClass1 */

                    @Override // com.github.tvbox.osc.ui.dialog.SourceUploadDialog.OnListener
                    public void onAdd(String str) {
                        Toast.makeText(ModelSettingFragment.this.mActivity, "导入中...", 1).show();
                        SourceUtil.addSource(str, new SourceUtil.Callback<String>() {
                            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4.AnonymousClass1.AnonymousClass1 */

                            public void success(String str) {
                                Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                            }

                            @Override // com.github.tvbox.osc.util.SourceUtil.Callback
                            public void error(String str) {
                                Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                            }
                        });
                    }

                    @Override // com.github.tvbox.osc.ui.dialog.SourceUploadDialog.OnListener
                    public void onReplace(String str) {
                        Toast.makeText(ModelSettingFragment.this.mActivity, "导入中...", 1).show();
                        SourceUtil.replaceAllSource(str, new SourceUtil.Callback<String>() {
                            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4.AnonymousClass1.AnonymousClass2 */

                            public void success(String str) {
                                Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                            }

                            @Override // com.github.tvbox.osc.util.SourceUtil.Callback
                            public void error(String str) {
                                Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                            }
                        });
                    }

                    @Override // com.github.tvbox.osc.ui.dialog.SourceUploadDialog.OnListener
                    public void onReset() {
                        Toast.makeText(ModelSettingFragment.this.mActivity, "重置中...", 1).show();
                        String string = HomeActivity.getRes().getString(R.string.app_source);
                        if (StringUtils.isNotEmpty(string)) {
                            SourceUtil.replaceAllSource(string, new SourceUtil.Callback<String>() {
                                /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4.AnonymousClass1.AnonymousClass3 */

                                public void success(String str) {
                                    Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                                }

                                @Override // com.github.tvbox.osc.util.SourceUtil.Callback
                                public void error(String str) {
                                    Toast.makeText(ModelSettingFragment.this.mActivity, str, 0).show();
                                }
                            });
                        } else {
                            Toast.makeText(ModelSettingFragment.this.mActivity, "无效操作", 1).show();
                        }
                    }
                });
                sourceUploadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass4.AnonymousClass2 */

                    public void onDismiss(DialogInterface dialogInterface) {
                        ((BaseActivity) ModelSettingFragment.this.mActivity).hideSystemUI(true);
                        EventBus.getDefault().unregister(dialogInterface);
                    }
                });
                sourceUploadDialog.show();
            }
        });
        findViewById(R.id.llLive).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass5 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.LIVE_HISTORY, new ArrayList());
                if (!arrayList.isEmpty()) {
                    String str = (String) Hawk.get(HawkConfig.LIVE_URL, "");
                    int i = 0;
                    if (arrayList.contains(str)) {
                        i = arrayList.indexOf(str);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ModelSettingFragment.this.getContext());
                    apiHistoryDialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_live));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass5.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            ModelSettingFragment.this.tvLive.setText(str);
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
        findViewById(R.id.llEpg).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass6 */

            public void onClick(View view) {
                ArrayList arrayList = (ArrayList) Hawk.get(HawkConfig.EPG_HISTORY, new ArrayList());
                if (!arrayList.isEmpty()) {
                    String str = (String) Hawk.get(HawkConfig.EPG_URL, "");
                    int i = 0;
                    if (arrayList.contains(str)) {
                        i = arrayList.indexOf(str);
                    }
                    final ApiHistoryDialog apiHistoryDialog = new ApiHistoryDialog(ModelSettingFragment.this.getContext());
                    apiHistoryDialog.setTip(HomeActivity.getRes().getString(R.string.dia_history_epg));
                    apiHistoryDialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass6.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter.SelectDialogInterface
                        public void click(String str) {
                            ModelSettingFragment.this.tvEpg.setText(str);
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
        findViewById(R.id.llHomeApi).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass7 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                List<SourceBean> sourceBeanList = ApiConfig.get().getSourceBeanList();
                if (sourceBeanList.size() > 0) {
                    SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                    int floor = (int) Math.floor((double) (sourceBeanList.size() / 10));
                    if (floor <= 1) {
                        floor = 1;
                    }
                    if (floor >= 3) {
                        floor = 3;
                    }
                    ((TvRecyclerView) selectDialog.findViewById(R.id.list)).setLayoutManager(new V7GridLayoutManager(selectDialog.getContext(), floor));
                    ViewGroup.LayoutParams layoutParams = ((LinearLayout) selectDialog.findViewById(R.id.cl_root)).getLayoutParams();
                    if (floor != 1) {
                        layoutParams.width = AutoSizeUtils.mm2px(selectDialog.getContext(), (float) (((floor - 1) * 260) + 400));
                    }
                    selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_source));
                    selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<SourceBean>() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass7.AnonymousClass1 */

                        public void click(SourceBean sourceBean, int i) {
                            ApiConfig.get().setSourceBean(sourceBean);
                            ModelSettingFragment.this.tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());
                        }

                        public String getDisplay(SourceBean sourceBean) {
                            return sourceBean.getName();
                        }
                    }, new DiffUtil.ItemCallback<SourceBean>() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass7.AnonymousClass2 */

                        public boolean areItemsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                            return sourceBean == sourceBean2;
                        }

                        public boolean areContentsTheSame(SourceBean sourceBean, SourceBean sourceBean2) {
                            return sourceBean.getKey().equals(sourceBean2.getKey());
                        }
                    }, sourceBeanList, sourceBeanList.indexOf(ApiConfig.get().getHomeSourceBean()));
                    selectDialog.show();
                }
            }
        });
        findViewById(R.id.llHomeShow).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass8 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.HOME_SHOW_SOURCE, Boolean.valueOf(!((Boolean) Hawk.get(HawkConfig.HOME_SHOW_SOURCE, false)).booleanValue()));
                ModelSettingFragment.this.tvHomeShow.setText(((Boolean) Hawk.get(HawkConfig.HOME_SHOW_SOURCE, true)).booleanValue() ? "开启" : "关闭");
            }
        });
        findViewById(R.id.llHomeRec).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass9 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.HOME_REC, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                arrayList.add(2);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_hm_type));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass9.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.HOME_REC, num);
                        ModelSettingFragment.this.tvHomeRec.setText(ModelSettingFragment.this.getHomeRecName(num.intValue()));
                    }

                    public String getDisplay(Integer num) {
                        return ModelSettingFragment.this.getHomeRecName(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass9.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llHomeNum).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass10 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.HOME_NUM, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                arrayList.add(2);
                arrayList.add(3);
                arrayList.add(4);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_history));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass10.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.HOME_NUM, num);
                        ModelSettingFragment.this.tvHomeNum.setText(HistoryHelper.getHomeRecName(num.intValue()));
                    }

                    public String getDisplay(Integer num) {
                        return HistoryHelper.getHomeRecName(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass10.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.showPreview).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass11 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.SHOW_PREVIEW, Boolean.valueOf(true ^ ((Boolean) Hawk.get(HawkConfig.SHOW_PREVIEW, true)).booleanValue()));
                ModelSettingFragment.this.tvShowPreviewText.setText(((Boolean) Hawk.get(HawkConfig.SHOW_PREVIEW, true)).booleanValue() ? "开启" : "关闭");
            }
        });
        findViewById(R.id.llScale).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass12 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.PLAY_SCALE, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                arrayList.add(2);
                arrayList.add(3);
                arrayList.add(4);
                arrayList.add(5);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_ratio));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass12.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.PLAY_SCALE, num);
                        ModelSettingFragment.this.tvScale.setText(PlayerHelper.getScaleName(num.intValue()));
                    }

                    public String getDisplay(Integer num) {
                        return PlayerHelper.getScaleName(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass12.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llPIP).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass13 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.PIC_IN_PIC, Boolean.valueOf(!((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, false)).booleanValue()));
                ModelSettingFragment.this.tvPIP.setText(((Boolean) Hawk.get(HawkConfig.PIC_IN_PIC, true)).booleanValue() ? "开启" : "关闭");
            }
        });
        findViewById(R.id.llPlay).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass14 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.PLAY_TYPE, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                arrayList.add(2);
                arrayList.add(10);
                arrayList.add(11);
                arrayList.add(12);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_player));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass14.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.PLAY_TYPE, num);
                        ModelSettingFragment.this.tvPlay.setText(PlayerHelper.getPlayerName(num.intValue()));
                        PlayerHelper.init();
                    }

                    public String getDisplay(Integer num) {
                        return PlayerHelper.getPlayerName(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass14.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llIjkCachePlay).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass15 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.IJK_CACHE_PLAY, Boolean.valueOf(!((Boolean) Hawk.get(HawkConfig.IJK_CACHE_PLAY, false)).booleanValue()));
                ModelSettingFragment.this.tvIjkCachePlay.setText(((Boolean) Hawk.get(HawkConfig.IJK_CACHE_PLAY, false)).booleanValue() ? "开启" : "关闭");
            }
        });
        findViewById(R.id.llMediaCodec).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass16 */

            public void onClick(View view) {
                List<IJKCode> ijkCodes = ApiConfig.get().getIjkCodes();
                if (ijkCodes != null && ijkCodes.size() != 0) {
                    FastClickCheckUtil.check(view);
                    String str = (String) Hawk.get(HawkConfig.IJK_CODEC, "");
                    int i = 0;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= ijkCodes.size()) {
                            break;
                        } else if (str.equals(ijkCodes.get(i2).getName())) {
                            i = i2;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                    selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_decode));
                    selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<IJKCode>() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass16.AnonymousClass1 */

                        public void click(IJKCode iJKCode, int i) {
                            iJKCode.selected(true);
                            ModelSettingFragment.this.tvMediaCodec.setText(iJKCode.getName());
                        }

                        public String getDisplay(IJKCode iJKCode) {
                            return iJKCode.getName();
                        }
                    }, new DiffUtil.ItemCallback<IJKCode>() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass16.AnonymousClass2 */

                        public boolean areItemsTheSame(IJKCode iJKCode, IJKCode iJKCode2) {
                            return iJKCode == iJKCode2;
                        }

                        public boolean areContentsTheSame(IJKCode iJKCode, IJKCode iJKCode2) {
                            return iJKCode.getName().equals(iJKCode2.getName());
                        }
                    }, ijkCodes, i);
                    selectDialog.show();
                }
            }
        });
        findViewById(R.id.llParseWebVew).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass17 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                boolean z = !((Boolean) Hawk.get(HawkConfig.PARSE_WEBVIEW, true)).booleanValue();
                Hawk.put(HawkConfig.PARSE_WEBVIEW, Boolean.valueOf(z));
                ModelSettingFragment.this.tvParseWebView.setText(((Boolean) Hawk.get(HawkConfig.PARSE_WEBVIEW, true)).booleanValue() ? "系统自带" : "XWalkView");
                if (!z) {
                    Toast.makeText(ModelSettingFragment.this.mContext, "注意: XWalkView只适用于部分低Android版本，Android5.0以上推荐使用系统自带", 1).show();
                    XWalkInitDialog xWalkInitDialog = new XWalkInitDialog(ModelSettingFragment.this.mContext);
                    xWalkInitDialog.setOnListener(new XWalkInitDialog.OnListener() {
                        /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass17.AnonymousClass1 */

                        @Override // com.github.tvbox.osc.ui.dialog.XWalkInitDialog.OnListener
                        public void onchange() {
                        }
                    });
                    xWalkInitDialog.show();
                }
            }
        });
        findViewById(R.id.llRender).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass18 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.PLAY_RENDER, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_render));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass18.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.PLAY_RENDER, num);
                        ModelSettingFragment.this.tvRender.setText(PlayerHelper.getRenderName(num.intValue()));
                        PlayerHelper.init();
                    }

                    public String getDisplay(Integer num) {
                        return PlayerHelper.getRenderName(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass18.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llDns).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass19 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.DOH_URL, 0)).intValue();
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_dns));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<String>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass19.AnonymousClass1 */

                    public String getDisplay(String str) {
                        return str;
                    }

                    public void click(String str, int i) {
                        ModelSettingFragment.this.tvDns.setText(OkGoHelper.dnsHttpsList.get(i));
                        Hawk.put(HawkConfig.DOH_URL, Integer.valueOf(i));
                        String dohUrl = OkGoHelper.getDohUrl(i);
                        OkGoHelper.dnsOverHttps.setUrl(dohUrl.isEmpty() ? null : HttpUrl.get(dohUrl));
                        IjkMediaPlayer.toggleDotPort(i > 0);
                    }
                }, new DiffUtil.ItemCallback<String>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass19.AnonymousClass2 */

                    public boolean areItemsTheSame(String str, String str2) {
                        return str.equals(str2);
                    }

                    public boolean areContentsTheSame(String str, String str2) {
                        return str.equals(str2);
                    }
                }, OkGoHelper.dnsHttpsList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llBackup).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass20 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                new BackupDialog(ModelSettingFragment.this.mActivity).show();
            }
        });
        findViewById(R.id.llWp).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass21 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                if (!ApiConfig.get().wallpaper.isEmpty()) {
                    Toast.makeText(ModelSettingFragment.this.mContext, ModelSettingFragment.this.getString(R.string.mn_wall_load), 0).show();
                }
                OkGo.get(ApiConfig.get().wallpaper).execute(new FileCallback(ModelSettingFragment.this.requireActivity().getFilesDir().getAbsolutePath(), "wp") {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass21.AnonymousClass1 */

                    @Override // com.lzy.okgo.callback.Callback
                    public void onSuccess(Response<File> response) {
                        ((BaseActivity) ModelSettingFragment.this.requireActivity()).changeWallpaper(true);
                    }

                    @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                    public void onError(Response<File> response) {
                        super.onError(response);
                    }

                    @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                    }
                });
            }
        });
        findViewById(R.id.llWpRecovery).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass22 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                File file = new File(ModelSettingFragment.this.requireActivity().getFilesDir().getAbsolutePath() + "/wp");
                if (file.exists()) {
                    file.delete();
                }
                ((BaseActivity) ModelSettingFragment.this.requireActivity()).changeWallpaper(true);
            }
        });
        findViewById(R.id.llHomeRecStyle).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass23 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.HOME_REC_STYLE, Boolean.valueOf(true ^ ((Boolean) Hawk.get(HawkConfig.HOME_REC_STYLE, true)).booleanValue()));
                ModelSettingFragment.this.tvRecStyleText.setText(((Boolean) Hawk.get(HawkConfig.HOME_REC_STYLE, true)).booleanValue() ? "是" : "否");
            }
        });
        findViewById(R.id.llSearchView).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass24 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get(HawkConfig.SEARCH_VIEW, 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_search));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass24.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put(HawkConfig.SEARCH_VIEW, num);
                        ModelSettingFragment.this.tvSearchView.setText(ModelSettingFragment.this.getSearchView(num.intValue()));
                    }

                    public String getDisplay(Integer num) {
                        return ModelSettingFragment.this.getSearchView(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass24.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.show();
            }
        });
        findViewById(R.id.llLocale).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass25 */
            private final int chkLang = ((Integer) Hawk.get("language", 0)).intValue();

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                int intValue = ((Integer) Hawk.get("language", 0)).intValue();
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                arrayList.add(1);
                SelectDialog selectDialog = new SelectDialog(ModelSettingFragment.this.mActivity);
                selectDialog.setTip(ModelSettingFragment.this.getString(R.string.dia_locale));
                selectDialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass25.AnonymousClass1 */

                    public void click(Integer num, int i) {
                        Hawk.put("language", num);
                        ModelSettingFragment.this.tvLocale.setText(ModelSettingFragment.this.getLocaleView(num.intValue()));
                    }

                    public String getDisplay(Integer num) {
                        return ModelSettingFragment.this.getLocaleView(num.intValue());
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass25.AnonymousClass2 */

                    public boolean areItemsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }

                    public boolean areContentsTheSame(Integer num, Integer num2) {
                        return num.intValue() == num2.intValue();
                    }
                }, arrayList, intValue);
                selectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass25.AnonymousClass3 */

                    public void onDismiss(DialogInterface dialogInterface) {
                        if (AnonymousClass25.this.chkLang != ((Integer) Hawk.get("language", 0)).intValue()) {
                            ModelSettingFragment.this.reloadActivity();
                        }
                    }
                });
                selectDialog.show();
            }
        });
        findViewById(R.id.llAbout).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass26 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                new AboutDialog(ModelSettingFragment.this.mActivity).show();
            }
        });
        findViewById(R.id.llFastSearchView).setOnClickListener(new View.OnClickListener() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass27 */

            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                Hawk.put(HawkConfig.FAST_SEARCH_MODE, Boolean.valueOf(!((Boolean) Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)).booleanValue()));
                ModelSettingFragment.this.tvFastSearchView.setText(((Boolean) Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)).booleanValue() ? "已开启" : "已关闭");
            }
        });
        SettingActivity.callback = new SettingActivity.DevModeCallback() {
            /* class com.github.tvbox.osc.ui.fragment.ModelSettingFragment.AnonymousClass28 */

            @Override // com.github.tvbox.osc.ui.activity.SettingActivity.DevModeCallback
            public void onChange() {
                ModelSettingFragment.this.findViewById(R.id.llDebug).setVisibility(0);
            }
        };
    }

    @Override // androidx.fragment.app.Fragment, com.github.tvbox.osc.base.BaseLazyFragment
    public void onDestroyView() {
        super.onDestroyView();
        SettingActivity.callback = null;
    }

    /* access modifiers changed from: package-private */
    public void reloadActivity() {
        Intent launchIntentForPackage = getActivity().getApplicationContext().getPackageManager().getLaunchIntentForPackage(getActivity().getApplication().getPackageName());
        launchIntentForPackage.addFlags(335577088);
        Bundle bundle = new Bundle();
        bundle.putBoolean("useCache", true);
        launchIntentForPackage.putExtras(bundle);
        getActivity().getApplicationContext().startActivity(launchIntentForPackage);
    }
}
