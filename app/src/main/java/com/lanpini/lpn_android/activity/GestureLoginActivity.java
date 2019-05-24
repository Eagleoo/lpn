package com.lanpini.lpn_android.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lanpini.lpn_android.R;
import com.lanpini.lpn_android.util.ACache;
import com.lanpini.lpn_android.util.Constant;
import com.lanpini.lpn_android.util.Util;
import com.lanpini.lpn_android.util.Web;
import com.star.lockpattern.util.LockPatternUtil;
import com.star.lockpattern.widget.LockPatternView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sym on 2015/12/24.
 */
public class GestureLoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginGestureActivity";

    @BindView(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @BindView(R.id.messageTv)
    TextView messageTv;
    @BindView(R.id.forgetGestureBtn)
    Button forgetGestureBtn;
    @BindView(R.id.tv_username)
    TextView tv_username;
    @BindView(R.id.portrait)
    ImageView img_portrait;
    private Animation shake;

    private ACache aCache;
    private static final long DELAYTIME = 600l;
    private byte[] gesturePassword;
    private SharedPreferences sp;
    private int erros=0;
    private Long times;
    private Long times_less;//时间差
    private TimeCount timer;
    private File file=null;
    private String Uri_path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_login);
        ButterKnife.bind(this);
        this.init();
    }

    @SuppressLint("ResourceAsColor")
    private void init() {
        shake = AnimationUtils.loadAnimation(this, R.anim.shaker);
        sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
        Uri_path=sp.getString("path","");
        if (!Uri_path.equals("")){
            file = new File(Uri_path);
        }
        times=System.currentTimeMillis();

        if (!sp.getBoolean("isError",true)){
            if (times-sp.getLong("times",0)>sp.getLong("times_less",0)){
                lockPatternView.setVisibility(View.VISIBLE);
            }
            else {
                timer=new TimeCount(sp.getLong("times_less",0)-(times-sp.getLong("times",0)),1000);
                timer.start();
                lockPatternView.setVisibility(View.GONE);
            }
        }
        String phone= Util.phoneFormat( sp.getString("username",""));
        String portrait= sp.getString("portrait","");
        tv_username.setText(phone);
        if (portrait.equals("default")){
            img_portrait.setImageResource(R.drawable.logo);
        }

        else {
            if (file!=null){
                if (Uri_path.contains("content")){
                    Glide.with(GestureLoginActivity.this).load(Uri_path).placeholder(R.drawable.logo).into(img_portrait);
                }
                else {
                    Glide.with(GestureLoginActivity.this).load(file).placeholder(R.drawable.logo).into(img_portrait);
                }

            }
            else {
                Glide.with(GestureLoginActivity.this).load(Web.url+portrait).placeholder(R.drawable.logo).into(img_portrait);
            }
        }
        aCache = ACache.get(GestureLoginActivity.this);
        //得到当前用户的手势密码
        gesturePassword = aCache.getAsBinary(Constant.GESTURE_PASSWORD);
        lockPatternView.setOnPatternListener(patternListener);
        lockPatternView.setTactileFeedbackEnabled(true);
        updateStatus(Status.DEFAULT);
    }

    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
        }

        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
            if(pattern != null){
                if(LockPatternUtil.checkPattern(pattern, gesturePassword)) {
                    updateStatus(Status.CORRECT);

                } else {
                    updateStatus(Status.ERROR);
                }
            }
        }
    };

    /**
     * 更新状态
     * @param status
     */
    private void updateStatus(Status status) {
        messageTv.setText(status.strId);
        messageTv.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                messageTv.startAnimation(shake);
                erros++;
                if (erros>=5){
                    lockPatternView.setVisibility(View.GONE);
                    timer=new TimeCount(60000, 1000);
                    timer.start();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isError", false);
                    editor.commit();

                }
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                if (getIntent().getStringExtra("update")!=null){
                    Util.showIntent(GestureLoginActivity.this,CreateGestureActivity.class);
                    finish();
                }
                else {
                    loginGestureSuccess();
                }

                break;
        }
    }

    /**
     * 手势登录成功（去首页）
     */
    private void loginGestureSuccess() {
        Util.islock=true;
        finish();
    }

    /**
     * 忘记手势密码（去账号登录界面）
     */
    @OnClick(R.id.forgetGestureBtn)
    void forgetGesturePasswrod() {

        this.finish();
    }

    private enum Status {
        //默认的状态
        DEFAULT(R.string.gesture_default, R.color.grey_a5a5a5),
        //密码输入错误
        ERROR(R.string.gesture_error, R.color.red_f4333c),
        //密码输入正确
        CORRECT(R.string.gesture_correct, R.color.grey_a5a5a5);


        private Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
        private int strId;
        private int colorId;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&&getIntent().getStringExtra("update")==null){
            new AlertDialog.Builder(GestureLoginActivity.this).setTitle("提示：").setMessage("您确定要退出吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                    System.exit(0);
                }
            }).setNegativeButton("取消", null).setIcon(R.drawable.logo).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            messageTv.setTextColor(Color.RED);
            messageTv.setText("请"+millisUntilFinished / 1000 +"s后重新再试");
            times_less=millisUntilFinished;
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("times", System.currentTimeMillis());
            editor.putLong("times_less",times_less);
            editor.commit();
        }

        @Override
        public void onFinish() {
            updateStatus(Status.DEFAULT);
            lockPatternView.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isError", true);
            editor.commit();
            erros=0;
        }
    }

}
