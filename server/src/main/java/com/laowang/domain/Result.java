package com.laowang.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wangyonghao
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable{

    private static final long serialVersionUID = 6624812054047777574L;

    /**返回信息说明*/
    private String msg;
    /**返回状态码*/
    private StatusCode code;
    /**返回数据*/
    private T data;


    public enum StatusCode{
        /**失败*/
        FAIL,
        /**成功*/
        SUCCESS,
        /**再次请求*/
        AGAIN
    }
}
