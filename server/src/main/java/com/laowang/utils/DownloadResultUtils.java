package com.laowang.utils;

import com.laowang.domain.Result;

/**
 * @author wangyonghao
 */
public class DownloadResultUtils {

    /**封装各种成功情况*/
    public static Result success(Result.StatusCode code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static Result success(String msg, Object data){
        return success(Result.StatusCode.SUCCESS,msg,data);
    }

    public static Result success(Object data){
        return success(Result.StatusCode.SUCCESS,"OK",data);
    }

    public static Result success(String msg){
        return  success(msg,null);
    }

    public static Result success() {
        return success("成功");
    }

    /**封装失败情况*/
    public static Result error(String msg){
        Result result = new Result();
        result.setCode(Result.StatusCode.FAIL);
        result.setMsg(msg);
        return result;
    }


    /**封装重发请求*/
    public static Result again(){
        Result result = new Result();
        result.setCode(Result.StatusCode.AGAIN);
        result.setMsg("请稍后再请求一次");
        return result;
    }
}
