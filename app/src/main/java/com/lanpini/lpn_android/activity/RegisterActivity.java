package com.lanpini.lpn_android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lanpini.lpn_android.Bean.UserInfoBean;
import com.lanpini.lpn_android.HttpUtil.HttpManger;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.util.Util;
import com.yanzhenjie.sofia.Sofia;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.username)
    EditText eUsername;
    @BindView(R.id.password)
    EditText ePassword;
    @BindView(R.id.email)
    EditText email;

    @BindView(R.id.back)
    ImageView back;

    @BindView(R.id.portrait)
    ImageView portrait;
    @BindView(R.id.type)
    TextView type;
    @BindView(R.id.submit)
    Button submit;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ButterKnife.bind(this);

        Sofia.with(this)
                .statusBarBackground(ContextCompat.getColor(this, R.color.colorAccent));
        intent=getIntent();

        Glide.with(context).load(intent.getStringExtra("avatar")).placeholder(R.drawable.logo).into(portrait);
    }

    @OnClick({R.id.back,R.id.submit})
    public void click(View view) {
        switch (view.getId()) {

            case R.id.back:
                finish();
                break;
            case R.id.submit:
                if (!Util.isMobileNO(eUsername.getText().toString())){
                    Util.show(context,"请检查手机号是否输入正确！");
                    return;
                }
                if (ePassword.getText().length()<6){
                    Util.show(context,"密码最少6位！");
                    return;
                }
                if (!Util.isEmail(email.getText().toString())){
                    Util.show(context,"请检查邮箱是否输入正确！");
                    return;
                }

                register();
                break;
        }

    }

    private void register(){
        HashMap<String, String> params = new HashMap<>();
        params.put("nick", intent.getStringExtra("nick"));
        params.put("avatar", intent.getStringExtra("avatar"));
        params.put("sex", intent.getStringExtra("sex"));
        params.put("birthday", "1995-10-01");
        params.put("oauth_name", intent.getStringExtra("oauth_name"));
        params.put("oauth_access_token", intent.getStringExtra("oauth_access_token"));
        params.put("oauth_openid", intent.getStringExtra("oauth_openid"));
        params.put("txtPassword", ePassword.getText().toString());
        params.put("txtEmail", email.getText().toString());
        params.put("txtMobile", eUsername.getText().toString());
        params.put("site_id", "1");

        HttpManger.postRequest(context,"/tools/submit_api.ashx?action=user_oauth_register", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(o);
                    String status = jsonObject.optString("status") + "";

                    if (status.equals("1")){
                        Gson gson = new Gson();
                        UserInfoBean userInfoBean = gson.fromJson(o, UserInfoBean.class);
                        UserInfoBean.setUser(userInfoBean);
//                Log.e("是否唯恐", "asa" + (UserInfoBean.getUser() == null));
                        if (userInfoBean != null) {
//                    Log.e("请求结果 status", "status" + userInfoBean.getStatus());
                            if (userInfoBean.getStatus() == 1) {
//                        Log.e("请求结果 status1", "status 開始跳轉" + Util.isMainThread());
//                        Util.show(context, "開始跳轉2" + Util.getCurProcessName(context));
//                        Log.e("请求结果 status3", "status 開始跳轉" + Util.getCurProcessName(context));

                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("login", "yes");
                                startActivity(intent);
                                SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("user_id", 1);
                                editor.putString("username",eUsername.getText().toString());
                                editor.putString("password",ePassword.getText().toString());
                                editor.putString("token",userInfoBean.getToken());
                                editor.putString("path","");
                                if (userInfoBean.getModel().getAvatar().equals("")){
                                    editor.putString("portrait","default");
                                }
                                else {
                                    editor.putString("portrait",userInfoBean.getModel().getAvatar());
                                }
                                editor.commit();
//                        if (forgetGesture!=null){
//                            startActivity(new Intent(context,CreateGestureActivity.class));
//                        }
//                        String gesturePassword = aCache.getAsString(Constant.GESTURE_PASSWORD);
//                        if(gesturePassword == null || "".equals(gesturePassword)) {
//                            Intent intent1 = new Intent(context, CreateGestureActivity.class);
//                            intent1.putExtra("ignore","ignore");
//                            startActivity(intent1);
//                        }


                                Util.showIntent(context,MainActivity.class);
                                JPushInterface.setAlias(context, 200, eUsername.getText().toString());

                                finish();
                            }

                            Util.show(context, userInfoBean.getMsg());

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("请求结果", "str" + o);
            }

            @Override
            public void onError(String o) {
                Log.e("请求结果", o);
            }
        });

    }

}
