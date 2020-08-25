package com.sq.gmall.service.search;

import com.sq.gmall.bean.PmsSearchParam;
import com.sq.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * @Description
 * @Author sq
 * Created by sq on 2020/8/6 21:09
 */
public interface SearchService {
    /**
     * 根据条件查询es中数据并返回
     * @param pmsSearchParam
     * @return
     */
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
