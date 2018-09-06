package com.laowang.utils;

import com.laowang.domain.Result;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

public class FileUtilsTest {
//    public static void main(String[] args) {
//        byte[] bytes = FileUtil.getFileBytesByPosition("D:\\123.doc",0,233984);
//        System.out.println(bytes);
//        byte[] result = FileUtil.storeToFile("D:\\temp\\temp\\4567.doc",0,233984,bytes);
//    }
public static void main(String[] args) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    //headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    ArrayList<MediaType> types = new ArrayList<MediaType>();
    types.add(MediaType.APPLICATION_OCTET_STREAM);
    headers.setAccept(types);
    headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
    MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
    //设置token
    param.add("token","123");
    //设置数据块相关
    param.add("filePath","D:\\\\123.doc");
    param.add("startPosition", "0");
    param.add("endPosition", "233984");

    HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param,headers);

//    ResponseEntity<byte[]> response = restTemplate.exchange("http://localhost:2568/multiDownload/download", HttpMethod.GET, httpEntity, byte[].class);;
    ResponseEntity<byte[]> r = restTemplate.postForEntity("http://localhost:2568/multiDownload/download",httpEntity,byte[].class);
    byte[] bytes = r.getBody();
    Result result = (Result) MarkUtils.bytesToObject(bytes);
    if (result.getCode() == Result.StatusCode.SUCCESS) {
        //缓存下载内容
        System.out.println(result.getData());
    }
}
}