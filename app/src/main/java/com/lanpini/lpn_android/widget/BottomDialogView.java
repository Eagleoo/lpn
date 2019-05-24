package com.lanpini.lpn_android.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.jungly.gridpasswordview.GridPasswordView;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.activity.MainActivity;
import com.lanpini.lpn_android.HttpUtil.HttpManger;
import com.lanpini.lpn_android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;

public class BottomDialogView extends PopupWindow {
    private View dialogView;
    private GridPasswordView payPassEt;
    private Button confirmBtn;
    private ImageView backDialogIv;
    private HashMap<String, String> params1;
    public BottomDialogView(final Activity context, HashMap<String, String> params,final BottomDialogOnclickListener bottomDialogOnclickListener) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = inflater.inflate(R.layout.dialog, null);
        backDialogIv = (ImageView) dialogView.findViewById(R.id.backDialogIv);
        payPassEt = (GridPasswordView) dialogView.findViewById(R.id.payPassEt);
        confirmBtn = (Button) dialogView.findViewById(R.id.confirmBtn);
        backDialogIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        confirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        params1=params;

        this.setContentView(dialogView);
        this.setWidth(LayoutParams.MATCH_PARENT);
        this.setHeight(LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.DialogShowStyle); //设置SelectPicPopupWindow弹出窗体动画效果
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);

        onPwdChangedTest(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(600);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });

            }
        }).start();


    }

    // Test GridPasswordView.clearPassword() in OnPasswordChangedListener.
    // Need enter the password twice and then check the password , like Alipay
    void onPwdChangedTest(final Context context){
        payPassEt.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                if (psw.length()==6){
                    HashMap<String, String> params = new HashMap<>();
                    params.put("toUser",  params1.get("toUser"));
                    params.put("imgyzcode", params1.get("imgyzcode"));
                    params.put("moneyTypeid",  params1.get("moneyTypeid"));
                    params.put("num",  params1.get("num"));
                    params.put("token", params1.get("token"));
                    params.put("spassword", psw);
                    params.put("yzcode", params1.get("yzcode"));
                    HttpManger.postRequest(context, "/tools/submit_api.ashx?action=moneyTransfer", params, "请稍后...", new HttpManger.DoRequetCallBack() {

                        @Override
                        public void onSuccess(String o) {

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(o);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String msg = jsonObject.optString("msg") + "";
                            Util.show(context,msg);
                            if (msg.equals("转账成功！")){
                                Intent intent=new Intent();
                                intent.putExtra("url","/userusdt.aspx?action=yw_list2");
                                intent.setClass(context,MainActivity.class);
                                context.startActivity(intent);
                            }
                            Log.e("请求结果", "**********str**********" + o);
                            dismiss();
                        }

                        @Override
                        public void onError(String o) {
                            Log.e("请求结果", "erro" + o);
                        }
                    });
                }
            }
        });
    }

}