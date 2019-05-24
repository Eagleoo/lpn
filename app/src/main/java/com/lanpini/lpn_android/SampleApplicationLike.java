package com.lanpini.lpn_android;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lanpini.lpn_android.util.ActivityCollector;
import com.lzy.okgo.OkGo;
import com.mob.MobSDK;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;
import com.tencent.tinker.entry.DefaultApplicationLike;

import cn.jpush.android.api.JPushInterface;

//import com.meituan.android.walle.WalleChannelReader;

/**
 * 自定义ApplicationLike类.
 *
 * 注意：这个类是Application的代理类，以前所有在Application的实现必须要全部拷贝到这里<br/>
 *
 * @author wenjiewu
 * @since 2016/11/7
 */
public class SampleApplicationLike extends DefaultApplicationLike {

    public static final String TAG = "Tinker.SampleApplicationLike";

    private  static Context context;

    private  Application application;

    public static Context getContext() {
        return context;
    }

    public SampleApplicationLike(Application application, int tinkerFlags,
                                 boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
                                 long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime,
                applicationStartMillisTime, tinkerResultIntent);
        this.application=application;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = application.getApplicationContext();
        MobSDK.init(context);
        OkGo.getInstance().init(application);
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(context);
        // 设置是否开启热更新能力，默认为true
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = false;
        // 补丁回调接口
        Beta.betaPatchListener = new BetaPatchListener() {
            @Override
            public void onPatchReceived(String patchFile) {
                //Toast.makeText(getApplication(), "补丁下载地址" + patchFile, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
//                Toast.makeText(getApplication(),
//                        String.format(Locale.getDefault(), "%s %d%%",
//                                Beta.strNotificationDownloading,
//                                (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)),
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadSuccess(String msg) {
                //Toast.makeText(getApplication(), "补丁下载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFailure(String msg) {
                //Toast.makeText(getApplication(), "补丁下载失败", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onApplySuccess(String msg) {
                //Toast.makeText(getApplication(), "补丁应用成功", Toast.LENGTH_SHORT).show();

                //showPingDialog();
            }

            @Override
            public void onApplyFailure(String msg) {
                //Toast.makeText(getApplication(), "补丁应用失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPatchRollback() {

            }
        };

        // 设置开发设备，默认为false，上传补丁如果下发范围指定为“开发设备”，需要调用此接口来标识开发设备
        //Bugly.setIsDevelopmentDevice(getApplication(), true);
        // 多渠道需求塞入
        // String channel = WalleChannelReader.getChannel(getApplication());
        // Bugly.setAppChannel(getApplication(), channel);
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        Bugly.init(getApplication(), "8108706ed2", false);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // TODO: 安装tinker
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(
            Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Beta.unInit();
    }

    private void showPingDialog() {
        // TODO Auto-generated method stub
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_tinker);
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.dialog_layout);
        TextView bodyTv = (TextView) dialog.findViewById(R.id.dialog_body_tv);
        //bodyTv.setText(body);
        TextView leftTv = (TextView) dialog.findViewById(R.id.dialog_yiban_tv);
        //leftTv.setText(left);
        TextView rightTv = (TextView) dialog.findViewById(R.id.dialog_chiyu_tv);
        //rightTv.setText(right);
        Log.e("ooooooooooo","ooooooooooo");
        Toast.makeText(getApplication(), "·······弹窗来了········", Toast.LENGTH_SHORT).show();

        leftTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();


            }
        });
        rightTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                ActivityCollector.finishAll();
                System.exit(0);

            }

        });


        dialog.show();

    }
}
