package com.freddy.chat.bean;

/**
 * Created by CL on 2019/5/8.
 *
 * @description: 消息体
 */

public class Body {
    /*私钥*/
    private String prk;
    /*消息体*/
    private String data;

    public String getPrk() {
        return prk;
    }

    public void setPrk(String prk) {
        this.prk = prk;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Body{" +
                "prk='" + prk + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
