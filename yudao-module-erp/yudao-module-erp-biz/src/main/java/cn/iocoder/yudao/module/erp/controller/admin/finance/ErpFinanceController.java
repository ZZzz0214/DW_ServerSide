package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 财务")
@RestController
@RequestMapping("/erp/finance")
@Validated
@Slf4j
public class ErpFinanceController {

    // 图片下载配置常量
    private static final int CONNECTION_TIMEOUT = 15000;  // 连接超时15秒
    private static final int READ_TIMEOUT = 60000;        // 读取超时60秒
    private static final int BUFFER_SIZE = 8192;          // 缓冲区大小8KB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 最大文件大小50MB
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Resource
    private ErpFinanceService financeService;

    @PostMapping("/create")
    @Operation(summary = "创建财务")
    @PreAuthorize("@ss.hasPermission('erp:finance:create')")
    public CommonResult<Long> createFinance(@Valid @RequestBody ErpFinanceSaveReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        return success(financeService.createFinance(createReqVO, currentUsername));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务")
    @PreAuthorize("@ss.hasPermission('erp:finance:update')")
    public CommonResult<Boolean> updateFinance(@Valid @RequestBody ErpFinanceSaveReqVO updateReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeService.updateFinance(updateReqVO, currentUsername);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance:delete')")
    public CommonResult<Boolean> deleteFinance(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeService.deleteFinance(ids, currentUsername);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance:query')")
    public CommonResult<ErpFinanceRespVO> getFinance(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        ErpFinanceDO finance = financeService.getFinance(id, currentUsername);
        return success(BeanUtils.toBean(finance, ErpFinanceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务分页")
    @PreAuthorize("@ss.hasPermission('erp:finance:query')")
    public CommonResult<PageResult<ErpFinanceRespVO>> getFinancePage(@Valid ErpFinancePageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        PageResult<ErpFinanceRespVO> pageResult = financeService.getFinanceVOPage(pageReqVO, currentUsername);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得财务列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance:query')")
    public CommonResult<List<ErpFinanceRespVO>> getFinanceListByIds(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpFinanceRespVO> list = financeService.getFinanceVOList(ids, currentUsername);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出财务 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceExcel(@Valid ErpFinancePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpFinanceRespVO> pageResult = financeService.getFinanceVOPage(pageReqVO, currentUsername);

        // 转换为导出VO
        List<ErpFinanceExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpFinanceExportVO.class);

        // 下载图片并转换为byte[]
        int successCount = 0;
        int failCount = 0;
        for (ErpFinanceExportVO exportVO : exportList) {
            if (exportVO.getCarouselImages() != null && !exportVO.getCarouselImages().trim().isEmpty()) {
                try {
                    // 获取第一张图片URL
                    String[] imageUrls = exportVO.getCarouselImages().split(",");
                    if (imageUrls.length > 0) {
                        String firstImageUrl = imageUrls[0].trim();
                        if (!firstImageUrl.isEmpty()) {
                            log.debug("开始获取图片，编号: {}, URL: {}", exportVO.getNo(), firstImageUrl);
                            // 获取图片数据
                            byte[] imageData = getImageData(firstImageUrl);
                            if (imageData != null && imageData.length > 0) {
                                exportVO.setCarouselImagesData(imageData);
                                // 清空carouselImages字段的文本内容，避免显示URL
                                exportVO.setCarouselImages("");
                                successCount++;
                                log.debug("图片获取成功，编号: {}, 大小: {} bytes", exportVO.getNo(), imageData.length);
                            } else {
                                failCount++;
                                log.warn("图片获取失败，编号: {}, URL: {}, 返回数据为空", exportVO.getNo(), firstImageUrl);
                            }
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    log.warn("下载图片失败，编号: {}, 错误: {}", exportVO.getNo(), e.getMessage(), e);
                    // 图片下载失败不影响导出，继续处理
                }
            }
        }
        log.info("图片下载完成，成功: {}张，失败: {}张", successCount, failCount);

        // 导出 Excel（支持图片）
        // 轮播图片字段在ExportVO中是第2列（索引1），图片大小设置为200x200像素
        ExcelUtils.writeWithImage(response, "财务信息.xlsx", "数据", ErpFinanceExportVO.class,
                exportList, "carouselImagesData", 1, 200, 200);
    }

    /**
     * 获取图片数据（通过HTTP方式，支持本地文件存储）
     */
    private byte[] getImageData(String imageUrl) {
        try {
            return downloadImageFromUrl(imageUrl);
        } catch (Exception e) {
            log.warn("获取图片失败: {}, 错误: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 从URL下载图片（HTTP方式，支持本地文件存储）
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws Exception {
        String originalUrl = imageUrl;
        // 如果是相对路径，转换为完整URL
        if (imageUrl.startsWith("/admin-api/")) {
            imageUrl = "http://localhost:48080" + imageUrl;
        } else if (imageUrl.startsWith("http://localhost:48080/admin-api/") ||
                   imageUrl.startsWith("http://192.168.1.85:48080/admin-api/") ||
                   imageUrl.startsWith("https://")) {
            // 已经是完整的URL，直接使用
        } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            imageUrl = "http://localhost:48080/admin-api/infra/file/4/get/" + imageUrl;
        }
        log.debug("下载图片，原始URL: {}, 处理后URL: {}", originalUrl, imageUrl);

        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP请求失败，响应码: " + responseCode + ", URL: " + imageUrl);
        }

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > MAX_FILE_SIZE) {
                    throw new RuntimeException("图片文件过大，超过50MB限制: " + imageUrl);
                }
                outputStream.write(buffer, 0, bytesRead);
            }

            if (outputStream.size() == 0) {
                throw new RuntimeException("下载的图片文件为空: " + imageUrl);
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.warn("下载图片失败，将跳过此图片: {}, 错误: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    @PostMapping("/import")
    @Operation(summary = "导入财务")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:finance:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpFinanceImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            // 清除之前的错误信息
            ConversionErrorHolder.clearErrors();
            List<ErpFinanceImportExcelVO> list = ExcelUtils.read(inputStream, ErpFinanceImportExcelVO.class, new RowIndexListener<>());
            return success(financeService.importFinanceList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入财务模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手工创建导入模板示例数据
        ErpFinanceImportExcelVO demo = ErpFinanceImportExcelVO.builder()
                .no("CW202403250001")
                .billName("办公用品采购")
                .amount(new BigDecimal("1000.00"))
                .incomeExpense(1) // 将显示为"收入"
                .category("办公费用")
                .account("微信") // 添加收付账号示例
                .status(1) // 将显示为"待处理"
                .remark("采购办公用品")
                .orderDate(java.time.LocalDate.now())
                .build();

        List<ErpFinanceImportExcelVO> list = Arrays.asList(demo);

        // 输出
        ExcelUtils.write(response, "财务导入模板.xls", "财务列表", ErpFinanceImportExcelVO.class, list);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核财务记录")
    @PreAuthorize("@ss.hasPermission('erp:finance:audit')")
    public CommonResult<Boolean> auditFinance(@Valid @RequestBody ErpFinanceAuditReqVO auditReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeService.auditFinance(auditReqVO, currentUsername);
        return success(true);
    }

    @PostMapping("/unaudit")
    @Operation(summary = "反审核财务记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance:unaudit')")
    public CommonResult<Boolean> unauditFinance(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        financeService.unauditFinance(ids, currentUsername);
        return success(true);
    }
}
