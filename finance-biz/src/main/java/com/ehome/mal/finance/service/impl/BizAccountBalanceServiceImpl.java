package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccount;
import com.ehome.mal.finance.api.entity.BizAccountBalance;
import com.ehome.mal.finance.mapper.BizAccountBalanceMapper;
import com.ehome.mal.finance.mapper.BizAccountMapper;
import com.ehome.mal.finance.service.BizAccountBalanceService;
import com.ehome.mal.finance.api.vo.TrialBalanceVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BizAccountBalanceServiceImpl extends ServiceImpl<BizAccountBalanceMapper, BizAccountBalance> implements BizAccountBalanceService {

    private final BizAccountMapper accountMapper;

    @Override
    public List<BizAccountBalance> getBalancesByPeriod(String tenantId, String periodCode) {
        LambdaQueryWrapper<BizAccountBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountBalance::getTenantId, tenantId)
                .eq(BizAccountBalance::getPeriodCode, periodCode);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBalance(Long accountId, String periodCode, BigDecimal debit, BigDecimal credit) {
        LambdaQueryWrapper<BizAccountBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountBalance::getAccountId, accountId)
                .eq(BizAccountBalance::getPeriodCode, periodCode);
        BizAccountBalance balance = getOne(wrapper);

        if (balance == null) {
            balance = new BizAccountBalance();
            balance.setAccountId(accountId);
            balance.setPeriodCode(periodCode);
            balance.setBeginningBalance(BigDecimal.ZERO);
            balance.setPeriodDebit(debit != null ? debit : BigDecimal.ZERO);
            balance.setPeriodCredit(credit != null ? credit : BigDecimal.ZERO);
            balance.setYearDebit(debit != null ? debit : BigDecimal.ZERO);
            balance.setYearCredit(credit != null ? credit : BigDecimal.ZERO);
            balance.setEndingBalance(BigDecimal.ZERO);
            balance.setCreateTime(Instant.now());
            return save(balance);
        } else {
            if (debit != null) {
                balance.setPeriodDebit(balance.getPeriodDebit().add(debit));
                balance.setYearDebit(balance.getYearDebit().add(debit));
            }
            if (credit != null) {
                balance.setPeriodCredit(balance.getPeriodCredit().add(credit));
                balance.setYearCredit(balance.getYearCredit().add(credit));
            }
            balance.setUpdateTime(Instant.now());
            return updateById(balance);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importBeginningBalances(String tenantId, String periodCode, List<BizAccountBalance> balances) {
        for (BizAccountBalance balance : balances) {
            balance.setTenantId(tenantId);
            balance.setPeriodCode(periodCode);
            balance.setPeriodDebit(BigDecimal.ZERO);
            balance.setPeriodCredit(BigDecimal.ZERO);
            balance.setYearDebit(BigDecimal.ZERO);
            balance.setYearCredit(BigDecimal.ZERO);
            if (balance.getBeginningBalance() == null) {
                balance.setBeginningBalance(BigDecimal.ZERO);
            }
            balance.setEndingBalance(balance.getBeginningBalance());
            balance.setCreateTime(Instant.now());
            }
        return saveBatch(balances);
    }

    @Override
    public TrialBalanceVO calculateTrialBalance(String tenantId, String periodCode) {
        List<BizAccountBalance> balances = getBalancesByPeriod(tenantId, periodCode);

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<String> differences = new ArrayList<>();

        for (BizAccountBalance balance : balances) {
            totalDebit = totalDebit.add(balance.getYearDebit());
            totalCredit = totalCredit.add(balance.getYearCredit());
            if (balance.getYearDebit().compareTo(balance.getYearCredit()) != 0
                    && balance.getBeginningBalance() != null
                    && balance.getBeginningBalance().compareTo(BigDecimal.ZERO) != 0) {
                differences.add("科目ID:" + balance.getAccountId() + " 借方:" + balance.getYearDebit() + " 贷方:" + balance.getYearCredit());
            }
        }

        TrialBalanceVO vo = new TrialBalanceVO();
        vo.setBalanced(totalDebit.compareTo(totalCredit) == 0);
        vo.setTotalDebit(totalDebit);
        vo.setTotalCredit(totalCredit);
        vo.setDifferences(differences);
        return vo;
    }

    @Override
    public byte[] generateImportTemplate(String tenantId) {
        List<BizAccount> accounts = accountMapper.selectList(
                new LambdaQueryWrapper<BizAccount>()
                        .eq(BizAccount::getTenantId, tenantId)
                        .eq(BizAccount::getIsEnabled, 1)
                        .orderByAsc(BizAccount::getAccountCode)
        );

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("期初余额导入模板");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"科目编码", "科目名称", "余额方向(借/贷)", "期初余额"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 1;
            for (BizAccount account : accounts) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(account.getAccountCode());
                c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1);
                c1.setCellValue(account.getAccountName());
                c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2);
                c2.setCellValue(account.getDirection() != null ? account.getDirection() : "借");
                c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3);
                c3.setCellValue(0);
                c3.setCellStyle(dataStyle);
            }

            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);

            Sheet instrSheet = workbook.createSheet("填写说明");
            instrSheet.createRow(0).createCell(0).setCellValue("期初余额导入模板填写说明");
            instrSheet.createRow(1).createCell(0).setCellValue("1. 请在"期初余额导入模板"sheet中填写各科目的期初余额");
            instrSheet.createRow(2).createCell(0).setCellValue("2. 科目编码和科目名称已预填，请勿修改");
            instrSheet.createRow(3).createCell(0).setCellValue("3. 余额方向：借方科目填"借"，贷方科目填"贷"");
            instrSheet.createRow(4).createCell(0).setCellValue("4. 期初余额：填写正数，系统根据余额方向自动判断借贷");
            instrSheet.createRow(5).createCell(0).setCellValue("5. 导入前系统自动校验试算平衡（借方合计=贷方合计），不平衡将拒绝导入");
            instrSheet.createRow(6).createCell(0).setCellValue("6. 不需要导入的科目，期初余额填0或留空即可");
            instrSheet.setColumnWidth(0, 18000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成Excel模板失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(String tenantId, String periodCode, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持Excel文件（.xlsx或.xls）");
        }

        List<BizAccount> allAccounts = accountMapper.selectList(
                new LambdaQueryWrapper<BizAccount>()
                        .eq(BizAccount::getTenantId, tenantId)
                        .eq(BizAccount::getIsEnabled, 1)
        );
        Map<String, BizAccount> accountByCode = new HashMap<>();
        for (BizAccount acct : allAccounts) {
            accountByCode.put(acct.getAccountCode(), acct);
        }

        List<BizAccountBalance> balances = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel文件无数据sheet");
            }

            int lastRow = sheet.getLastRowNum();
            if (lastRow < 1) {
                throw new IllegalArgumentException("Excel文件无数据行");
            }

            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String accountCode = getCellStringValue(row.getCell(0));
                String accountName = getCellStringValue(row.getCell(1));
                String direction = getCellStringValue(row.getCell(2));
                BigDecimal amount = getCellBigDecimalValue(row.getCell(3));

                if ((accountCode == null || accountCode.isEmpty()) && amount == null) {
                    continue;
                }

                if (accountCode == null || accountCode.isEmpty()) {
                    errors.add("第" + (i + 1) + "行：科目编码不能为空");
                    errorCount++;
                    continue;
                }

                BizAccount account = accountByCode.get(accountCode);
                if (account == null) {
                    errors.add("第" + (i + 1) + "行：科目编码" + accountCode + "不存在或已禁用");
                    errorCount++;
                    continue;
                }

                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }

                if (direction == null || direction.isEmpty()) {
                    direction = account.getDirection() != null ? account.getDirection() : "借";
                }

                BizAccountBalance balance = new BizAccountBalance();
                balance.setTenantId(tenantId);
                balance.setAccountId(account.getId());
                balance.setPeriodCode(periodCode);
                balance.setPeriodDebit(BigDecimal.ZERO);
                balance.setPeriodCredit(BigDecimal.ZERO);
                balance.setYearDebit(BigDecimal.ZERO);
                balance.setYearCredit(BigDecimal.ZERO);
                balance.setCreateTime(Instant.now());

                if ("贷".equals(direction)) {
                    balance.setBeginningBalance(amount.negate());
                    totalCredit = totalCredit.add(amount);
                } else {
                    balance.setBeginningBalance(amount);
                    totalDebit = totalDebit.add(amount);
                }
                balance.setEndingBalance(balance.getBeginningBalance());

                balances.add(balance);
                successCount++;
            }

            if (balances.isEmpty()) {
                throw new IllegalArgumentException("Excel文件中无有效数据行");
            }

            boolean balanced = totalDebit.compareTo(totalCredit) == 0;
            if (!balanced) {
                errors.add("试算不平衡：借方合计=" + totalDebit + "，贷方合计=" + totalCredit + "，差异=" + totalDebit.subtract(totalCredit));
                errorCount++;
            }

            if (!errors.isEmpty() && !balanced) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("successCount", 0);
                result.put("errorCount", errorCount);
                result.put("errors", errors);
                result.put("totalDebit", totalDebit);
                result.put("totalCredit", totalCredit);
                return result;
            }

            LambdaQueryWrapper<BizAccountBalance> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(BizAccountBalance::getTenantId, tenantId)
                    .eq(BizAccountBalance::getPeriodCode, periodCode);
            remove(deleteWrapper);

            saveBatch(balances);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errors", errors);
            result.put("totalDebit", totalDebit);
            result.put("totalCredit", totalCredit);
            return result;

        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                if (v == Math.floor(v) && !Double.isInfinite(v)) {
                    yield String.valueOf((long) v);
                }
                yield String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); }
                catch (Exception e) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> null;
        };
    }

    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty()) yield null;
                try { yield new BigDecimal(s); }
                catch (NumberFormatException e) { yield null; }
            }
            case FORMULA -> {
                try { yield BigDecimal.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { yield null; }
            }
            case BLANK -> null;
            default -> null;
        };
    }
}
