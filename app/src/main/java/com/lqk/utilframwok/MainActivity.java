package com.lqk.utilframwok;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lqk.framework.app.Ioc;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ioc.getIoc().init(getApplication());
    }
}
