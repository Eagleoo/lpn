package com.lanpini.lpn_android.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.util.Util;
import com.lanpini.lpn_android.util.Web;
import com.lanpini.lpn_android.widget.UpDataVersionDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yanzhenjie.sofia.Sofia;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.relative_about)
    RelativeLayout relative_about;
    @BindView(R.id.relative_update)
    RelativeLayout relative_update;
    @BindView(R.id.relative_clear)
    RelativeLayout relative_clear;
    @BindView(R.id.share_iv)
    View share;
    @BindView(R.id.refresh)
    View refresh;
    @BindView(R.id.back)
    View back;
    @BindView(R.id.center)
    TextView center;
    @BindView(R.id.tv_update)
    TextView tv_update;
    @BindView(R.id.tv_clear)
    TextView tv_clear;
    @BindView(R.id.btn_cancle)
    Button btn_cancle;
    @BindView(R.id.linearAll)
    LinearLayout linearAll;
    @BindView(R.id.update_show)
    ImageView update_show;

    String version;
    String content;
    String url;
    String createTime;
    boolean isNew=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);
        checkVersion();
        try {
            tv_clear.setText(Util.getTotalCacheSize(getApplicationContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        back.setVisibility(View.VISIBLE);
        center.setText(R.string.setting);
        linearAll.setVisibility(View.GONE);
        share.setVisibility(View.GONE);
        refresh.setVisibility(View.GONE);
        Sofia.with(this)
                .statusBarBackground(ContextCompat.getColor(this, R.color.colorAccent))
                .invasionStatusBar();

        setAnyBarAlpha(0);
    }
    private void setAnyBarAlpha(int alpha) {

        Sofia.with(this)
                .statusBarBackgroundAlpha(alpha);
    }


    @SuppressLint("SetTextI18n")
    @OnClick({R.id.relative_about, R.id.relative_font, R.id.relative_update, R.id.relative_clear,R.id.back,R.id.btn_cancle})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.relative_about:
                Intent intent1=new Intent();
                intent1.setClass(context,MainActivity.class);
                intent1.putExtra("about","about");
                startActivity(intent1);
                break;

            case R.id.relative_font:
                showSingleChoiceListAlertDialog();
                break;
            case R.id.relative_update:
                if (isNew){
                    new UpDataVersionDialog(context, content, url).show();
                }
                else {
                    Toast.makeText(context, R.string.high_update, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.relative_clear:
                try {
                    String size=Util.getTotalCacheSize(getApplicationContext());
                    size=size.substring(0,size.length()-2);
                    if (Double.valueOf(size)!=0&&!tv_clear.getText().toString().equals("0.0")){
                        Util.show(context,getString(R.string.clear1)+Util.getTotalCacheSize(getApplicationContext())+getString(R.string.clear2));
                        Util.clearAllCache(context);
                        tv_clear.setText("");

                    }
                    else {
                        Toast.makeText(context, R.string.no_cache, Toast.LENGTH_SHORT).show();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.back:
                finish();
                break;
            case R.id.btn_cancle:
                final Intent intent=new Intent();
                intent.putExtra("loginout","loginout");
                intent.setClass(context,MainActivity.class);
                new AlertDialog.Builder(context).setTitle(R.string.careful)
                        .setMessage(R.string.out)
                        .setIcon(R.drawable.logo)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

                break;
        }

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
                    tv_update.setText(getString(R.string.new_update).toString()+version);
                    update_show.setVisibility(View.VISIBLE);
                    isNew=true;
                }
                else {
                    tv_update.setText(getString(R.string.high_update1).toString()+version);
                    isNew=false;
                    update_show.setVisibility(View.GONE);
                }
            }
        });
    }

    private String mCheckedItem;
    private void showSingleChoiceListAlertDialog() {
        final String[] list = new String[]{"简体中文", "繁體中文"};
        int checkedItemIndex = 0;
        if (sp.getString("font","").equals("t")){
            checkedItemIndex=1;
        }

        mCheckedItem = list[checkedItemIndex];

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setSingleChoiceItems(list,
                        checkedItemIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCheckedItem = list[which];
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initI18();
                    }
                })
                .show();
    }


    private void initI18() {
        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Intent intent = new Intent(this, MainActivity.class);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (mCheckedItem.equals("繁體中文")) {
            config.locale = Locale.TAIWAN;
            intent.putExtra("font","t");
            editor.putString("font","t");
            editor.commit();
        } else {
            config.locale = Locale.CHINESE;
            intent.putExtra("font","s");
            editor.putString("font","s");
            editor.commit();
        }
        resources.updateConfiguration(config, dm);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



}
