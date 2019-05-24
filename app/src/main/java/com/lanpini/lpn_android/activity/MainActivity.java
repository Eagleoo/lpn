package com.lanpini.lpn_android.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lanpini.lpn_android.Bean.UserInfoBean;
import com.lanpini.lpn_android.ConfigurationInfor;
import com.lanpini.lpn_android.MyChromeClient;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.WebViewJsInterface;
import com.lanpini.lpn_android.util.ActivityCollector;
import com.lanpini.lpn_android.util.CommonUtil;
import com.lanpini.lpn_android.util.Util;
import com.lanpini.lpn_android.util.Web;
import com.lanpini.lpn_android.widget.ShapeLoadingDialog;
import com.lanpini.lpn_android.zxing.activity.CaptureActivity;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yanzhenjie.sofia.Sofia;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.sharesdk.onekeyshare.OnekeyShare;
import io.supercharge.shimmerlayout.ShimmerLayout;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;

public class MainActivity extends BaseActivity implements WebViewJsInterface.StrCallBack {

    @BindView(R.id.center)
    TextView center;
    @BindView(R.id.linearTittle)
    LinearLayout linearTittle;
    @BindView(R.id.linearAll)
    LinearLayout linearAll;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.shimmer_layout)
    ShimmerLayout shimmerLayout;
    @BindView(R.id.header)
    BezierRadarHeader mRefreshHeader;
    @BindView(R.id.refreshLayout)
    RefreshLayout mRefreshLayout;
    @BindView(R.id.nowifi)
    View nowifi;
    private ShapeLoadingDialog shapeLoadingDialog;
    private int state = 0; // 0登录 1 会员中心 2 商品
    private String shareurl = "";
    String url = "",scanResult="";
    SharedPreferences sp;
    private HomeWatcherReceiver mHomeWatcherReceiver = null;
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final int RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP = 1;
    private final int RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP = 2;
    private int REQUEST_CODE = 0x01;

    private void setNowifi(boolean isnowifi) {
        if (isnowifi) {
            nowifi.setVisibility(View.VISIBLE);
        } else {
            nowifi.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().hasExtra("dealWith")) {
            int dealwithid = getIntent().getIntExtra("dealWith", -1);
//            Log.e("checkEquipment", getIntent().getIntExtra("dealWith", -1) + "");
            if (dealwithid == 1) {
                webView.loadUrl(Web.url + "/userbussions.aspx?action=list");
            } else if (dealwithid == 2) {
                webView.loadUrl(Web.url + "/userbussions_aplay.aspx?action=list");
            } else if (dealwithid == 3) {
                webView.loadUrl(Web.url + "/usercenter.aspx?action=index");
            } else if (dealwithid == 4) {
                webView.loadUrl(Web.url + "/useramount.aspx?action=recharge");
            } else if (dealwithid == 5) {
                webView.loadUrl(Web.url + "/useramount.aspx?action=silver");
            }

        }else if(getIntent().hasExtra("login")){
            String str= getIntent().getStringExtra("login");
            if(str.equals("yes")){
                StringBuilder builder1 = new StringBuilder();
                builder1.append("token=").append(UserInfoBean.getUser().getToken());
                String postData = builder1.toString();
                webView.postUrl(Web.url + Web.autologin, EncodingUtils.getBytes(postData, "UTF-8"));
                webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");
            }else if(str.equals("back")){
                webView.loadUrl(Web.url);
            }
            else if (str.equals("false")){
                webView.loadUrl(Web.url);
            }
        }
        else if (getIntent().hasExtra("url")){
            String str= getIntent().getStringExtra("url");
            webView.loadUrl(Web.url+str);
        }else if (getIntent().hasExtra("loginout")){
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("token");
            editor.commit();
            webView.loadUrl(Web.url+"/usercenter.aspx?action=exit");
        }
        else if (getIntent().hasExtra("about")){
            webView.loadUrl(Web.url+"/content.aspx?id=70");
        }
        else if (getIntent().hasExtra("notify_url")){
            String str= getIntent().getStringExtra("notify_url");
            webView.loadUrl(str);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        if (getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
        }
        initI18();
        registerReceiver();
        setView();
        setRefresh();
        Req_Permisson();
    }

    boolean errorurl = false;
    private void setAnyBarAlpha(int alpha) {

        Sofia.with(this)
                .statusBarBackgroundAlpha(alpha);
    }
    public void setRefresh() {
        mRefreshHeader.setEnableHorizontalDrag(true);
        mRefreshLayout.setEnableHeaderTranslationContent(true);
        setThemeColor(R.color.colorAccent, R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

                Util.FlipAnimatorXViewShow(logo,logo,1000);
                if (state==1){//第一次进入自动刷新
                    if(sp.getString("show_alert","1").equals("0")){
                        //地址栏传参告诉web是否提示切换语言  0不提示  1提示
                        webView.loadUrl(Web.url+"?index="+sp.getString("show_alert","1"));
                    }
                    else {

                        webView.loadUrl(Web.url+"?index="+sp.getString("show_alert","1"));//修改字体
                    }
                   }
                else {
                    webView.reload();
                }


            }
        });
    }
    public void setView() {
        Sofia.with(this)
                .statusBarBackground(ContextCompat.getColor(this, R.color.colorAccent))
                .invasionStatusBar();

        setAnyBarAlpha(0);
        shapeLoadingDialog = new ShapeLoadingDialog.Builder(context)
                .loadText(R.string.load)
                .build();
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();
        if (Util.isNetworkAvalible(context)){
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        }
        else {
            //只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webSettings.setUserAgentString(ConfigurationInfor.UserAgent);//设置代理
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        //开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);
        webView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        webView.setBackgroundResource(R.color.black80);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Log.e("开启硬件加速","开启硬件加速");//安卓5.0以前开启会显示出错
        }
        webView.setWebChromeClient(new MyChromeClient(this));
        //如果不设置WebViewClient，请求会跳转系统浏览器
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("错误", "onReceivedError" + view.getUrl());
                //单独处理拍照时特殊情况
                if (view.getUrl().equals(Web.url + "/usercenter.aspx?action=index")&& Util.isNetworkAvalible(MainActivity.this)){
                    webView.loadUrl(Web.url+"/usercenter.aspx?action=index");
                }
                else {
                    errorurl = true;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                shareurl = url;
                Log.e("现在加载的页面", "url" + url);
                //退出登录
//                if (url.equals(Web.url + "/usercenter.aspx?action=index")&&sp.getString("token","").equals("")){
//                    webView.loadUrl(Web.url+"/usercenter.aspx?action=exit");
//                }
                //未登录去登录页面
                if (url.contains("login.aspx")) {
//                    Intent intent=new Intent(context,LoginActivity.class);
//                    intent.putExtra("history_url",myLastUrl());
//                    startActivity(intent);
                    Util.showIntent(context,LoginActivity.class);
                    Util.setIsLogin(false);
                    state = 0;
                    finish();
                    return true;
                }
                if (url.startsWith("weixin://wap/pay?") || url.startsWith("alipays://platformapi/startApp?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(context, "暂未安装相关APP", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                finishLoad(6000,1000,webView,shimmerLayout,null);
                if (state!=1){
                    shapeLoadingDialog.show();
                }
                else {
                    state=0;
                }
                //logo在转的时候会显示back
                if (logo.getVisibility()==View.VISIBLE){
                    back.setVisibility(View.GONE);
                }
                else {
                    back.setVisibility(View.VISIBLE);
                }
                if (url.equals(Web.url+"/?index="+sp.getString("show_alert","1"))||url.equals(Web.url+"/donations_goods_support.aspx?page=zanzhu_product")
                        ||url.equals(Web.url+"/")){
                    center.setVisibility(View.GONE);
                    linearAll.setVisibility(View.VISIBLE);
                    linearTittle.setVisibility(View.VISIBLE);
                }
                else if(url.equals(Web.url+"/donations_goods_list.aspx")||(url.equals(Web.url+"/subscription_goods_list.aspx"))
                        ||(url.equals(Web.url+"/usercenter.aspx?action=index"))){
                    linearAll.setVisibility(View.VISIBLE);
                    linearTittle.setVisibility(View.GONE);
                    center.setVisibility(View.VISIBLE);
                }
                else {
                    linearAll.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                    center.setVisibility(View.VISIBLE);
                }



            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                webView.setVisibility(View.VISIBLE);
                mRefreshLayout.finishRefresh();
                shimmerLayout.stopShimmerAnimation();
                shimmerLayout.setVisibility(View.GONE);
                Log.e("errorurl", "errorurl:" + errorurl);

                if (view.getTitle().contains(Web.url)) {
                    center.setText(R.string.app_name);
                } else {
                    center.setText(Util.formatTitleStr(view.getTitle()));
                }

                if (view.getTitle().contains("404")||view.getTitle().length()>25){
                    errorurl=true;
                    center.setText(R.string.error);
                }

                if (errorurl) {
                    setNowifi(true);
                    errorurl = false;
                } else {
                    setNowifi(false);
                }


                if (shapeLoadingDialog!=null){
                    shapeLoadingDialog.dismiss();
                }



                //Android端调用js的代码一定要在onPageFinished方法之后
                if (getIntent().hasExtra("font")){
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getIntent().getStringExtra("font").equals("t")){
                                webView.loadUrl("javascript:zh_tran('t')");

                            }
                            else {
                                webView.loadUrl("javascript:zh_tran('s')");

                            }

                        }
                    });
                }

                //用户登录过后就不执行了js方法了
                if (sp.getString("token","").equals("")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript("javascript:getCookie('zh_choose')", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.e("*******value******",value);
                                Resources resources = getResources();
                                DisplayMetrics dm = resources.getDisplayMetrics();
                                Configuration config = resources.getConfiguration();
                                SharedPreferences.Editor editor = sp.edit();
                                if (value.equals("\"t\"")){
                                    config.locale = Locale.TAIWAN;
                                    editor.putString("font","t");
                                }
                                else {
                                    config.locale = Locale.CHINESE;
                                    editor.putString("font","s");

                                }
                                resources.updateConfiguration(config, dm);
                                editor.commit();
                            }
                        });
                    }

                    else {
                        new AlertDialog.Builder(context).setTitle("提示：")
                                .setMessage("手机系统过低，无法使用字体转换功能")
                                .setPositiveButton("确定",null).show();
                    }

                }

            }
        });
        final Intent intent = getIntent();

        //验证登录
        if (intent.hasExtra("login")) {
            StringBuilder builder1 = new StringBuilder();
            builder1.append("token=").append(UserInfoBean.getUser().getToken());
            String postData = builder1.toString();
            webView.postUrl(Web.url + Web.autologin, EncodingUtils.getBytes(postData, "UTF-8"));
            webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");
        } else {
            if (!url.equals("")) {
                webView.loadUrl(Web.url + url);
            } else {
                if (intent.hasExtra("firstIn")){
                    state=1;
                    mRefreshLayout.autoRefresh();//第一次进入触发自动刷新
                    shimmerLayout.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    shimmerLayout.startShimmerAnimation();

                }

                else {
                    webView.loadUrl(Web.url);
                }

            }
            webView.addJavascriptInterface(new WebViewJsInterface(this), "webviewcall");

        }

    }

    @OnClick({R.id.back, R.id.refresh, R.id.share_iv, R.id.nowifi, R.id.logo})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.back:

                Log.e("加载地址", "地址:" + webView.getUrl());
                if (webView.getUrl().equals(Web.url+"/register.aspx")||webView.getUrl().equals(Web.url+"/repassword.aspx")){
                    finish();
                }
                else {
                    if (webView.getUrl().contains("usercenter.aspx?action=password") || webView.getUrl().contains("usercenter.aspx?action=pay_password")) {
                        webView.loadUrl(Web.url + "/usercenter.aspx?action=index");
                    } else if (webView.getUrl().contains("userbussions.aspx?action=yw_list&page=")) {
                        int page = Integer.parseInt(Util.getValueByName(webView.getUrl(), "page"));
                        if (page >= 2) {
                            webView.loadUrl(Web.url + "/userbussions.aspx?action=list");
                        }
                    } else if (webView.getUrl().contains("/userbussions_aplay.aspx?action=list&back=zc")) {
                        webView.loadUrl(Web.url + "/asset.aspx");
                    } else if (webView.getUrl().contains(Web.url + "userjiguang_show.aspx?id")) {
                        webView.loadUrl(Web.url + "/userjiguang.aspx");
                    } else if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        alert();
                    }
                }
                break;
            case R.id.refresh:
                Util.clearAllCache(context);
                webView.reload();
                break;

            case R.id.nowifi:
                webView.reload();
                break;
            case R.id.share_iv:
                showShare("请下载安装蓝毗尼App", "请下载安装蓝毗尼App");
                break;
            case R.id.logo:
                back.setVisibility(View.GONE);
                Util.FlipAnimatorXViewShow(logo,logo,1000);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            }else if (webView.getUrl().equals(Web.url+"/register.aspx")||webView.getUrl().equals(Web.url+"/repassword.aspx")){
               finish();
            }
            else {
                alert();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void alert() {
        new AlertDialog.Builder(context).setTitle(R.string.careful).setMessage(R.string.out).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCollector.finishAll();
                System.exit(0);
            }
        }).setNegativeButton(R.string.cancel, null).setIcon(R.drawable.logo).show();
    }

    private void showShare(String sharetext, String commentText) {
        Random r=new Random();
        int i = r.nextInt(10);//0-9随机数
        OnekeyShare oks = new OnekeyShare();
        oks.setTitle(sharetext);
        oks.setTitleUrl(shareurl);
        oks.setUrl(shareurl);
        //oks.setImagePath(Environment.getExternalStorageDirectory()+"/LOGO.png");
        oks.setImageUrl("http://m.lpn.dnsrw.com/templates/mobile/images/logo.png");
//        oks.setAddress("10086");
        oks.setComment(commentText);
        oks.setText(commentText);
        oks.setSite(shareurl);
        oks.setSilent(false);
        oks.setSiteUrl(shareurl);
        oks.show(this);
    }

    boolean isRefresh = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isRefresh) {
            Log.e("重新刷新", "isRefresh:" + isRefresh);
            webView.reload();
            isRefresh = false;
        }
