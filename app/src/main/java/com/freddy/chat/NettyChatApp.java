package com.freddy.chat;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;


import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

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
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .methodCount(1)
                .tag("IM-Service").build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
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
