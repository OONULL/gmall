package com.sq.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.sq.gmall.bean.PmsBaseCatalog1;
import com.sq.gmall.bean.PmsBaseCatalog2;
import com.sq.gmall.bean.PmsBaseCatalog3;
import com.sq.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.sq.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.sq.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.sq.gmall.service.manage.CatelogService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @title: CatelogServiceImpl
 * @Description 商品2级类目mapper
 * @Author sq
 * @Date: 2020/7/28 19:55
 * @Version 1.0
 */
@Service
public class CatelogServiceImpl implements CatelogService {

    @Autowired
    private PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    private PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    private PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    /**
     * 获取商品1级类目
     * @return
     */
    @Override
    public List<PmsBaseCatalog1> getCatalog1() {

        return pmsBaseCatalog1Mapper.selectAll();
    }
    /**
     * 获取商品2级类目
     * @return
     * @param catalog1Id
     */
    @Override
    public List<PmsBaseCatalog2> getCatalog2(Integer catalog1Id) {
        Example example = new Example(PmsBaseCatalog2.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("catalog1Id",catalog1Id);
        return pmsBaseCatalog2Mapper.selectByExample(example);
    }
    /**
     * 获取商品3级类目
     * @return
     */
    @Override
    public List<PmsBaseCatalog3> getCatalog3(Integer catalog2Id) {
        Example example = new Example(PmsBaseCatalog3.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("catalog2Id",catalog2Id);
        return pmsBaseCatalog3Mapper.selectByExample(example);
    }
}
