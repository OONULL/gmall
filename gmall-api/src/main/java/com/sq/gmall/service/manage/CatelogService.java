package com.sq.gmall.service.manage;

import com.sq.gmall.bean.PmsBaseCatalog1;
import com.sq.gmall.bean.PmsBaseCatalog2;
import com.sq.gmall.bean.PmsBaseCatalog3;

import java.util.List;

/**
 * @Description 商品类目service接口
 * @Author sq
 * Created by sq on 2020/7/28 19:54
 */
public interface CatelogService {
    /**
     * 获取商品1级类目
     * @return
     */
    List<PmsBaseCatalog1> getCatalog1();
    /**
     * 获取商品2级类目
     * @return
     * @param catalog1Id
     */
    List<PmsBaseCatalog2> getCatalog2(Integer catalog1Id);
    /**
     * 获取商品3级类目
     * @return
     */
    List<PmsBaseCatalog3> getCatalog3(Integer catalog2Id);
}
