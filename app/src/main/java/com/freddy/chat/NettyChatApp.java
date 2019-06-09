package com.freddy.chat;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       NettyChatApp.java</p>
 * <p>@PackageName:     com.freddy.chat</p>
 * <b>
 * <p>@Description:     类描述</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/07 23:58</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class NettyChatApp extends Application {

    public static NettyChatApp instance;

    public static NettyChatApp sharedInstance() {
        if (instance == null) {
            throw new IllegalStateException("app not init...");
        }
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        msgContainer = new HashMap<>();
    }

    //消息容器
    private   Map<String, String> msgContainer;
    public  Map<String, String> getMsgContainer(){
        return msgContainer;
    };

}
