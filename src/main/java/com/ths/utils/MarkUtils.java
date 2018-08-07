package com.ths.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author wangyonghao
 */
@Slf4j
public class MarkUtils {

    /**获取对象的byte数组，出现异常返回null*/
    public static byte[] objectToBytes(Object var){
        if (var == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(var);

            byte[] bytes = bo.toByteArray();

            bo.close();
            oo.close();

            return bytes;
        } catch (IOException e) {
            log.error("===>结果对象序列化异常");
            e.printStackTrace();
            return null;
        }
    }

    /**byte数组转换成对象，出现异常返回null*/
    public static Object bytesToObject(byte[] bytes){
        Object obj = null;
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();

            bi.close();
            oi.close();

        } catch (IOException e) {
            log.error("===>结果对象序列化异常");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            log.error("===>反序列化没找到对应类");
            e.printStackTrace();
        }
        return obj;
    }

//
//public static void main(String[] args) {
//        Result result = new Result("序列化", Result.StatusCode.SUCCESS,"this is a data");
//        byte[] bytes = ObjectToBytes(result);
//        System.out.println(JSONObject.toJSONString(bytes));
//
//        Result temp = (Result) bytesToObject(bytes);
//        System.out.println(JSONObject.toJSONString(temp));
//
//    }
}