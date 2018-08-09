package com.laowang.utils;

import com.laowang.domain.DownloadBlock;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author wangyonghao
 */
@Slf4j
public class FileUtils {
    /**
     * 创建文件
     * @param filePath
     * @return
     * @Exception SecurityException
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        //文件不存在，检查文件夹是否存在，不存在递归创建。在创建文件，返回结果
        if (!file.isFile()) {
            if (file.getParent() != null) {
                new File(file.getParent()).mkdirs();
            }
            try {
                return file.createNewFile() && file.exists();
            } catch (IOException e) {
                if (file.exists()) {
                    return true;
                }
                log.error("===>文件创建异常");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean rename(String filePath){
        File temp = new File(filePath);
        File goal = new File(filePath.replace(".temp",""));
        if (!goal.exists()){
            temp.renameTo(goal);
        }else{
            filePath = filePath.replaceAll(".temp","");
            String dir = filePath.substring(0,filePath.lastIndexOf("\\")+1);
            String name = filePath.substring(filePath.lastIndexOf("\\")+1);
            for (int i = 1; i < 20; i++) {
                filePath = dir + "("+i+")" + name;
                System.out.println(filePath);
                if (!new File(filePath).exists()){
                    break;
                }
            }
            temp.renameTo(new File(filePath));
        }
        return true;
    }

    /**
     * 保存字节数组到随机读写文件
     * @param block
     */
    public static boolean storeToFile(DownloadBlock block) {
        try {
            RandomAccessFile file = new RandomAccessFile(new File(block.getStoreFilePath()), "rwd");
            file.seek(block.getStart());
            file.write(block.getContent(), 0, (block.getEnd() - block.getStart()));
            file.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
