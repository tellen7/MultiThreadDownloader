package com.laowang.controller;

import com.laowang.utils.FileUtil;
import com.laowang.domain.Result;
import com.laowang.utils.MarkUtils;
import com.laowang.utils.DownloadResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * @author wangyonghao
 */
@Controller
@Slf4j
public class DownloadController {

    /**
     * 功能：下载文件指定位置的byte数组
     */
    @RequestMapping(value = "/download", method = {RequestMethod.GET,RequestMethod.POST},produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] download(@RequestParam String startPosition,
                    @RequestParam String endPosition,
                    @RequestParam String filePath){
        byte[] bytes = null;
        try {
            bytes = FileUtil.getFileBytesByPosition(filePath,Integer.valueOf(startPosition),Integer.valueOf(endPosition));
        } catch (NumberFormatException e) {
            log.error("===>类型转换异常,start:{} end:{}",startPosition,endPosition);
            return MarkUtils.objectToBytes(DownloadResultUtils.error("待下载文件位置转换成number失败"));
        }
        if (bytes == null) {
            log.info("===> filePath:{} start:{} end:{}",filePath,startPosition,endPosition);
            // 文件不存在或则文件IO异常
            return MarkUtils.objectToBytes(DownloadResultUtils.error("文件不存在或则文件IO异常"));
        }
        log.info("===> filePath:{} start:{} end:{}",filePath,startPosition,endPosition);
        return MarkUtils.objectToBytes(DownloadResultUtils.success(bytes));
    }

    /**
     * 标记某个用户已经下载过某文件
     */
    @PostMapping("/markDownloaded")
    public @ResponseBody
    Result markDownloaded(@RequestParam String serverFile, @RequestParam String token){
        //do something to change the map in fetchDataList()
        log.info("文件{}，用户{}",serverFile,token);
        //TODO 根据token 获取用户信息，组合用户信息和serverFile，插入数据库，表示当前用户已经下载过文件
        return DownloadResultUtils.success();
    }

    /**
     *  获取待下载文件列表（文件位置和长度）
     */
    @PostMapping("/fetchDataList")
    public @ResponseBody
    Result fetchDataList(){
        //filePath<---->size
        Map<String, Object> list = new HashMap<>(4);
        log.info("fetch data list.");
        list.put("C:\\Users\\viruser.v-desktop\\Downloads\\Head+First+Java+中文高清版.pdf", FileUtil.getFileLength("C:\\Users\\viruser.v-desktop\\Downloads\\Head+First+Java+中文高清版.pdf"));
        list.put("C:\\Users\\viruser.v-desktop\\Downloads\\mergesort.gif", FileUtil.getFileLength("C:\\Users\\viruser.v-desktop\\Downloads\\mergesort.gif"));
        list.put("C:\\Users\\viruser.v-desktop\\Downloads\\JAVA编程思想第四版.pdf", FileUtil.getFileLength("C:\\Users\\viruser.v-desktop\\Downloads\\JAVA编程思想第四版.pdf"));
        return DownloadResultUtils.success(list);
    }



    /**
     * 废弃
     * 功能： 返回文件的大小，下载文件的下载url；或则文件不存在
     */
    @GetMapping(value = "/size",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Result getSizeOfFile(@RequestParam String filePath){
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()){
            return DownloadResultUtils.error("文件不存在或则请求的是一个文件夹");
        }
        long size = file.length();
        log.info("===>filePath: {} 长度：{}",filePath,size);
        return DownloadResultUtils.success(size);
    }

}
