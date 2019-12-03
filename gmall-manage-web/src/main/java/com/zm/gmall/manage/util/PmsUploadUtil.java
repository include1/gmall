package com.zm.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String uploaadImage(MultipartFile multipartFile){
        String imgUrl = "http://10.35.198.193";
        //上传服务器代码
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();//获取配置文件路劲
        try {
            ClientGlobal.init(tracker);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageClient storageClient = new StorageClient(trackerServer,null);

        try {
            byte[] bytes = multipartFile.getBytes();//获取上传对象
            //获取文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            System.out.println(originalFilename);
            int i =originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i+1);
            String [] uploadInfos= storageClient.upload_file(bytes,extName,null);

            for(String info : uploadInfos){
                imgUrl += "/"+info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgUrl;
    }
}
