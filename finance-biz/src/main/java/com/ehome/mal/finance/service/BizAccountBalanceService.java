package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizAccountBalance;
import com.ehome.mal.finance.api.vo.TrialBalanceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BizAccountBalanceService extends IService<BizAccountBalance> {
    List<BizAccountBalance> getBalancesByPeriod(String tenantId, String periodCode);

    boolean updateBalance(Long accountId, String periodCode, java.math.BigDecimal debit, java.math.BigDecimal credit);

    boolean importBeginningBalances(String tenantId, String periodCode, List<BizAccountBalance> balances);

    TrialBalanceVO calculateTrialBalance(String tenantId, String periodCode);

    byte[] generateImportTemplate(String tenantId);

    Map<String, Object> importFromExcel(String tenantId, String periodCode, MultipartFile file);
}
