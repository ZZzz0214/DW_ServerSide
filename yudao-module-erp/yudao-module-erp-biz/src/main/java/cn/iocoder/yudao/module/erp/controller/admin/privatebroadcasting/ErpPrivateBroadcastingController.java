package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting;


import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcasting.ErpPrivateBroadcastingService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 私播货盘")
@RestController
@RequestMapping("/erp/private-broadcasting")
@Validated
@Slf4j
public class ErpPrivateBroadcastingController {

    // 图片下载配置常量
    private static final int CONNECTION_TIMEOUT = 15000;  // 连接超时15秒
    private static final int READ_TIMEOUT = 60000;        // 读取超时60秒
    private static final int BUFFER_SIZE = 8192;          // 缓冲区大小8KB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 最大文件大小50MB
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Resource
    private ErpPrivateBroadcastingService privateBroadcastingService;

    @PostMapping("/create")
    @Operation(summary = "创建私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:create')")
    public CommonResult<Long> createPrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO createReqVO) {
        return success(privateBroadcastingService.createPrivateBroadcasting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:update')")
    public CommonResult<Boolean> updatePrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO updateReqVO) {
        privateBroadcastingService.updatePrivateBroadcasting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:delete')")
    public CommonResult<Boolean> deletePrivateBroadcasting(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingService.deletePrivateBroadcasting(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcasting(@RequestParam("id") Long id) {
        return success(privateBroadcastingService.getPrivateBroadcastingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingPage(@Valid ErpPrivateBroadcastingPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingRespVO> pageResult = privateBroadcastingService.getPrivateBroadcastingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<List<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingRespVO> list = privateBroadcastingService.getPrivateBroadcastingVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出私播货盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPrivateBroadcastingExcel(@Valid ErpPrivateBroadcastingPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPrivateBroadcastingRespVO> pageResult = privateBroadcastingService.getPrivateBroadcastingVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpPrivateBroadcastingExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpPrivateBroadcastingExportVO.class);
        
        // 下载图片并转换为byte[]
        int successCount = 0;
        int failCount = 0;
        for (ErpPrivateBroadcastingExportVO exportVO : exportList) {
            if (exportVO.getProductImage() != null && !exportVO.getProductImage().trim().isEmpty()) {
                try {
                    // 获取第一张图片URL
                    String[] imageUrls = exportVO.getProductImage().split(",");
                    if (imageUrls.length > 0) {
                        String firstImageUrl = imageUrls[0].trim();
                        if (!firstImageUrl.isEmpty()) {
                            log.debug("开始获取图片，编号: {}, URL: {}", exportVO.getNo(), firstImageUrl);
                            // 获取图片数据
                            byte[] imageData = getImageData(firstImageUrl);
                            if (imageData != null && imageData.length > 0) {
                                exportVO.setProductImageData(imageData);
                                // 清空productImage字段的文本内容，避免显示URL
                                exportVO.setProductImage("");
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
                    log.warn("下载产品图片失败，编号: {}, 错误: {}", exportVO.getNo(), e.getMessage(), e);
                    // 图片下载失败不影响导出，继续处理
                }
            }
        }
        log.info("图片下载完成，成功: {}张，失败: {}张", successCount, failCount);
        
        // 导出 Excel（支持图片）
        // 产品图片字段在ExportVO中是第2列（索引1），图片大小设置为200x200像素
        ExcelUtils.writeWithImage(response, "私播货盘信息.xlsx", "数据", ErpPrivateBroadcastingExportVO.class,
                exportList, "productImageData", 1, 200, 200);
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
    @Operation(summary = "导入私播货盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpPrivateBroadcastingImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpPrivateBroadcastingImportExcelVO> list = ExcelUtils.read(inputStream, ErpPrivateBroadcastingImportExcelVO.class, new RowIndexListener<>());
            return success(privateBroadcastingService.importPrivateBroadcastingList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入私播货盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpPrivateBroadcastingExportVO> list = Arrays.asList(
                ErpPrivateBroadcastingExportVO.builder()
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "私播货盘导入模板.xls", "私播货盘列表", ErpPrivateBroadcastingExportVO.class, list);
    }
}
