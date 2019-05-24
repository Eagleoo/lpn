package com.lanpini.lpn_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lanpini.lpn_android.PayUtils.PayInfo;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by Administrator on 2018/5/11.
 */

public class AppRegister extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI api = WXAPIFactory.createWXAPI(context, null);


        api.registerApp(PayInfo.WI_APP_ID);
    }
}
