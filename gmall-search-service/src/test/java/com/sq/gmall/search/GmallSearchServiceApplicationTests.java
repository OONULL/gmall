package com.sq.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsSearchSkuInfo;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.service.manage.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    private SkuService skuService;
    @Autowired
    private JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

        //查询mysql
        List<PmsSkuInfo> skuInfoList = skuService.getAllSku();
        //转化为es数据
        for (PmsSkuInfo skuInfo : skuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(skuInfo,pmsSearchSkuInfo);
            //导入es
            Index gmallPms = new Index.Builder(pmsSearchSkuInfo).index("gmall_pms").type("pmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(gmallPms);
        }
    }

}
