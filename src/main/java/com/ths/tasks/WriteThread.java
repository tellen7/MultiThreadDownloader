package com.ths.tasks;

import com.ths.domain.ControlBean;
import com.ths.domain.DownloadBlock;
import com.ths.domain.Result;
import com.ths.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangyonghao
 */
@Slf4j
public class WriteThread implements Runnable {

    String token = "this is a token";
    String url;

    @Value("${multiDownload.customer.markDownloadedUrl}")
    public void setMarkDownloaded(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DownloadBlock block = ControlBean.successBlocks.take();
                doWrite(block);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件块写入成功，清空引用，判断写入文件块数是否达到目标数，达到则清空filesCurrent中对应文件的引用
     * 写入文件失败，I/O异常，则把文件块放回到successBlocks，等下一次再写
     *
     * @param block 文件块
     */
    public void doWrite(DownloadBlock block) {
        //如果写入文件成功
        if (FileUtils.storeToFile(block)) {
            log.info("===>成功写入{}【No.{}块文件块】", block.getStoreFilePath(), block.getNumber());
            int number = ControlBean.filesCurrent.get(block.getServerFilePath()).addAndGet(1);
            if (ControlBean.filesTotal.get(block.getServerFilePath()).get() == number) {
                FileUtils.rename(block.getStoreFilePath());
                log.info("====>文件{}", block.getServerFilePath() + "下载完成...");
            }
        } else {
            //可能不发生
            log.info("===>失败写入{}【No.{}块文件块】", block.getStoreFilePath(), block.getNumber());
            ControlBean.successBlocks.add(block);
        }
    }


    public boolean markFileDownloaded(DownloadBlock block) {
        //设置请求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<String, String>();
        //设置token
        param.add("token", token);
        param.add("serverFile", block.getServerFilePath());
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param, headers);

        try {
            ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Result.class);
            if (response.getBody().getCode() == Result.StatusCode.SUCCESS) {
                ControlBean.filesCurrent.remove(block.getServerFilePath());
                ControlBean.filesTotal.remove(block.getServerFilePath());
                return true;
            } else {
                return false;
            }
        } catch (RestClientException e) {
            log.error("===>标记服务端下载文件完成的请求获取响应失败 {}" + e);
            return false;
        }
    }
}
