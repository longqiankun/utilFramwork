package com.lqk.utilframwok;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lqk.framework.app.Ioc;
import com.lqk.framework.inject.InjectLayer;
import com.lqk.framework.util.ToastUtils;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtils.showToast(this,"hello");
    }
}
