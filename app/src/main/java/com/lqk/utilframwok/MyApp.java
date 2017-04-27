package com.lqk.utilframwok;/**
 * Created by shiqichuban on 17/4/27.
 */

import android.app.Application;
import android.graphics.Color;

import com.lqk.framework.app.Ioc;
import com.lqk.framework.util.ToastUtils;

/**
 * name: longqiankun
 * email: longqiankun@shiqichuban.com
 * Date: 2017-04-27
 * Time: 16:44
 * Company: 拾柒网络
 * description:
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Ioc.getIoc().init(this,false);
        ToastUtils.setBg(R.drawable.toast);
        ToastUtils.setTextColor(Color.parseColor("#eeeeee"));
        ToastUtils.setTextsize(16);
    }
}
