package com.ths.domain;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 控制bean，用来表示提示文件下载成功，用来处理成功关闭程序
 * @author wangyonghao
 */
public class ControlBean {
    /**每一个文件的存储名和总块数的映射关系*/
    public static ConcurrentHashMap<String,AtomicInteger> filesTotal = new ConcurrentHashMap<String,AtomicInteger>();
    /**当前每一个文件的存储名和下载成功数的映射关系*/
    public static ConcurrentHashMap<String,AtomicInteger> filesCurrent = new ConcurrentHashMap<String,AtomicInteger>();
    /**每一个文件的丢弃文件块数*/
    public static ConcurrentHashMap<String,AtomicInteger> dropBlock = new ConcurrentHashMap<String,AtomicInteger>();
    /**待下载文件块队列*/
    public static LinkedBlockingQueue<DownloadBlock> waitBlocks = new LinkedBlockingQueue<DownloadBlock>();
    /**成功下载文件块队列*/
    public static LinkedBlockingQueue<DownloadBlock> successBlocks = new LinkedBlockingQueue<DownloadBlock>();
    /**失败下载文件块队列*/
    public static LinkedBlockingQueue<DownloadBlock> loseBlocks = new LinkedBlockingQueue<DownloadBlock>();
    /**继续下载标志（中断下载的变量）*/
    public static volatile boolean continueDownload = true;
}
