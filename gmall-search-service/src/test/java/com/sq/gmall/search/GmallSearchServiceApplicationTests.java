package com.sq.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsSearchSkuInfo;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.service.manage.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();







        //用api执行复杂查询
        Search search = new Search.Builder("").addIndex("gmall_pms").addType("pmsSkuInfo").build();

        SearchResult searchResult = jestClient.execute(search);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : searchResult.getHits(PmsSearchSkuInfo.class)) {
            PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
        }
    }

    @Test
    public void put() throws IOException {

        //查询mysql
        List<PmsSkuInfo> skuInfoList = skuService.getAllSku();
        //转化为es数据
        for (PmsSkuInfo skuInfo : skuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(skuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfo.setId(Long.parseLong(skuInfo.getId()));
            //导入es
            Index gmallPms = new Index.Builder(pmsSearchSkuInfo).index("gmall_pms").type("pmsSkuInfo").id(String.valueOf(pmsSearchSkuInfo.getId())).build();
            jestClient.execute(gmallPms);
        }
    }



}
