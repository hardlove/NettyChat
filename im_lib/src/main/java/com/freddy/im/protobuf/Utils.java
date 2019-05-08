package com.freddy.im.protobuf;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * Created by CL on 2019/5/8.
 *
 * @description:
 */

public class Utils {
    public static String format(Message protoMsg) {
        String jsonFormat = JsonFormat.printToString(protoMsg);
        return jsonFormat;

    }
}
