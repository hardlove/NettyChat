package com.freddy.chat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
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
import com.freddy.im.netty.NettyTcpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

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

    //    String hosts = "[{\"host\":\"47.75.218.21\", \"port\":54321}]";
    String hosts = "[{\"host\":\"47.52.31.105\", \"port\":54321}]";

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
    private ArrayList<Item> datas;
    private EditText mEdtIp;//输入IP
    private EditText mEdtPort;//输入端口


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEdtIp = findViewById(R.id.edt_Ip);
        mEdtPort = findViewById(R.id.edt_port);

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

        refreshHost();


    }

    private Vector<String> convertHosts(String hosts) {
        if (hosts != null && hosts.length() > 0) {
            com.alibaba.fastjson.JSONArray hostArray = com.alibaba.fastjson.JSONArray.parseArray(hosts);
            if (null != hostArray && hostArray.size() > 0) {
                Vector<String> serverUrlList = new Vector<String>();
                com.alibaba.fastjson.JSONObject host;
                for (int i = 0; i < hostArray.size(); i++) {
                    host = JSON.parseObject(hostArray.get(i).toString());
                    serverUrlList.add(host.getString("host") + " "
                            + host.getInteger("port"));
                }
                return serverUrlList;
            }
        }
        return null;
    }

    /**
     * 修改服务器配置
     *
     * @param view
     */
    public void onChangeSetting(View view) {
        Log.d(TAG, "onChangeSetting: ~~~~~");
        mEdtIp.setEnabled(true);
        mEdtPort.setEnabled(true);
        loginAuth = false;

    }

    /**
     * 确认修改服务器配置
     *
     * @param view
     */
    public void onConfirm(View view) {
        Log.d(TAG, "onConfirm: ~~~~~~~~~");
        mEdtIp.setEnabled(false);
        mEdtPort.setEnabled(false);
        String ip = mEdtIp.getText().toString().trim();

        String port = mEdtPort.getText().toString().trim();

        hosts = "[{\"host\":\"" + ip + "\", \"port\":" + port + "}]";
        System.out.println("配置：" + hosts);

    }

    /**
     * 修改为测试服务器地址
     *
     * @param view
     */
    public void onTestSetting(View view) {
        Log.d(TAG, "onTestSetting: ~~~~~~~");
        hosts = "[{\"host\":\"47.52.31.105\", \"port\":54321}]";

        refreshHost();
    }

    /**
     * 刷新Host
     */
    private void refreshHost() {
        mEdtIp.setEnabled(false);
        mEdtPort.setEnabled(false);

        Vector<String> vector = convertHosts(hosts);
        String[] address = vector.get(0).split(" ");
        mEdtIp.setText(address[0]);
        mEdtPort.setText(address[1]);
        logout();
    }

    /**
     * 修改为本地服务器地址
     *
     * @param view
     */
    public void onLocalSetting(View view) {
        Log.d(TAG, "onLocalSetting: ~~~~~~~~~");
        hosts = "[{\"host\":\"47.75.218.21\", \"port\":54321}]";

        refreshHost();

    }

    /**
     * 测试重连
     *
     * @param view
     */
    public void onReConnect(View view) {
        Log.d(TAG, "onReConnect: ~~~~~");
        mtvLoginStatusText.setText("开始重连");
        NettyTcpClient.getInstance().resetConnect(false);
        loginAuth = false;


    }


    /**
     * 重置UI
     */
    private void clearUI() {

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
        loginAuth = false;
        Log.d(TAG, "loginIm: ~~~~~~~~~~~~");
        if (mEdtIp.isEnabled() || mEdtPort.isEnabled()) {
            Toast.makeText(this, "请确认当前环境配置！", Toast.LENGTH_SHORT).show();
            return;
        }
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
        Log.d(TAG, "btnlogOut: ~~~~~~~~~`");
        logout();

    }

    private void logout() {
        loginAuth = false;
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
        Log.d(TAG, "ClearMsg: ~~~~~~~~");
//        singleMsgReciveCount = 0;
//        signgleMsgSendCount = 0;
//        groupMsgReciveCount = 0;
//        groupMsgSendCount = 0;
//        NettyChatApp.instance.getMsgContainer().clear();
//        singleChatReceiveMap.clear();
//        groupChatReceiveMap.clear();
//        singleChatSendMap.clear();
//        groupChatSendMap.clear();

        clearUI();


    }

    /**
     * 发送消息
     *
     * @param view
     */
    public void sendMsg(View view) {
        Log.d(TAG, "sendMsg: ~~~~~~~~~");
        if (!loginAuth) {
            Toast.makeText(this, "请登录！", Toast.LENGTH_SHORT).show();
            return;
        }
        sendMsg();
        closeInputMethod();


    }


    /**
     * 发送批量消息
     *
     * @param view
     */
    public void sendMultiMsg(View view) {
        Log.d(TAG, "sendMultiMsg: ~~~~~~~~~~~~");
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

        handler.sendEmptyMessageDelayed(1, 300);

        closeInputMethod();


    }

    public void onStopSend(View view) {
        Log.d(TAG, "onStopSend: ~~~~~");
        handler.removeMessages(1);
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
                if (loginAuth) {
                    sendMsg();
                    handler.sendEmptyMessageDelayed(1, 300);
                } else {
                    System.out.println("====未登录。。。，无法发送消息");
                }

            }


        }
    };

    /**
     * 发送单条消息
     */
    private void sendMsg() {
        StringBuilder sb = new StringBuilder();
        if (type == 1) {
            signgleMsgSendCount++;
            sb.append("单聊【" + mEditContent.getText().toString().trim() + "】" + signgleMsgSendCount);
            addSingleChatSendCount(toUserId);
        } else if (type == 2) {
            groupMsgSendCount++;
            sb.append("群聊【" + mEditContent.getText().toString().trim() + "】" + groupMsgSendCount);
            addGroupChatSendCount(toUserId);
        }
        refreshMsgCount();

//        mTextSendMsg.append(sb.toString() + "\n");

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
     * 添加发送群聊消息计数
     *
     * @param toUserId
     */

    private void addGroupChatSendCount(String toUserId) {
        if (groupChatSendMap.containsKey(toUserId)) {
            groupChatSendMap.put(toUserId, groupChatSendMap.get(toUserId) + 1);
        } else {
            groupChatSendMap.put(toUserId, 1);
        }
    }

    /**
     * 添加发送单聊消息计数
     *
     * @param toUserId
     */
    private void addSingleChatSendCount(String toUserId) {
        if (singleChatSendMap.containsKey(toUserId)) {
            singleChatSendMap.put(toUserId, singleChatSendMap.get(toUserId) + 1);
        } else {
            singleChatSendMap.put(toUserId, 1);
        }
    }

    /**
     * 添加收到单聊消息计数
     *
     * @param fromUserId
     */
    private void addSingleChatReceiveCount(String fromUserId) {
        if (singleChatReceiveMap.containsKey(fromUserId)) {
            singleChatReceiveMap.put(fromUserId, singleChatReceiveMap.get(fromUserId) + 1);
        } else {
            singleChatReceiveMap.put(fromUserId, 1);
        }
    }

    /**
     * 添加收到群聊消息计数
     *
     * @param fromUserId
     */
    private void addGroupReceiveCount(String fromUserId) {
        if (groupChatReceiveMap.containsKey(fromUserId)) {
            groupChatReceiveMap.put(fromUserId, groupChatReceiveMap.get(fromUserId) + 1);
        } else {
            groupChatReceiveMap.put(fromUserId, 1);
        }
    }

    /**
     * 统计
     *
     * @param view
     */
    public void onStatistics(View view) {
        Log.d(TAG, "onStatistics: ~~~~~~~");
        CThreadPoolExecutor.runInBackground(new Runnable() {
            @Override
            public void run() {
                datas = getDatas();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this).setTitle("统计结果")
                                .setCancelable(false)
                                .setAdapter(getAdapter(), null)
                                .setPositiveButton("确定", null)
                                .create().show();

                    }
                });
            }
        });

    }

    public static class Item {
        public String fromUserId;
        public int msgCount;
        public boolean isTitle;

        public Item(String fromUserId, int msgCount) {
            this.fromUserId = fromUserId;
            this.msgCount = msgCount;
        }

        @Override
        public String toString() {
            if (isTitle) {
                return fromUserId + "   总数：" + msgCount;
            } else {
                return "fromUserId='" + fromUserId + '\'' + ", msgCount=" + msgCount;

            }
        }

        public String getFromUserId() {
            return fromUserId;
        }

        public void setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
        }

        public int getMsgCount() {
            return msgCount;
        }

        public void setMsgCount(int msgCount) {
            this.msgCount = msgCount;
        }

        public boolean isTitle() {
            return isTitle;
        }

        public void setTitle(boolean title) {
            isTitle = title;
        }
    }

    public ArrayList<Item> getDatas() {
        ArrayList<Item> datas = new ArrayList<>();


        int singleCount = 0;
        int groupCount = 0;

        Set<Map.Entry<String, Integer>> entries = singleChatReceiveMap.entrySet();
        Set<Map.Entry<String, Integer>> groupEntries = groupChatReceiveMap.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            datas.add(new Item(entry.getKey(), entry.getValue()));
            singleCount += entry.getValue();
        }
        Item singleCountItem = new Item("单聊统计", singleCount);
        singleCountItem.setTitle(true);

        datas.add(0, singleCountItem);

        int index = datas.size();
        for (Map.Entry<String, Integer> entry : groupEntries) {
            datas.add(new Item(entry.getKey(), entry.getValue()));
            groupCount += entry.getValue();
        }
        Item groupCountItem = new Item("群聊统计", groupCount);
        groupCountItem.setTitle(true);
        datas.add(index, groupCountItem);
        return datas;
    }

    @NonNull
    private ListAdapter getAdapter() {
        return new ArrayAdapter(this, android.R.layout.simple_list_item_1, datas);
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
        Log.d(TAG, "onDestroy: ~~~~~~~~~~");
        super.onDestroy();
        CEventCenter.unregisterEventListener(this, EVENTS);
    }

    /**
     * 记录收到某个人发来的单聊消息的数量
     */
    Map<String, Integer> singleChatReceiveMap = new HashMap<>();
    /**
     * 记录收到某个群发来的群聊聊消息的数量
     */
    Map<String, Integer> groupChatReceiveMap = new HashMap<>();

    /**
     * 记录发送到某个人的单聊消息的数量
     */
    Map<String, Integer> singleChatSendMap = new HashMap<>();
    /**
     * 记录发送到某个群消息的数量
     */
    Map<String, Integer> groupChatSendMap = new HashMap<>();


    @Override

    public void onCEvent(final String topic, final int msgCode, final int resultCode, final Object obj) {
        CThreadPoolExecutor.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                switch (topic) {
                    case Events.CHAT_SINGLE_MESSAGE: {
                        singleMsgReciveCount++;
                        final SingleMessage singleMessage = (SingleMessage) obj;
//                        mTvReciceMsg.append(singleMessage.getMsgId() + "\n");
                        addSingleChatReceiveCount(singleMessage.getFromId());

                        refreshMsgCount();

                        break;
                    }

                    case Events.CHAT_GROUP_MESSAGE:
                        groupMsgReciveCount++;
                        final GroupMessage groupMessage = (GroupMessage) obj;
//                        mTvReciceMsg.append(groupMessage.getMsgId() + "\n");
                        addGroupReceiveCount(groupMessage.getFromId());

                        refreshMsgCount();
                        break;

                    case Events.IM_LOGIN:
                        if (resultCode == IMConstant.LOGIN_AUTH_SUCCEED) {
                            loginAuth = true;
                            mtvLoginStatusText.setText("登录成功");
                        } else if (resultCode == IMConstant.LOGIN_AUTH_PROGRESSING) {
                            loginAuth = false;
                            mtvLoginStatusText.setText("正在登录");
                        } else if (resultCode == IMConstant.LOGIN_AUTH_FAILED) {
                            loginAuth = false;
                            ConnectivityManager cm = (ConnectivityManager) NettyChatApp.sharedInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo info = cm.getActiveNetworkInfo();
                            boolean connected = info != null && info.isConnected();
                            if (connected) {
                                mtvLoginStatusText.setText("登录失败");
                            } else {
                                mtvLoginStatusText.setText("网络异常");
                            }
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
        Log.d(TAG, "onConnecting: ~~~~~~");
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
        Log.d(TAG, "onConnected: ~~~~~~~~");
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
        Log.d(TAG, "onConnectFailed: ~~~~~~~~");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtvLoginStatusText.setText("连接失败");
                loginAuth = false;


            }
        });
    }
}
