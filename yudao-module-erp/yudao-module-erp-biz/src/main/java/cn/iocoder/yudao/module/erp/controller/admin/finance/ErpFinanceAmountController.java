package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceAmountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 财务金额")
@RestController
@RequestMapping("/erp/finance-amount")
@Validated
public class ErpFinanceAmountController {

    @Resource
    private ErpFinanceAmountService financeAmountService;

    @PostMapping("/create")
    @Operation(summary = "创建财务金额")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:create')")
    public CommonResult<Long> createFinanceAmount(@Valid @RequestBody ErpFinanceAmountSaveReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        return success(financeAmountService.createFinanceAmount(createReqVO, currentUsername));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务金额")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:update')")
    public CommonResult<Boolean> updateFinanceAmount(@Valid @RequestBody ErpFinanceAmountSaveReqVO updateReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.updateFinanceAmount(updateReqVO, currentUsername);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务金额")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:delete')")
    public CommonResult<Boolean> deleteFinanceAmount(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.deleteFinanceAmount(ids, currentUsername);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务金额")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:query')")
    public CommonResult<ErpFinanceAmountRespVO> getFinanceAmount(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        ErpFinanceAmountDO financeAmount = financeAmountService.getFinanceAmount(id, currentUsername);
        return success(BeanUtils.toBean(financeAmount, ErpFinanceAmountRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务金额分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:query')")
    public CommonResult<PageResult<ErpFinanceAmountRespVO>> getFinanceAmountPage(@Valid ErpFinanceAmountPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        PageResult<ErpFinanceAmountRespVO> pageResult = financeAmountService.getFinanceAmountVOPage(pageReqVO, currentUsername);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得财务金额列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:query')")
    public CommonResult<List<ErpFinanceAmountRespVO>> getFinanceAmountListByIds(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpFinanceAmountRespVO> list = financeAmountService.getFinanceAmountVOList(ids, currentUsername);
        return success(list);
    }

    @GetMapping("/balance-summary")
    @Operation(summary = "获取当前用户余额汇总")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:query')")
    public CommonResult<ErpFinanceAmountRespVO> getUserBalanceSummary() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        ErpFinanceAmountRespVO summary = financeAmountService.getUserBalanceSummary(currentUsername);
        return success(summary);
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值操作")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:recharge')")
    public CommonResult<Boolean> recharge(
            @RequestParam("channelType") String channelType,
            @RequestParam("amount") BigDecimal amount) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.recharge(currentUsername, channelType, amount);
        return success(true);
    }

    @PostMapping("/recharge-with-images")
    @Operation(summary = "充值操作（带图片和备注）")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:recharge')")
    public CommonResult<Boolean> rechargeWithImages(@Valid @RequestBody ErpFinanceAmountRechargeReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.rechargeWithImages(currentUsername, reqVO.getChannelType(), reqVO.getAmount(),
                reqVO.getCarouselImages(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/init")
    @Operation(summary = "初始化当前用户财务金额记录")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:create')")
    public CommonResult<ErpFinanceAmountRespVO> initUserFinanceAmount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        ErpFinanceAmountDO financeAmount = financeAmountService.initUserFinanceAmount(currentUsername);
        return success(BeanUtils.toBean(financeAmount, ErpFinanceAmountRespVO.class));
    }

    @GetMapping("/channel-balance")
    @Operation(summary = "获取指定渠道的当前余额")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:query')")
    public CommonResult<BigDecimal> getChannelBalance(@RequestParam("channelType") String channelType) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        BigDecimal balance = financeAmountService.getChannelBalance(currentUsername, channelType);
        return success(balance);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核财务金额记录")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:audit')")
    public CommonResult<Boolean> auditFinanceAmount(@Valid @RequestBody ErpFinanceAmountAuditReqVO auditReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.auditFinanceAmount(auditReqVO, currentUsername);
        return success(true);
    }

    @PostMapping("/unaudit")
    @Operation(summary = "反审核财务金额记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:unaudit')")
    public CommonResult<Boolean> unauditFinanceAmount(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeAmountService.unauditFinanceAmount(ids, currentUsername);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出财务金额 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceAmountExcel(@Valid ErpFinanceAmountPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        try {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
            pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
            PageResult<ErpFinanceAmountRespVO> pageResult = financeAmountService.getFinanceAmountVOPage(pageReqVO, currentUsername);
            System.out.println("财务金额导出的数据"+pageResult.getList());
            
            // 转换为导出VO
            List<ErpFinanceAmountExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpFinanceAmountExportVO.class);
            
            // 导出 Excel
            ExcelUtils.write(response, "财务金额.xlsx", "数据", ErpFinanceAmountExportVO.class, exportList);
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("导出财务金额Excel时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得财务金额导入模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手工创建导出 demo
        ErpFinanceAmountImportExcelVO demo = new ErpFinanceAmountImportExcelVO();
        demo.setNo("CWJE202403250001");
        demo.setCarouselImages("http://example.com/image1.jpg");
        demo.setChannelType("微信");
        demo.setAmount(new BigDecimal("100.00"));
        demo.setOperationType(1);
        demo.setBeforeBalance(new BigDecimal("500.00"));
        demo.setAfterBalance(new BigDecimal("600.00"));
        demo.setRemark("充值记录");

        List<ErpFinanceAmountImportExcelVO> list = Arrays.asList(demo);

        // 输出
        ExcelUtils.write(response, "财务金额导入模板.xls", "财务金额列表", ErpFinanceAmountImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入财务金额")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:finance-amount:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpFinanceAmountImportRespVO> importFinanceAmountList(@RequestParam("file") MultipartFile file,
                                                                               @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpFinanceAmountImportExcelVO> list = ExcelUtils.read(inputStream, ErpFinanceAmountImportExcelVO.class);
            System.out.println("前端拿到的数据"+list);
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
            return success(financeAmountService.importFinanceAmountList(list, updateSupport, currentUsername));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }
}
