package com.lanpini.lpn_android.activity;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lanpini.lpn_android.Bean.LoginInfoBean;
import com.lanpini.lpn_android.Bean.UserInfoBean;
import com.lanpini.lpn_android.DataUtil.SaveGetSharedPreferencesInfo;
import com.lanpini.lpn_android.HttpUtil.HttpManger;
import com.lanpini.lpn_android.MyTextWatcher;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.util.ACache;
import com.lanpini.lpn_android.util.ActivityCollector;
import com.lanpini.lpn_android.util.MD5;
import com.lanpini.lpn_android.util.SelectorFactory;
import com.lanpini.lpn_android.util.Util;
import com.lanpini.lpn_android.util.Web;
import com.lanpini.lpn_android.widget.UpDataVersionDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mob.tools.utils.UIHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

public class LoginActivity extends BaseActivity implements PlatformActionListener, Handler.Callback {

    @BindView(R.id.username)
    EditText eUsername;
    @BindView(R.id.password)
    EditText ePassword;
    @BindView(R.id.login)
    TextView login;
    @BindView(R.id.phoneline)
    View phoneline;
    @BindView(R.id.passwordline)
    View passwordline;
    @BindView(R.id.back1)
    ImageView back;
    @BindView(R.id.toRegistered)
    View toRegistered;
    @BindView(R.id.toForgetPassword)
    View toForgetPassword;
    String version;
    String content;
    String url;
    String createTime;
    String forgetGesture;
    String type;
    private ACache aCache;
    String userId;
    String userName;
    String userIcon;
    String token;
    String tokenSecret;
    String userGender;

    @Override
    public boolean isWidth() {
        return false;
    }


