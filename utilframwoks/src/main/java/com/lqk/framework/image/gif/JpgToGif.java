package com.lqk.framework.image.gif;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class JpgToGif {
        public void jpgToGif(String pic[], String newPic) {
           try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(1);
            e.start(newPic);
            for (int i = 0; i < pic.length; i++) {
                // 设置播放的延迟时间
                e.setDelay(300);
                Bitmap src = BitmapFactory.decodeFile(pic[i]);
                e.addFrame(src); // 添加到帧中
            }
            e.finish();// 刷新任何未决的数据，并关闭输出文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}