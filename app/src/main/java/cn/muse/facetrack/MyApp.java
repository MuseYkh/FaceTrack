package cn.muse.facetrack;

import android.app.Application;

/**
 * author: muse
 * created on: 2018/12/11 下午3:38
 * description:
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
