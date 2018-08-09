package com.laowang.tasks;

import com.laowang.domain.DownloadBlock;
import com.laowang.domain.ControlBean;
import com.laowang.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangyonghao
 */
@Slf4j
@Component
public class Downloader {

    /**20MB*/
    private static int blockSize=1000*1024*20;
    private static String url="http://0.0.0.0/multiDownload/download";
    private static String storeDir;

    @Value("${multiDownload.customer.storeDir}")
    public void setStoreDir(String dir){
        storeDir = dir;
    }


    {//第一次加载类，会开启一个reTask线程，一个写线程，3个下载线程,一个善后线程
        new Thread(new ReTaskThread(),"ReTaskThread").start();
        new Thread(new WriteThread(),"writeThread").start();
        new Thread(new DownloadThread(),"downloadThread-0").start();
        new Thread(new DownloadThread(),"downloadThread-1").start();
        new Thread(new DownloadThread(),"downloadThread-2").start();
        new Thread(new DropStoreThread(),"dropStoreThread").start();
    }

    /**
     * 下载器: 封装ControlBean
     * @param serverFilePath 要下载的文件在服务端的路径
     * @param length         待下载文件的长度
     * @return 创建下载任务的状态
     */
    public static String download(String serverFilePath, Integer length) {
        if (StringUtils.isEmpty(serverFilePath)) {
            return "msg:serverFilePath为空";
        }

        log.info("===>待下载文件总长度： " + length);
        String fileName = serverFilePath.substring(serverFilePath.lastIndexOf('/') + 1);
        String storeFile = storeDir + fileName + ".temp";
        File file = new File(storeFile);
        //2.保存文件不存在的话，创建它
        if (!file.exists() || !file.isFile()) {
            FileUtils.createFile(storeFile);
        }

        //3.文件总块数记录,文件块初始化
        int fileNumber = createTaskQueue(length, url, serverFilePath, storeFile);
        //下面两个变量用来标记文件下载完成，清空这个文件块的所占用内存
        //这个量不能被改变，用来记忆用户下载记录的
        ControlBean.filesTotal.put(serverFilePath, new AtomicInteger(fileNumber));
        ControlBean.filesCurrent.put(serverFilePath, new AtomicInteger(0));
        ControlBean.dropBlock.put(serverFilePath, new AtomicInteger(0));

        return "ok";
    }

    /**
     * 创建下载任务的队列
     * @param size           待下载文件总长度
     * @param url
     * @param serverFilePath
     * @param storeFile
     * @return               分割好的文件块数
     */
    private static int createTaskQueue(int size, String url, String serverFilePath, String storeFile) {
        //1.1这里直接计算出需要被划分多少个文件块
        int blocks = 0;
        if (size <= blockSize) {
            blocks = 1;
        } else if (size % blockSize == 0) {
            blocks = size / blockSize;
        } else {
            blocks = size / blockSize + 1;
        }
        //1.3创建文件块，并初始化.文件块从0开始
        for (int i = 0; i < blocks; i++) {
            DownloadBlock newBlock = new DownloadBlock();
            if (i == (blocks - 1)) {
                newBlock.setEnd(size);
            } else {
                newBlock.setEnd((i + 1) * blockSize);
            }
            newBlock.setStart(i * blockSize);
            newBlock.setNumber(i + 1);
            newBlock.setUrl(url);
            newBlock.setServerFilePath(serverFilePath);
            newBlock.setStoreFilePath(storeFile);
            newBlock.setFailCount(new AtomicInteger(0));
            ControlBean.waitBlocks.add(newBlock);
        }
        //测试,加日志
        log.info("===>文件分快完成,一共{}块", blocks);
        return blocks;
    }
}
