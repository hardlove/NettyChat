package com.freddy.chat;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.Body;
import com.freddy.chat.bean.Head;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.im.IMSConfig;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements I_CEventListener {

    private EditText mEditContent;
    private TextView mTextView;
    private EditText mEditToken;
    private Button mBtnLogin;
    private TextView mTvSignleMsgCount;
    private int singMsgReciveCount;//收到的单聊消息数量
    private int signMsgSendCount;//

    String fromUserId;
    String fromUserToken;
    private String toUserId;
    private static int SEND_MSG_COUNT = 100;//设置消息发送的数量

    //        String hosts = "[{\"host\":\"192.168.0.145\", \"port\":54321}]";
    String hosts = "[{\"host\":\"47.52.255.159\", \"port\":54321}]";//10001    1475ae4964f9497c85f63f22c5a255ee

    private String[] userIds;
    private String[] tokens;


    private static final String[] EVENTS = {
            Events.CHAT_SINGLE_MESSAGE, Events.IM_LOGIN
    };
    private EditText mEditToUser;
    private TextView mtvLoginStatusText;
    private EditText mEdtSendMsgCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditContent = findViewById(R.id.et_content);
        mTextView = findViewById(R.id.tv_msg);
        mEditToken = findViewById(R.id.edtToken);
        mBtnLogin = findViewById(R.id.btnLogin);
        mTvSignleMsgCount = findViewById(R.id.tvSingleMsgCount);
        mtvLoginStatusText = findViewById(R.id.tvLoginStatus);
        mEdtSendMsgCount = findViewById(R.id.edtSendMsgCount);
        mEdtSendMsgCount.setText("100");

        Spinner loginUser = findViewById(R.id.spinner_login_user);
        Spinner toUser = findViewById(R.id.spinner_to_user);

        userIds = getResources().getStringArray(R.array.userIds);
        tokens = getResources().getStringArray(R.array.userTokens);
        loginUser.setPrompt("请选择要登录的用户");
        loginUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromUserId = userIds[position];
                fromUserToken = tokens[position];
                mEditToken.setText(tokens[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mEditToken.setText("");

            }
        });
        toUser.setPrompt("请选择要接收消息的用户");
        toUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toUserId = userIds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loginUser.setSelection(0);
        toUser.setSelection(1);

        mtvLoginStatusText.setText("当前账号未登录");


    }

    private void resetUI() {
        mTvSignleMsgCount.setText("收到单聊消息总数：" + singMsgReciveCount);
        mTextView.setText("");
    }

    public void loginIm(View view) {
        mtvLoginStatusText.setText("正在登录");

        if (!IMSClientBootstrap.getInstance().isActive()) {
            IMSClientBootstrap.getInstance().closeImsClient();
        }
        IMSClientBootstrap.getInstance().closeImsClient();
        IMSClientBootstrap.getInstance().init(fromUserId, fromUserToken, hosts, IMSConfig.APP_STATUS_FOREGROUND);
        CEventCenter.registerEventListener(this, EVENTS);

    }

    public void btnlogOut(View view) {
        mtvLoginStatusText.setText("已退出登录");
        IMSClientBootstrap.getInstance().closeImsClient();

    }

    public void ClearMsg(View view) {
        singMsgReciveCount = 0;
        signMsgSendCount = 0;
        NettyChatApp.instance.getMsgContainer().clear();
        resetUI();


    }

    public void sendMsg(View view) {
        sendSingleMsg();
        closeInputMethod();

    }



    public void sendMultiMsg(View view) {
        String count = mEdtSendMsgCount.getText().toString().replace(" ", "");

        try {
            SEND_MSG_COUNT = Integer.valueOf(count);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessageDelayed(1, 100);
        closeInputMethod();

    }

    private void closeInputMethod() {
        InputMethodManager inputService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputService.hideSoftInputFromWindow(mEditContent.getWindowToken(), 0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (signMsgSendCount < SEND_MSG_COUNT) {
                sendSingleMsg();
                handler.sendEmptyMessageDelayed(1, 300);
            }


        }
    };

    private void sendSingleMsg() {
        signMsgSendCount++;

        AppMessage appMessage = new AppMessage();
        Head head = new Head();
        Body body = new Body();
        head.setId(toUserId);
        head.setToken(fromUserToken);
        head.setSource("android");
        head.setSendUserId(fromUserId);
        head.setMessageId(UUID.randomUUID().toString());
        head.setTime(System.currentTimeMillis());
        head.setType(1);//单聊
        head.setContentType(1);//文本

        body.setPrk("私钥123");
        body.setData(mEditContent.getText().toString().trim() + "---" + signMsgSendCount);
        appMessage.setHead(head);
        appMessage.setBody(body);
        MessageProcessor.getInstance().sendMsg(appMessage);


        TextView sendCount = findViewById(R.id.tvSingleMsgSendCount);
        sendCount.setText("已发单聊消息总数：" + signMsgSendCount);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CEventCenter.unregisterEventListener(this, EVENTS);
    }

    @Override
    public void onCEvent(String topic, int msgCode, int resultCode, Object obj) {
        switch (topic) {
            case Events.CHAT_SINGLE_MESSAGE: {
                final SingleMessage message = (SingleMessage) obj;
                CThreadPoolExecutor.runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        mTextView.append("\n");
                        mTextView.append(message.getContent());
                        singMsgReciveCount++;
                        mTvSignleMsgCount.setText("收到到单聊消息总数：" + singMsgReciveCount);

                    }
                });
                break;
            }
            case Events.IM_LOGIN:
                final Boolean status = (Boolean) obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status) {
                            mtvLoginStatusText.setText("登录成功");
                        } else {
                            mtvLoginStatusText.setText("登录失败");
                        }
                    }
                });
                break;
            default:
                break;
        }
    }
}
