package com.myhuanghai.mymqtt;


import java.io.UnsupportedEncodingException;

/**
 * Created by huang on 2017/6/30.
 */
public class ByteUtils {


    /**
     * string到字节数组的转换.
     */
    public static byte[] stringToByte(String str) throws UnsupportedEncodingException {
        return str.getBytes("UTF-8");
    }

    /**
     * 字节数组到String的转换.
     */
    public static String bytesToString(byte[] str) {
        String keyword = null;
        try {
            keyword = new String(str,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return keyword;
    }

}
