package com.laowang.tasks;

import com.laowang.domain.ControlBean;
import com.laowang.domain.Result;
import com.laowang.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wangyonghao
 */
@Slf4j
@Component
public class DropStoreThread implements Runnable {
    private String token="this is a token";
    private String url = "http://localhost:2568/multiDownload/markDownloaded";

    @Value("${multiDownload.customer.storeDir}")
    private String storeDir;

    @Override
    public void run() {
        while(true){
            //一直遍历
            if (ControlBean.filesTotal.size()>0) {
                List files = new LinkedList();
                //遍历的时候有新文件添加进来，导致nullPointer？？？
                ControlBean.filesTotal.forEach((key,value)->{
                    //download done to store file and communicate with server
                    //System.out.println(ControlBean.filesCurrent.get(key).get()+value.get());
                    if (key!=null&&value!=null&&ControlBean.filesCurrent.get(key).get() == value.get()){
                        String fileName = key.substring(key.lastIndexOf('/') + 1);
                        String storeFile = storeDir + fileName + ".temp";
                        FileUtil.rename(storeFile);
                        log.info("===>文件{}下载完成...",key);
                        if (markFileDownloaded(key)){
                            files.add(key);
                            log.info("===>标记服务端文件{}完成...",key);
                        } else{
                            log.info("===>文件{} 已经下载完成，标记服务端失败",key);
                        }
                    }
                    //如果  下载失败丢弃的文件块数 + 下载成功的文件块数 == 文件总块数
                    if (ControlBean.dropBlock.get(key).get()!=0 && (ControlBean.filesCurrent.get(key).get()+ControlBean.dropBlock.get(key).get()) == value.get()){
                        String fileName = key.substring(key.lastIndexOf('/') + 1);
                        String storeFile = storeDir + fileName + ".temp";
                        File file = new File(storeFile);
                        if (file.exists()) {
                            file.delete();
                        }
                        files.add(key);
                        log.info("===>文件{}下载失败 ，已处理完成",key);
                    }
                });
                // 删除处理完成的文件记录信息
                for (int i = 0; i < files.size(); i++) {
                    ControlBean.dropBlock.remove(files.get(i));
                    ControlBean.filesCurrent.remove(files.get(i));
                    ControlBean.filesTotal.remove(files.get(i));
                }
            }
        }
    }

    public boolean markFileDownloaded(String filePath ){
        //设置请求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        //设置token
        param.add("token",token);
        param.add("serverFile",filePath);
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param,headers);

        try {
            ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.POST,httpEntity, Result.class);
            if (response.getBody().getCode() == Result.StatusCode.SUCCESS){
                return true;
            }else{
                return false;
            }
        } catch (RestClientException e) {
            log.error("===>标记服务端下载文件完成的请求获取响应失败 {}"+e);
            return false;
        }
    }
}
