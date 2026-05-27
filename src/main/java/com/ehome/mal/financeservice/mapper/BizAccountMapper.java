package com.ehome.mal.financeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ehome.mal.financeservice.entity.BizAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BizAccountMapper extends BaseMapper<BizAccount> {
    List<BizAccount> selectTreeList(String tenantId);
}