//        if (!sp.getString("token","").equals("")&&!Util.islock){
//            gesturePassword = aCache.getAsString(Constant.GESTURE_PASSWORD);
//            if(gesturePassword!=null){
//                if (getIntent().getStringExtra("forget")==null){
//                    Intent intent = new Intent(context, GestureLoginActivity.class);
//                    startActivity(intent);
//                }
//            }
//        }
    }

    @Override
    public void doCall(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            String type = jsonObject.optString("type") + "";

            if (type.equals("setting")){
                //goCapture();
                startActivity(new Intent(this, SettingActivity.class));
            }else if (type.equals("mallDetail")){
                goCapture();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //扫描结果回调
        if (resultCode == 161) {

            if (data!=null){
                Bundle bundle = data.getExtras();
                scanResult = bundle.getString("qr_scan_result");
                if (scanResult!=null){
                    webView.loadUrl(Web.url+"/"+Util.getStringResult(scanResult,"url"));
                }

                //将扫描出的信息显示出来
                Log.e("主页面将扫描出的信息显示出来", "scanResult:" + scanResult);
            }

        }
        Uri uri;
        if (data == null && MyChromeClient.mCameraFilePath.equals("")
                &&mUploadMessage == null && mUploadCallbackAboveL == null) {
            return;
        }
        if (data == null) {
            File file = new File(MyChromeClient.mCameraFilePath);
            uri = Uri.fromFile(file);
        } else {
            uri = data.getData();
        }
        switch (requestCode) {
            case RESULT_CODE_PICK_FROM_ALBUM_BELLOW_LOLLILOP:
//                uri = afterChosePic(data);
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(uri);
                    mUploadMessage = null;
                }
                break;
            case RESULT_CODE_PICK_FROM_ALBUM_ABOVE_LOLLILOP:
                try {
//                    uri = afterChosePic(data);
                    if (uri == null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[]{});
                        mUploadCallbackAboveL = null;
                        break;
                    }
                    if (mUploadCallbackAboveL != null && uri != null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[]{uri});
                        mUploadCallbackAboveL = null;
                    }
                } catch (Exception e) {
                    mUploadCallbackAboveL = null;
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }

            webView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearView();
            webView.removeAllViews();
            webView.destroy();
            if (mHomeWatcherReceiver != null) {
                try {
                    unregisterReceiver(mHomeWatcherReceiver);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        super.onDestroy();
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    if (Util.isRecent){
                        Util.isRecent=false;
                    }
                    else {
                        Util.islock=false;
                    }
                }
                else if (TextUtils.equals(SYSTEM_DIALOG_REASON_RECENT_APPS, reason)) {
                    Util.isRecent=true;
                    Util.islock=true;
                }
            }
            else if (TextUtils.equals(intentAction, Intent.ACTION_SCREEN_OFF)){
                Util.islock=false;
            }
        }
    }
    private void registerReceiver() {
        mHomeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mHomeWatcherReceiver, filter);
    }

    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
        mRefreshLayout.setPrimaryColorsId(colorPrimary, android.R.color.white);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, colorPrimaryDark));
        }
    }

    public void Req_Permisson(){
        HiPermission.create(context)
                .style(R.style.PermissionAnimFade)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()))//图标的颜色
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Toast.makeText(context,"权限被拒绝", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Toast.makeText(context,"权限被拒绝", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }


    //首页的动图加载动画太久了，不人性化，出此下策
    private void finishLoad(long all, long interal, final WebView webView1, final ShimmerLayout shimmerLayout1, final RefreshLayout refreshLayout1){
        CountDownTimer count=new CountDownTimer(all,interal) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                webView1.setVisibility(View.VISIBLE);
                shimmerLayout1.stopShimmerAnimation();
                shimmerLayout1.setVisibility(View.GONE);
                if (refreshLayout1!=null){
                    refreshLayout1.finishRefresh();
                }
                finishLoad(3000,1000,webView,shimmerLayout,mRefreshLayout);
            }
        };
        count.start();
    }

    private void initI18() {
        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (sp.getString("font","s").equals("t")) {
            config.locale = Locale.TAIWAN;
        } else {
            config.locale = Locale.CHINESE;
        }
        resources.updateConfiguration(config, dm);

    }

    private String myLastUrl(){
        String backPageUrl="";
        WebBackForwardList backForwardList = webView.copyBackForwardList();
        if (backForwardList != null && backForwardList.getSize() != 0) {
            //当前页面在历史队列中的位置
            int currentIndex = backForwardList.getCurrentIndex();
            WebHistoryItem historyItem =
                    backForwardList.getItemAtIndex(currentIndex - 1);
            if (historyItem != null) {
                backPageUrl = historyItem.getUrl();
//                Logger.t("111").d("拿到返回上一页的url"+backPageUrl);
            }
        }
        return backPageUrl;
    }


    public void goCapture(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    final String[] requestPermissionstr = {

                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

                    };
                    Util.checkpermissions(requestPermissionstr, context, new Util.PermissionsCallBack() {
                        @Override
                        public void success() {
                            if (CommonUtil.isCameraCanUse()) {
                                Intent intent = new Intent(context, CaptureActivity.class);
                                startActivityForResult(intent, REQUEST_CODE);
                            } else {
                                Toast.makeText(context, "请打开此应用的摄像头权限！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure() {

                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    Util.show(context,e.getMessage());
                }
            }
        });
    }
}
