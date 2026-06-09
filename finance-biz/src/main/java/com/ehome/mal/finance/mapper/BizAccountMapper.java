package com.ehome.mal.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ehome.mal.finance.api.entity.BizAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BizAccountMapper extends BaseMapper<BizAccount> {
    List<BizAccount> selectTreeList(String tenantId);

    List<BizAccount> selectByAccountType(String tenantId, String accountType);
}
