package com.ths.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 用来标记待下载文件的文件块信息
 * url              ： 下载文件的url
 * serverFilePath   ： 文件保存在服务端的路径（路径+文件名）
 * storeFIlePath    ： 文件保存在客户端的路径（路径+文件名）
 * number           ： 文件快的编号
 * start            ： 文件块的偏移量
 * end              ： 文件块的结束位置
 * content          ： 文件块的内容
 * blockState       ： 文件块的状态
 * @author viruser
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadBlock {

    /**下载文件的url*/
    private String url;
    /**文件保存在服务端的路径（路径+文件名）*/
    private String serverFilePath;
    /**文件保存在客户端的路径（路径+文件名）*/
    private String storeFilePath;
    /**文件块的编号（一个文件一套编号）*/
    private int number;
    /**文件快的开始位置*/
    private int start;
    /**文件快的结束位置*/
    private int end;
    /**文件块的文件内容*/
    private byte[] content;
    /**文件块下载失败次数*/
    private AtomicInteger failCount;
}