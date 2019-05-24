package com.lanpini.lpn_android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lanpini.lpn_android.ScreenAdaptation;
import com.lanpini.lpn_android.util.ActivityCollector;

public class BaseActivity extends AppCompatActivity {
    public Activity context;
    public SharedPreferences sp;
    public boolean isWidth() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = this;
        Log.e("isWidth", "isWidth()" + isWidth());
        ScreenAdaptation.setCustomDesity(this, getApplication(), isWidth());
        ActivityCollector.addActivity(this);
        sp= getSharedPreferences("loginUser", Context.MODE_PRIVATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
