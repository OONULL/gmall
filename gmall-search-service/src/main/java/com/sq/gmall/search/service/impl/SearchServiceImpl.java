package com.sq.gmall.search.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.sq.gmall.bean.PmsSearchParam;
import com.sq.gmall.bean.PmsSearchSkuInfo;
import com.sq.gmall.service.search.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @title: SearchServiceImpl
 * @Description
 * @Author sq
 * @Date: 2020/8/6 21:09
 * @Version 1.0
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    /**
     * 根据条件查询es中数据并返回
     * @param pmsSearchParam
     * @return
     */
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {

       String dsl = getDsl(pmsSearchParam);
        Search search = new Search.Builder(dsl).addIndex("gmall_pms").addType("pmsSkuInfo").build();
        SearchResult executer = null;
        try {
            executer = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<PmsSearchSkuInfo> searchSkuInfoList = new ArrayList<>();
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = executer.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo searchSkuInfo = hit.source;
            //高亮
            Map<String, List<String>> highlight = hit.highlight;
            if(!org.springframework.util.CollectionUtils.isEmpty(highlight)){
                String skuName = highlight.get("skuName").get(0);
                searchSkuInfo.setSkuName(skuName);
            }

            searchSkuInfoList.add(searchSkuInfo);


        }
        return searchSkuInfoList;
    }

    private String getDsl(PmsSearchParam pmsSearchParam) {
        String[] valueIds = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter
        //判断是否有3级类目
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //判断是否有平台属性数据
        if(valueIds !=null && valueIds.length>0){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //must
        //判断是否有搜索关键字
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //query(查询条件)
        searchSourceBuilder.query(boolQueryBuilder);

        //from(从哪里开始)
        searchSourceBuilder.from(0);

        //size(返回结果个数)
        searchSourceBuilder.size(20);

        //highlight(高亮)
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuName");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        //sort(排序)
        searchSourceBuilder.sort("id", SortOrder.DESC);

        //aggs(聚合)
        TermsBuilder name = AggregationBuilders.terms("name");
        

        return searchSourceBuilder.toString();
    }
}
