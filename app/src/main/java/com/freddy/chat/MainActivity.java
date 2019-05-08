package com.freddy.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.freddy.chat.bean.AppMessage;
import com.freddy.chat.bean.Body;
import com.freddy.chat.bean.Head;
import com.freddy.chat.bean.SingleMessage;
import com.freddy.chat.event.CEvent;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.I_CEventListener;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.im.MessageType;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.im.IMSClientFactory;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements I_CEventListener {

    private EditText mEditContent;
    private TextView mTextView;
    private EditText mEditUserId;
    private EditText mEditToken;
    private Button mBtnLogin;

    String userId;
    String token;
    private String toUserId;

//    String hosts = "[{\"host\":\"192.168.0.160\", \"port\":54321}]";
    String hosts = "[{\"host\":\"47.52.255.159\", \"port\":54321}]";

//10001    1475ae4964f9497c85f63f22c5a255ee
//10002     8dfee02f76d744ffa9091c03b999a1fc
//10003     de58eeab6ddc433786871cf93e077303
//10004     035742776f784945b308cdce7ab93a0b
//10005     b67ec4ba19d0449f996ac4212099bfa0
//10006     a035520c2f4d4131bcce6b6572f0153a
//10007     e9c53687e4b84aab8749dec0e2bf4dc4
//10008     0c68c971673a4fcfa9434f76519ae4f3
//10009      331e5e6449e24337883f975cc679be43
//10010      22a7a900f0c54278b048c2f1932fd6e1
    private String[] userIds = {
        "10001",
        "10002",
//        "10003",
//        "10004",
//        "10005",
//        "10006",
//        "10007",
//        "10008",
//        "10009",
//        "10010"
    };
    private String[] tokens = {
            "1475ae4964f9497c85f63f22c5a255ee",
            "8dfee02f76d744ffa9091c03b999a1fc",
//            "de58eeab6ddc433786871cf93e077303",
//            "035742776f784945b308cdce7ab93a0b",
//            "b67ec4ba19d0449f996ac4212099bfa0",
//            "a035520c2f4d4131bcce6b6572f0153a",
//            "e9c53687e4b84aab8749dec0e2bf4dc4",
//            "0c68c971673a4fcfa9434f76519ae4f3",
//            "331e5e6449e24337883f975cc679be43",
//            "22a7a900f0c54278b048c2f1932fd6e1"

    };



    private static final String[] EVENTS = {
            Events.CHAT_SINGLE_MESSAGE
    };
    private EditText mEditToUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditContent = findViewById(R.id.et_content);
        mTextView = findViewById(R.id.tv_msg);
        mEditUserId = findViewById(R.id.edtUserId);
        mEditToken = findViewById(R.id.edtToken);
        mBtnLogin = findViewById(R.id.btnLogin);
        mEditToUser = findViewById(R.id.edtToUser);


        userId = userIds[0];
        token = tokens[0];
        mEditUserId.setText(userId);
        mEditToken.setText(token);
        mEditUserId.setEnabled(false);
        mEditToken.setEnabled(false);
        toUserId = userIds[1];

    }

    int index;
    public void ChangeUser(View view) {
        index++;
        index = index % userIds.length;

        userId = userIds[index];
        token = tokens[index];
        mEditUserId.setText(userId);
        mEditToken.setText(token);
        if (index == 0) {
            toUserId = userIds[1];
        } else {
            toUserId = userIds[0];
        }
        mEditToUser.setText(toUserId);

    }

    public void loginIm(View view) {
        if (index == 0) {
            toUserId = userIds[1];
        } else {
            toUserId = userIds[0];
        }
        mEditToUser.setText(toUserId);

        if (!IMSClientFactory.getIMSClient().isClosed()) {
            IMSClientFactory.getIMSClient().close();

        }
//        userId = mEditUserId.getText().toString().trim();
//        token = mEditToken.getText().toString().trim();
        IMSClientBootstrap.getInstance().init(userId, token, hosts, 1);
        CEventCenter.registerEventListener(this, EVENTS);

    }


    public void sendMsg(View view) {

        AppMessage appMessage = new AppMessage();
        Head head = new Head();
        Body body = new Body();
        head.setId(toUserId);
        head.setToken(token);
        head.setSource("android");
        head.setSendUserId(userId);
        head.setMessageId(UUID.randomUUID().toString());
        head.setTime(System.currentTimeMillis());
        head.setType(1);//单聊
        head.setContentType(1);//文本

        body.setPrk("私钥123");
        body.setData(mEditContent.getText().toString().trim());

        appMessage.setHead(head);
        appMessage.setBody(body);
        MessageProcessor.getInstance().sendMsg(appMessage);

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
                        mTextView.setText(message.getContent());
                    }
                });
                break;
            }

            default:
                break;
        }
    }
}
