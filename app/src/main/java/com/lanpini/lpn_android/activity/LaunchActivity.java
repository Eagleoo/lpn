package com.lanpini.lpn_android.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.util.MyShardPreferen;
import com.lanpini.lpn_android.util.SelectorFactory;
import com.lanpini.lpn_android.util.Util;
import com.stx.xhb.xbanner.XBanner;
import com.yanzhenjie.sofia.Sofia;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;

public class LaunchActivity extends BaseActivity {

    @BindView(R.id.luncher)
    ImageView luncher;
    @BindView(R.id.skipTv)
    TextView skipTv;
    @BindView(R.id.xbanner)
    XBanner xBanner;
    @BindView(R.id.content)
    RelativeLayout relativeLayout;


    int point = 0;

    /**
     * 开始点击的位置
     */
    private int startX;

    /**
     * 临界值
     */
    private int criticalValue = 200;
    private void setAnyBarAlpha(int alpha) {

        Sofia.with(this)
                .statusBarBackgroundAlpha(alpha);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        ButterKnife.bind(this);
        Log.e("isFristLaunch", "isFristLaunch" + MyShardPreferen.isFristLaunch(this));
        Sofia.with(this)
                //.navigationBarBackground(Color.parseColor("#FA8632"))
                .statusBarBackground(ContextCompat.getColor(this, R.color.corner_color))
                .invasionStatusBar();

        setAnyBarAlpha(0);
        SharedPreferences sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (MyShardPreferen.isFristLaunch(this)) {
            editor.putString("show_alert","1");
            editor.commit();
            xBanner.setVisibility(View.VISIBLE);

            relativeLayout.setVisibility(View.GONE);
            final List<Integer> imgesUrl = new ArrayList<>();
            imgesUrl.add(R.drawable.launcher);
            imgesUrl.add(R.drawable.launcher);
            imgesUrl.add(R.drawable.launcher);
            xBanner.setData(imgesUrl, null);//第二个参数为提示文字资源集合
            xBanner.loadImage(new XBanner.XBannerAdapter() {
                @Override
                public void loadBanner(XBanner banner, Object model, View view, int position) {

                    Glide.with(LaunchActivity.this).load(imgesUrl.get(position)).into((ImageView) view);
                }
            });
            xBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    Log.e("onPageScrolled", "position:" + position + "positionOffset:" + positionOffset + "positionOffsetPixels:" + positionOffsetPixels);
                    point = position;
                }

                @Override
                public void onPageSelected(int position) {
                    Log.e("onPageSelected", "position:" + position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            ViewPager viewPager = xBanner.getViewPager();
            viewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case ACTION_DOWN:
                            startX = (int) event.getX();
                            Log.e("startX", "startX:" + startX);
                            break;
                        case ACTION_MOVE:
                            Log.e("event.getX()", "startX:" + startX + "event.getX():" + event.getX());
                            if (startX - event.getX() > criticalValue && point == imgesUrl.size() - 1) {
                                Log.e("event.getX()", "true");
                                enter();
                            }
                            break;
                    }

                    return false;
                }
            });

        } else {
            editor.putString("show_alert","0");
            editor.commit();
            xBanner.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enter();
                }
            }, 3000);
        }


        skipTv.setBackground(SelectorFactory.newShapeSelector()
                .setDefaultBgColor(Color.parseColor("#00ffffff"))
                .setPressedBgColor(Color.parseColor("#489FE4"))
                .setStrokeWidth(Util.dpToPx(context, 0.5f))
                .setCornerRadius(Util.dpToPx(context, 5))
                .setDefaultStrokeColor(Color.parseColor("#ffffff"))
                .create(context));
        if (sp.getString("font","s").equals("t")) {
            skipTv.setText("跳過");
        } else {
            skipTv.setText("跳过");
        }
    }


    @OnClick({R.id.skipTv})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.skipTv:
                enter();
                break;

        }
    }

    int star = 0;

    public synchronized void enter() {


        if (star > 0) {
            return;
        }
        MyShardPreferen.setFristLaunch(this);
        star++;
        luncher.setVisibility(View.VISIBLE);


        Bundle bundle = context.getIntent().getExtras();
        if (!Util.isNull(bundle) && !Util.isNull(bundle.getString("name"))) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("firstIn","firstIn");
            startActivity(intent);
        }
        finish();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


}
