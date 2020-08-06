package com.sq.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @title: PmsUploadUtil
 * @Description 上传媒资工具类
 * @Author sq
 * @Date: 2020/7/31 17:14
 * @Version 1.0
 */
public class PmsUploadUtil {
    /**
     * 上传图片到服务
     * @param multipartFile 图片数据
     * @return 图片地址
     */
    public static String uploadImage(MultipartFile multipartFile){
        //配置全局连接地址
        String path = PmsUploadUtil.class.getResource("/tracker.conf").getPath();//配置文件路径
        StringBuilder imgUrl = new StringBuilder("http://192.168.75.130");
        try {
             ClientGlobal.init(path);

            TrackerClient trackerClient = new TrackerClient();
            //获取trackerServer实例
            TrackerServer trackerServer = trackerClient.getConnection();

            StorageClient storageClient = new StorageClient(trackerServer,null);
            //获取上传图片二进制文件
            byte[] bytes = multipartFile.getBytes();
            //获取上传图片的名称
            String originalFilename = multipartFile.getOriginalFilename();
            //获取最后一个点位置
            int i = originalFilename.lastIndexOf(".");
            //获取扩展名
            String extension = originalFilename.substring(i + 1);
            String[] uploadInfo = storageClient.upload_file(bytes, extension, null);
            for (String s : uploadInfo) {
                imgUrl.append("/").append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        return imgUrl.toString();
    }
}
