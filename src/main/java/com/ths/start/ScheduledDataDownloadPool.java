package com.ths.start;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ths.domain.ControlBean;
import com.ths.domain.Result;
import com.ths.tasks.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author wangyonghao
 */
@Slf4j
@Component
public class ScheduledDataDownloadPool extends Thread implements CommandLineRunner {

    @Value("${multiDownload.customer.dataListUrl}")
    private String dataListUrl;
    @Value("${multiDownload.customer.openDownload}")
    private boolean openDownload;

    private String token="this is a token";

    @Autowired
    RestTemplate restTemplate;


    private final static ScheduledThreadPoolExecutor SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("dataDownloadPool-%d").setDaemon(true).build());

    @Override
    public void run(String... args) throws Exception {
        this.start();
    }

    @Override
    public void run() {
        //0.开启定时下载功能
        if (openDownload) {
            SCHEDULER.scheduleWithFixedDelay(() -> {
                try {
                    //1.拉取待下载文件的list
                    Map<String, Integer> map = getList(token);
                    //2.过滤出待下载的文件去下载
                    map.forEach((key, value) -> {
                        if (!ControlBean.filesTotal.containsKey(key)) {
                            //2.1下载过的文件在列表中不存在
                            log.info("===> start download:{} length:{}", key, value);
                            //2.2创建下载任务
                            String msg = Downloader.download(key, value);
                            //2.3创建下载任务结果日志
                            if ("ok".equals(msg)) {
                                log.info("===>创建下载{}任务完成", key);
                            } else {
                                log.error("===>创建下载{}任务失败; {}", key, msg);
                            }
                        } else {
                            //2.1待下载文件已经被下载过了，日志记录
                            log.info("===>文件{}正在下载中", key);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("===>下载文件线程池异常");
                }
                //20秒拉取一次待下载文件
            }, 10, 20, TimeUnit.SECONDS);
        } else {
            log.info("===>没有开启下载功能");
        }
    }

    /**
     * 拉取待下载文件的文件列表
     * @param token 用户标示
     * @return Map<String   ,   Integer>（服务器文件名，文件长度）
     */
    public Map<String, Integer> getList(String token) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();
        //设置token
        param.add("token", token);
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param, headers);
        ResponseEntity<Result> response = null;
        try {
            //可能会出现请求异常
            response = restTemplate.exchange(dataListUrl, HttpMethod.POST, httpEntity, Result.class);
            //下载成功,反序列化，判断是否下载成功
            Result result = response.getBody();
            if (result.getCode() == Result.StatusCode.SUCCESS) {
                return (Map<String, Integer>) result.getData();
            } else {
                log.error("===>获取待下载文件列表失败...");
                return null;
            }
        } catch (RestClientException e) {
            log.info("===>网络异常，获取待下载文件失败...", e);
            return null;
        }
    }
}
