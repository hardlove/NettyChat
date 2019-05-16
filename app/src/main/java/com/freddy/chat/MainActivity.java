package com.freddy.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.Body;
import com.freddy.chat.bean.GroupMessage;
import com.freddy.chat.bean.Head;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.im.IMSConfig;
import com.freddy.im.constant.IMConstant;
import com.freddy.im.listener.IMSConnectStatusCallback;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements I_CEventListener, IMSConnectStatusCallback {


    private int singleMsgReciveCount;//收到的单聊消息数量
    private int groupMsgReciveCount;//收到的群聊消息数量
    private int signgleMsgSendCount;//已发单聊消息数量
    private int groupMsgSendCount;//已发群聊消息数量
    int type = 1;//单聊：1 ；群聊2


    String fromUserId;
    String fromUserToken;
    private String toUserId;
    private static int SEND_MSG_COUNT = 100;//设置消息发送的数量
    boolean loginAuth = false;//是否登录成功

    //    String hosts = "[{\"host\":\"192.168.0.147\", \"port\":54321}]";
    String hosts = "[{\"host\":\"47.52.255.159\", \"port\":54321}]";

    private String[] userIds;
    private String[] tokens;
    private static final String TAG = "MainActivity";


    private static final String[] EVENTS = {
            Events.CHAT_SINGLE_MESSAGE, Events.IM_LOGIN
    };


    private EditText mEditToken;//登录账号的token
    private EditText mEditContent;//输入发送消息的内容
    private TextView mtvLoginStatusText;//显示登录的状态
    private EditText mEdtSendMutilMsgCount;//输入批量发送消息的数量

    private TextView mTextSendMsg;//显示发送的消息
    private TextView mTvReciceMsg;//显示收到的消息

    private TextView mTvSendSingleMsgCount;//显示已发送的单聊消息数量
    private TextView mTvReciveSignleMsgCount;//显示已收到的单聊消息数量

    private TextView mTvSendGroupMsgCount;//显示已发送的群聊消息数量
    private TextView mTvReciveGroupMsgCount;//显示已收到的群聊消息数量

    private TextView mTvSendAllMsgCount;//显示已发送的单聊|群聊消息数量
    private TextView mTvReciveAllMsgCount;//显示已收到的单聊|群聊消息数量

    private TextView mTvSendGroupId;//显示发送给的群


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditContent = findViewById(R.id.et_content);
        mEditToken = findViewById(R.id.edtToken);
        mtvLoginStatusText = findViewById(R.id.tvLoginStatus);

        mEdtSendMutilMsgCount = findViewById(R.id.edtSendMsgCount);
        mEdtSendMutilMsgCount.setText("100");//设置默认批量发送的消息数量

        //发送、收到的消息
        mTextSendMsg = findViewById(R.id.tv_sendMsg);
        mTvReciceMsg = findViewById(R.id.tv_receiveMsg);

        //显示单聊发送、接收数量
        mTvReciveSignleMsgCount = findViewById(R.id.tvSingleMsgReceiveCount);
        mTvSendSingleMsgCount = findViewById(R.id.tvSingleMsgSendCount);
        //显示群聊发送、接收数量
        mTvSendGroupMsgCount = findViewById(R.id.tvGroupMsgSendCount);
        mTvReciveGroupMsgCount = findViewById(R.id.tvGroupMsgReceiveCount);
        //显示单聊和群聊发送、接收数量
        mTvSendAllMsgCount = findViewById(R.id.tvSendAllCount);
        mTvReciveAllMsgCount = findViewById(R.id.tvReceiveAllCount);

        //显示发送给的群
        mTvSendGroupId = findViewById(R.id.tvSendGroupId);

        Spinner loginUser = findViewById(R.id.spinner_login_user);
        final Spinner toUser = findViewById(R.id.spinner_to_user);

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

        RadioGroup radioGroup = findViewById(R.id.radioGroup_ChatType);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "onCheckedChanged: checkedId:" + checkedId);
                if (checkedId == R.id.btnSingleChat) {
                    type = 1;
                    toUser.setEnabled(true);
                    mTvSendGroupId.setText("");
                } else if (checkedId == R.id.btnGroupChat) {
                    type = 2;
                    toUserId = "e329dc13d6e444d19838b0c6406de8ab";
                    toUser.setEnabled(false);
                    mTvSendGroupId.setText(toUserId);
                }

            }
        });


    }

    /**
     * 重置UI
     */
    private void clearUI() {
        singleMsgReciveCount = 0;
        signgleMsgSendCount = 0;
        groupMsgReciveCount = 0;
        groupMsgSendCount = 0;
        mTextSendMsg.setText("");
        mTvReciceMsg.setText("");
        refreshMsgCount();
    }

    /**
     * 登录
     *
     * @param view
     */
    public void loginIm(View view) {
        mtvLoginStatusText.setText("正在登录");

        if (IMSClientBootstrap.getInstance().isActive()) {
            IMSClientBootstrap.getInstance().closeImsClient();
        }
        IMSClientBootstrap.getInstance().init(fromUserId, fromUserToken, hosts, IMSConfig.APP_STATUS_FOREGROUND, this);
        CEventCenter.registerEventListener(this, EVENTS);

    }

    /**
     * 退出
     *
     * @param view
     */
    public void btnlogOut(View view) {
        mtvLoginStatusText.setText("已退出登录");
        if (IMSClientBootstrap.getInstance().isActive()) {
            IMSClientBootstrap.getInstance().closeImsClient();
        }

    }

    /**
     * 清除
     *
     * @param view
     */
    public void ClearMsg(View view) {
        singleMsgReciveCount = 0;
        signgleMsgSendCount = 0;
        groupMsgReciveCount = 0;
        groupMsgSendCount = 0;
        NettyChatApp.instance.getMsgContainer().clear();
        clearUI();


    }

    /**
     * 发送消息
     *
     * @param view
     */
    public void sendMsg(View view) {
        if (!loginAuth) {
            Toast.makeText(this, "请登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        sendSingleMsg();
        closeInputMethod();


    }


    /**
     * 发送批量消息
     *
     * @param view
     */
    public void sendMultiMsg(View view) {
        if (!loginAuth) {
            Toast.makeText(this, "请登录！", Toast.LENGTH_SHORT).show();
            return;
        }

        String count = mEdtSendMutilMsgCount.getText().toString().replace(" ", "");
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
            if (signgleMsgSendCount < SEND_MSG_COUNT) {
                sendSingleMsg();
                handler.sendEmptyMessageDelayed(1, 300);
            }


        }
    };

    /**
     * 发送单条消息
     */
    private void sendSingleMsg() {
        StringBuilder sb = new StringBuilder();
        if (type == 1) {
            signgleMsgSendCount++;
            sb.append("单聊【" + mEditContent.getText().toString().trim() + "】" + signgleMsgSendCount);
        } else if (type == 2) {
            groupMsgSendCount++;
            sb.append("群聊【" + mEditContent.getText().toString().trim() + "】" + groupMsgSendCount);
        }
        refreshMsgCount();
        mTextSendMsg.append(sb.toString() + "\n");

        AppMessage appMessage = new AppMessage();
        Head head = new Head();
        Body body = new Body();
        head.setId(toUserId);
        head.setToken(fromUserToken);
        head.setSource(IMConstant.SOURCE);
        if (type == 2) {
            head.setSendUserId(fromUserId);
        }
        head.setMessageId(UUID.randomUUID().toString());
        head.setTime(System.currentTimeMillis());
        head.setType(type);//单聊
        head.setContentType(1);//文本

        body.setPrk("私钥123");
        body.setData(sb.toString());
        appMessage.setHead(head);
        appMessage.setBody(body);
        MessageProcessor.getInstance().sendMsg(appMessage);

    }

    /**
     * 刷新消息数量
     */
    private void refreshMsgCount() {
        mTvSendSingleMsgCount.setText("已发单聊消息总数:" + signgleMsgSendCount);
        mTvSendGroupMsgCount.setText("已发群聊消息总数:" + groupMsgSendCount);
        mTvSendAllMsgCount.setText("已发单聊|群聊消息总数:" + (signgleMsgSendCount + groupMsgSendCount));


        mTvReciveSignleMsgCount.setText("收到单聊消息总数：" + singleMsgReciveCount);
        mTvReciveGroupMsgCount.setText("收到群聊消息总数：" + groupMsgReciveCount);
        mTvReciveAllMsgCount.setText("收到单聊|群聊消息总数：" + (singleMsgReciveCount + groupMsgReciveCount));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CEventCenter.unregisterEventListener(this, EVENTS);
    }

    @Override
    public void onCEvent(final String topic, final int msgCode, final int resultCode, final Object obj) {
        CThreadPoolExecutor.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                switch (topic) {
                    case Events.CHAT_SINGLE_MESSAGE: {
                        singleMsgReciveCount++;
                        final SingleMessage singleMessage = (SingleMessage) obj;
                        mTvReciceMsg.append(singleMessage.getContent());

                        refreshMsgCount();

                        break;
                    }

                    case Events.CHAT_GROUP_MESSAGE:
                        groupMsgReciveCount++;
                        final GroupMessage groupMessage = (GroupMessage) obj;
                        mTvReciceMsg.append(groupMessage.getContent());

                        refreshMsgCount();
                        break;

                    case Events.IM_LOGIN:
                        if (resultCode == IMConstant.LOGIN_AUTH_SUCCEED) {
                            loginAuth = true;
                            mtvLoginStatusText.setText("登录成功");
                        } else if (msgCode == IMConstant.LOGIN_AUTH_PROGRESSING) {
                            loginAuth = false;
                            mtvLoginStatusText.setText("正在登录");
                        } else if (msgCode == IMConstant.LOGIN_AUTH_FAILED) {
                            loginAuth = false;
                            mtvLoginStatusText.setText("登录失败");
                        } else if (resultCode == IMConstant.LOGIN_AUTH_KICK_OUT) {
                            loginAuth = false;
                            mtvLoginStatusText.setText("你已被踢下线");
                        }
                        break;
                    default:
                        break;
                }


            }
        });
    }

    @Override
    public void onConnecting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvLoginStatusText.setText("正在连接");
                loginAuth = false;

            }
        });
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvLoginStatusText.setText("连接成功");
                loginAuth = false;


            }
        });
    }

    @Override
    public void onConnectFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvLoginStatusText.setText("连接失败");
                loginAuth = false;


            }
        });
    }
}