    MyTextWatcher myTextWatcher = new MyTextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            super.afterTextChanged(editable);
            login.setEnabled(false);
            Pattern num= Pattern.compile("[0-9]");
            Pattern english= Pattern.compile("[a-zA-Z]");
            Pattern chinese= Pattern.compile("[\u4e00-\u9fa5]");
            StringBuffer stringBuffer=new StringBuffer();
            stringBuffer.append(editable);
            for(int i=0;i<stringBuffer.length();i++){
                char answer = stringBuffer.charAt(i);
                if(!chinese.matcher(String.valueOf(answer)).matches()&&!english.matcher(String.valueOf(answer)).matches()
                        &&!num.matcher(String.valueOf(answer)).matches()&&!String.valueOf(answer).equals("_")&&!String.valueOf(answer).equals("@")
                        &&!String.valueOf(answer).equals(".")){
                   Util.show(context,"输入有误，只能为汉字、数字、字母和指定符号！");
                }
            }
            if (!TextUtils.isEmpty(eUsername.getText().toString()) && !TextUtils.isEmpty(ePassword.getText().toString())) {
                login.setEnabled(true);
            }
        }
    };

    private boolean isNotificationEnabled(Context context) {
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void requestNotification(BaseActivity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", activity.getPackageName());
            intent.putExtra("app_uid", activity.getApplicationInfo().uid);
            activity.startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
            activity.startActivity(intent);
            return;
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        ButterKnife.bind(this);
        aCache = ACache.get(this);
        forgetGesture=getIntent().getStringExtra("forgetGesture");
        //NotificationManager: notify: id corrupted: sent 1616487783, got back 0
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.e("通知栏", "areNotificationsEnabled" + isNotificationEnabled(context));

        }

        if (!areNotificationsEnabled) {
            requestNotification(this, 200);
        }


        LoginInfoBean loginInfoBean = SaveGetSharedPreferencesInfo.getLoginInfo(context);
        if (loginInfoBean != null) {
            eUsername.setText(loginInfoBean.getUserid());
            ePassword.setText(loginInfoBean.getPassword());
        }

        login.setEnabled(false);

        eUsername.addTextChangedListener(myTextWatcher);
        ePassword.addTextChangedListener(myTextWatcher);

        toRegistered.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#f9f6f5"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setCornerRadius(Util.dpToPx(context, 2), 0, 0, Util.dpToPx(context, 2))
                .create(context));
        toForgetPassword.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultStrokeColor(Color.parseColor("#faf0e1"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setDefaultBgColor(Color.parseColor("#00ffffff"))
                .setPressedBgColor(Color.parseColor("#edcb97"))
                .setCornerRadius(0, Util.dpToPx(context, 2), Util.dpToPx(context, 2), 0)
                .create(context));

        if (!sp.getString("username","").equals("")){
            eUsername.setText(sp.getString("username",""));
        }

        checkVersion();
    }


    @OnClick({R.id.login, R.id.toRegistered, R.id.toForgetPassword,R.id.back1,R.id.wx_login,R.id.qq_login})
    public void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.login:
                login(false);
                break;
            case R.id.wx_login:
                authorize(new Wechat());
                type="微信";
                break;
            case R.id.qq_login:
                authorize(new QQ());
                type="QQ";
                break;
            case R.id.back1:
                if (getIntent().hasExtra("history_url")){
                    intent = new Intent(context, MainActivity.class);
                    intent.putExtra("url", getIntent().getStringExtra("history_url"));
                    startActivity(intent);
                }
                else {
                    Util.showIntent(context,MainActivity.class);
                }

                finish();
                break;
            case R.id.toRegistered:
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("url", "/register.aspx");
                startActivity(intent);
                break;
            case R.id.toForgetPassword:
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("url", "/repassword.aspx");
                intent.putExtra("forget", "forget");
                startActivity(intent);
                break;
        }

    }

    public void login(boolean isYD) {
        final String lName = eUsername.getText().toString();
        final String lpwd = ePassword.getText().toString();
        if (Util.isNull(lName)) {
            Util.show(context, "请输入用户名");
            return;
        } else if (Util.isNull(lpwd)) {
            Util.show(context, "请输入密码");
            return;
        } else if ("".equals(lpwd.trim())) {
            Util.show(context, "空格不能作为登录密码");
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("txtUserName", lName);
        params.put("txtPassword", isYD ? new MD5().getMD5ofStr(lpwd) : lpwd);
        params.put("site_id", "1");
        String mAction = "user_login";
        //user_login
        HttpManger.postRequest(context, "/tools/submit_api.ashx?action=" + mAction, params, "登录中", new HttpManger.DoRequetCallBack() {
//        HttpManger.postRequest(context, "/asp/login.asp", params, "登录中", new HttpManger.DoRequetCallBack() {


            @Override
            public void onSuccess(String o) {
                Log.e("请求结果", "str" + o);

//                if (true) {
//                    Gson gson = new Gson();
//                    TestBean testBean = gson.fromJson(o, TestBean.class);
//                    HashMap<String, String> params = new HashMap<>();
//                    params.put("site_id1", "1");
//                    params.put("site_id2", "2");
//                    params.put("token", testBean.getMsg().getToken());
//                    HttpManger.postRequest(context, "/asp/test.asp", params, "测试中", new HttpManger.DoRequetCallBack() {
//                        @Override
//                        public void onSuccess(String t) {
//
//                        }
//
//                        @Override
//                        public void onError(String t) {
//
//                        }
//                    });
//                    return;
//                }

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
                        editor.putString("username",lName);
                        editor.putString("password",lpwd);
                        editor.putString("token",userInfoBean.getToken());
                        editor.putString("path","");
                        if (userInfoBean.getModel().getAvatar().equals("")){
                            editor.putString("portrait","default");
                        }
                        else {
                            editor.putString("portrait",userInfoBean.getModel().getAvatar());
                        }
                        editor.commit();
                        Util.islock=true;
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
                        JPushInterface.setAlias(LoginActivity.this, 200, lName);

                        finish();
                    }

                    Util.show(context, userInfoBean.getMsg());

                }


            }

            @Override
            public void onError(String o) {

            }
        });


    }

    private void checkVersion() {
        OkGo.<String>get(Web.url + Web.version).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                version = response.body().split("<Version>")[1].split("</Version>")[0].trim();
                content = response.body().split("<Content>")[1].split("</Content>")[0].trim();
                url = response.body().split("<Url>")[1].split("</Url>")[0].trim();
                createTime = response.body().split("<CreateTime>")[1].split("</CreateTime>")[0].substring(0, 10).trim();
                String str1=Util.getString(Util.getString(version,"."),".");//服务器版本
                String str2=Util.getString(Util.getString(Util.getVerName(context),"."),".");//当前版本
                if (Integer.valueOf(str1)>Integer.valueOf(str2)) {
                    new UpDataVersionDialog(context, content, url).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(context).setTitle(R.string.careful).setMessage(R.string.out).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCollector.finishAll();
                    System.exit(0);
                }
            }).setNegativeButton(R.string.cancel, null).setIcon(R.drawable.logo).show();}
        return super.onKeyDown(keyCode, event);
    }

    private void authorize(Platform plat) {

        //ApplyCpd = CustomProgressDialog.showProgressDialog(this, "申请授权中...");
        plat.setPlatformActionListener(this);
        plat.removeAccount(true);
        if (plat instanceof QZone) {
            plat.SSOSetting(false);
        } else
            plat.SSOSetting(false);
        plat.showUser(null);
    }
    private static final int MSG_ACTION_CCALLBACK = 0;
    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 1;
        msg.arg2 = i;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);

    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 2;
        msg.arg2 = i;
        msg.obj = throwable;
        UIHandler.sendMessage(msg, this);
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 3;
        msg.arg2 = i;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.arg1) {
            case 1: { // 成功
                Toast.makeText(LoginActivity.this, "授权登陆成功", Toast.LENGTH_SHORT).show();

                //获取用户资料
                Platform platform = (Platform) msg.obj;
                userId = platform.getDb().getUserId();//获取用户账号
                userName = platform.getDb().getUserName();//获取用户名字
                userIcon = platform.getDb().getUserIcon();//获取用户头像
                token = platform.getDb().getToken();//获取用户头像
                tokenSecret = platform.getDb().getTokenSecret();//获取用户头像
                userGender = platform.getDb().getUserGender(); //获取用户性别，m = 男, f = 女，如果微信没有设置性别,默认返回null

                Log.e("微信信息：","账号："+userId+"名字："+userName+"头像："+userIcon+"token："+token+"secret："+tokenSecret+"性别："+userGender);

                IsRegister();

            }
            break;
            case 2: { // 失败
                Toast.makeText(LoginActivity.this, "授权登陆失败", Toast.LENGTH_SHORT).show();
            }
            break;
            case 3:
                { // 取消
                Toast.makeText(LoginActivity.this, "授权登陆取消", Toast.LENGTH_SHORT).show();
            }
            break;
        }
        return false;
    }

    private void IsRegister(){
        HashMap<String, String> params = new HashMap<>();
        params.put("oauth_name", type);
        params.put("oauth_openid", userId);
        params.put("site_id", "1");

        HttpManger.postRequest(context,"/tools/submit_api.ashx?action=user_oauth_Is_registered", params, "请稍后...", new HttpManger.DoRequetCallBack() {

            @Override
            public void onSuccess(String o) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(o);
                    String status = jsonObject.optString("status") + "";

                    if (status.equals("0")){
                        Util.showIntent(context,RegisterActivity.class,new String[]{"nick","avatar","sex","oauth_name","oauth_access_token","oauth_openid"},
                                new String[]{userName,userIcon,userGender,type,token,userId});
                    }else {
                        register();
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

    private void register(){
        HashMap<String, String> params = new HashMap<>();
        params.put("oauth_name", type);
        params.put("oauth_openid", userId);
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
