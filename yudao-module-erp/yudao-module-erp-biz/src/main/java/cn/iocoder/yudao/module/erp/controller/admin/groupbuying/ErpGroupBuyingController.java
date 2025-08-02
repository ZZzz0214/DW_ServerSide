package cn.iocoder.yudao.module.erp.controller.admin.groupbuying;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.groupbuying.ErpGroupBuyingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购货盘")
@RestController
@RequestMapping("/erp/group-buying")
@Validated
@Slf4j
public class ErpGroupBuyingController {

    @Resource
    private ErpGroupBuyingService groupBuyingService;

    @PostMapping("/create")
    @Operation(summary = "创建团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:create')")
    public CommonResult<Long> createGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO createReqVO) {
        return success(groupBuyingService.createGroupBuying(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:update')")
    public CommonResult<Boolean> updateGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO updateReqVO) {
        groupBuyingService.updateGroupBuying(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:delete')")
    public CommonResult<Boolean> deleteGroupBuying(@RequestParam("ids") List<Long> ids) {
        groupBuyingService.deleteGroupBuying(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:query')")
    public CommonResult<ErpGroupBuyingRespVO> getGroupBuying(@RequestParam("id") Long id) {
        return success(groupBuyingService.getGroupBuyingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:query')")
    public CommonResult<PageResult<ErpGroupBuyingRespVO>> getGroupBuyingPage(@Valid ErpGroupBuyingPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:query')")
    public CommonResult<List<ErpGroupBuyingRespVO>> getGroupBuyingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingRespVO> list = groupBuyingService.getGroupBuyingVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出团购货盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupBuyingExcel(@Valid ErpGroupBuyingPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpGroupBuyingExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpGroupBuyingExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "团购货盘.xlsx", "数据", ErpGroupBuyingExportVO.class,
                exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入团购货盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpGroupBuyingImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpGroupBuyingImportExcelVO> list = ExcelUtils.read(inputStream, ErpGroupBuyingImportExcelVO.class, new RowIndexListener<>());
            return success(groupBuyingService.importGroupBuyingList(list, updateSupport));
        } catch (Exception e) {
            // 记录详细错误信息
            log.error("导入团购货盘失败", e);
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入团购货盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpGroupBuyingExportVO> list = Arrays.asList(
                ErpGroupBuyingExportVO.builder()
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "团购货盘导入模板.xls", "团购货盘列表", ErpGroupBuyingExportVO.class, list);
    }

    @GetMapping("/download-images")
    @Operation(summary = "批量下载团购货盘图片")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:query')")
    public void downloadImages(@RequestParam("ids") List<Long> ids,
                             HttpServletResponse response) throws IOException {
        try {
            // 获取团购货盘数据
            List<ErpGroupBuyingRespVO> list = groupBuyingService.getGroupBuyingVOList(ids);
            
            // 设置响应头
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"groupbuying_images.zip\"");
            
            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                for (ErpGroupBuyingRespVO item : list) {
                    if (item.getProductImage() != null && !item.getProductImage().trim().isEmpty()) {
                        String[] imageUrls = item.getProductImage().split(",");
                        
                        for (int i = 0; i < imageUrls.length; i++) {
                            String imageUrl = imageUrls[i].trim();
                            if (!imageUrl.isEmpty()) {
                                try {
                                    // 下载图片
                                    byte[] imageData = downloadImageFromUrl(imageUrl);
                                    if (imageData != null) {
                                        // 生成文件名：编号_序号.扩展名
                                        String fileName = generateImageFileName(item.getNo(), i, imageUrl);
                                        
                                        // 添加到ZIP文件
                                        ZipEntry zipEntry = new ZipEntry(fileName);
                                        zipOut.putNextEntry(zipEntry);
                                        zipOut.write(imageData);
                                        zipOut.closeEntry();
                                    }
                                } catch (Exception e) {
                                    log.error("下载图片失败: {}", imageUrl, e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("批量下载图片失败", e);
            throw new RuntimeException("批量下载图片失败: " + e.getMessage());
        }
    }

    @GetMapping("/download-images-by-query")
    @Operation(summary = "按查询条件下载团购货盘图片")
    @PreAuthorize("@ss.hasPermission('erp:groupbuying:query')")
    public void downloadImagesByQuery(@Valid ErpGroupBuyingPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        try {
            // 设置页面大小为无限制，获取全部数据
            pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
            PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
            
            // 设置响应头
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"groupbuying_images.zip\"");
            
            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                for (ErpGroupBuyingRespVO item : pageResult.getList()) {
                    if (item.getProductImage() != null && !item.getProductImage().trim().isEmpty()) {
                        String[] imageUrls = item.getProductImage().split(",");
                        
                        for (int i = 0; i < imageUrls.length; i++) {
                            String imageUrl = imageUrls[i].trim();
                            if (!imageUrl.isEmpty()) {
                                try {
                                    // 下载图片
                                    byte[] imageData = downloadImageFromUrl(imageUrl);
                                    if (imageData != null) {
                                        // 生成文件名：编号_序号.扩展名
                                        String fileName = generateImageFileName(item.getNo(), i, imageUrl);
                                        
                                        // 添加到ZIP文件
                                        ZipEntry zipEntry = new ZipEntry(fileName);
                                        zipOut.putNextEntry(zipEntry);
                                        zipOut.write(imageData);
                                        zipOut.closeEntry();
                                    }
                                } catch (Exception e) {
                                    log.error("下载图片失败: {}", imageUrl, e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("按查询条件批量下载图片失败", e);
            throw new RuntimeException("按查询条件批量下载图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 从URL下载图片
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws Exception {
        // 如果是相对路径，转换为完整URL
        if (imageUrl.startsWith("/admin-api/")) {
            // 从配置中获取域名，这里暂时使用localhost
            imageUrl = "http://localhost:48080" + imageUrl;
        } else if (imageUrl.startsWith("http://192.168.1.85:48080/admin-api/")) {
            // 已经是完整的URL，直接使用
        } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            // 如果既不是完整URL也不是相对路径，可能是其他格式，尝试添加默认前缀
            imageUrl = "http://192.168.1.85:48080/admin-api/infra/file/4/get/" + imageUrl;
        }
        
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 生成图片文件名
     */
    private String generateImageFileName(String no, int index, String imageUrl) {
        // 获取图片扩展名
        String extension = ".jpg"; // 默认扩展名
        if (imageUrl.contains(".")) {
            int lastDotIndex = imageUrl.lastIndexOf(".");
            int queryIndex = imageUrl.indexOf("?", lastDotIndex);
            if (queryIndex > 0) {
                extension = imageUrl.substring(lastDotIndex, queryIndex);
            } else {
                extension = imageUrl.substring(lastDotIndex);
            }
        }
        
        // 生成文件名：编号_001.扩展名
        if (index == 0) {
            return no + extension;
        } else {
            return String.format("%s_%03d%s", no, index + 1, extension);
        }
    }
}
