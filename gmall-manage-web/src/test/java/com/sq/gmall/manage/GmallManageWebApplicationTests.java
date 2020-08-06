package com.sq.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
        //配置全局连接地址
        String path = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();//配置文件路径
        ClientGlobal.init(path);

        TrackerClient trackerClient = new TrackerClient();
        //获取trackerServer实例
        TrackerServer trackerServer = trackerClient.getConnection();

        StorageClient storageClient = new StorageClient(trackerServer,null);

        String[] uploadInfo = storageClient.upload_file("R:/C盘/ccccc/1122.Jpg", "Jpg", null);

        for (String s : uploadInfo) {
            System.out.println(s);
        }
    }

}
