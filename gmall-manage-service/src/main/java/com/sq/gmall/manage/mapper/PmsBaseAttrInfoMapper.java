package com.sq.gmall.manage.mapper;

import com.sq.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description 类目属性表mapper
 * @Author sq
 * Created by sq on 2020/7/28 21:43
 */
public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    /**
     * 根据valueId查询属性列表
     * @param valueIds
     * @return
     */
    List<PmsBaseAttrInfo> getAttrValueListByBalueId(@Param("valueIds")String valueIds);
}
