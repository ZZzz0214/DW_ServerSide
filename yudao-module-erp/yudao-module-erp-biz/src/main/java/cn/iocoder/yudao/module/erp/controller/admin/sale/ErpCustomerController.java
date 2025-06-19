package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
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
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 客户")
@RestController
@RequestMapping("/erp/customer")
@Validated
public class ErpCustomerController {

    @Resource
    private ErpCustomerService customerService;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('erp:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody ErpCustomerSaveReqVO createReqVO) {
        return success(customerService.createCustomer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('erp:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody ErpCustomerSaveReqVO updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:customer:delete')")
    public CommonResult<Boolean> deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:customer:query')")
    public CommonResult<ErpCustomerRespVO> getCustomer(@RequestParam("id") Long id) {
        ErpCustomerDO customer = customerService.getCustomer(id);
        return success(BeanUtils.toBean(customer, ErpCustomerRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('erp:customer:query')")
    public CommonResult<PageResult<ErpCustomerRespVO>> getCustomerPage(@Valid ErpCustomerPageReqVO pageReqVO) {
        PageResult<ErpCustomerDO> pageResult = customerService.getCustomerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpCustomerRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('erp:customer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomerExcel(@Valid ErpCustomerPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpCustomerDO> list = customerService.getCustomerPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "客户.xls", "数据", ErpCustomerRespVO.class,
                        BeanUtils.toBean(list, ErpCustomerRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得客户导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpCustomerImportExcelVO> list = Arrays.asList(
            ErpCustomerImportExcelVO.builder()
                .no("KH20241201000001")
                .name("张三")
                .receiverName("李四")
                .telephone("13800138000")
                .address("北京市朝阳区xxx街道xxx号")
                .wechatAccount("wechat123")
                .alipayAccount("alipay@example.com")
                .bankAccount("622908212277228617")
                .remark("重要客户")
                .build()
        );
        // 输出
        ExcelUtils.write(response, "客户导入模板.xls", "客户列表", ErpCustomerImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入客户")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:customer:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpCustomerImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpCustomerImportExcelVO> list = ExcelUtils.read(inputStream, ErpCustomerImportExcelVO.class);
            return success(customerService.importCustomers(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索客户")
    @PreAuthorize("@ss.hasPermission('erp:customer:query')")
    public CommonResult<List<ErpCustomerSaveReqVO>> searchCustomers(@Valid ErpCustomerPageReqVO searchReqVO) {
        List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(searchReqVO);
        return success(customers);
    }

}