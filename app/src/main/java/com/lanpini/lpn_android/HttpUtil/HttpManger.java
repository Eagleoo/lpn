package com.lanpini.lpn_android.HttpUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.lanpini.lpn_android.widget.ShapeLoadingDialog;
import com.lanpini.lpn_android.Bean.BaseBean;
import com.lanpini.lpn_android.Bean.UserInfoBean;
import com.lanpini.lpn_android.util.EncryptionUtil;
import com.lanpini.lpn_android.util.UrlUtil;
import com.lanpini.lpn_android.util.Web;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.Date;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HttpManger {

    public interface DoRequetCallBack {
        void onSuccess(String t);

        void onError(String t);
    }

    /**
     * @param context
     * @param uri
     * @param params           请求的参数
     * @param message
     * @param doRequetCallBack
     */

    public static void postRequest(final Context context, final String uri, Map<String, String> params, String message, final DoRequetCallBack doRequetCallBack) {
        final ShapeLoadingDialog shapeLoadingDialog = new ShapeLoadingDialog.Builder(context)
                .loadText(message)
                .build();
        shapeLoadingDialog.show();
        String timestamps = Integer.valueOf(String.valueOf(new Date().getTime() / 1000)) + "";

        UserInfoBean userInfoBean = UserInfoBean.getUser();
        if (userInfoBean != null) {
            params.put("token", userInfoBean.getToken());
        }

            OkGo.<String>post(Web.url + uri).tag(context)
                    .headers("X-Auth-Sign", EncryptionUtil.getSign(params, uri, timestamps))
                    .headers("X-Auth-Key", EncryptionUtil.KEY)
                    .headers("X-Auth-TimeStamp", timestamps)
                    .headers("timestamp", timestamps)
                    .headers("method", "POST")
                    .headers("uri", UrlUtil.getURLEncoderString(uri))
                    .headers("contentlength", EncryptionUtil.getRequestCountLength(params) + "")
                    .headers("key", EncryptionUtil.KEY)
                    .params(params)
                    .execute(new StringCallback() {
                        @SuppressLint("CheckResult")
                        @Override
                        public void onSuccess(Response<String> response) {
                            Observable.just(response).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Response<String>>() {
                                        @Override
                                        public void accept(Response<String> stringResponse) throws Exception {
                                            Gson gson = new Gson();
                                            Log.e("数据", "stringResponse:" + stringResponse.body());
                                            BaseBean baseBean = gson.fromJson(stringResponse.body(), BaseBean.class);

                                            shapeLoadingDialog.dismiss();

                                            doRequetCallBack.onSuccess(stringResponse.body());
                                        }
                                    });


                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            shapeLoadingDialog.dismiss();
                            doRequetCallBack.onError(response.body());

                        }
                    });
        Log.e("请求地址：",Web.url+uri+params);
    }
}
