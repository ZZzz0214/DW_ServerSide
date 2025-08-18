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
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购货盘")
@RestController
@RequestMapping("/erp/group-buying")
@Validated
@Slf4j
public class ErpGroupBuyingController {

    // 图片下载配置常量
    private static final int CONNECTION_TIMEOUT = 15000;  // 连接超时15秒
    private static final int READ_TIMEOUT = 60000;        // 读取超时60秒
    private static final int BUFFER_SIZE = 8192;          // 缓冲区大小8KB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 最大文件大小50MB
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

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
                int successCount = 0;
                int failCount = 0;
                
                for (ErpGroupBuyingRespVO item : list) {
                    if (item.getProductImage() != null && !item.getProductImage().trim().isEmpty()) {
                        String[] imageUrls = item.getProductImage().split(",");
                        
                        for (int i = 0; i < imageUrls.length; i++) {
                            String imageUrl = imageUrls[i].trim();
                            if (!imageUrl.isEmpty()) {
                                try {
                                    // 下载图片
                                    byte[] imageData = downloadImageFromUrl(imageUrl);
                                    if (imageData != null && imageData.length > 0) {
                                        // 生成文件名：编号_序号.扩展名
                                        String fileName = generateImageFileName(item.getNo(), i, imageUrl);
                                        
                                        // 添加到ZIP文件
                                        ZipEntry zipEntry = new ZipEntry(fileName);
                                        zipOut.putNextEntry(zipEntry);
                                        zipOut.write(imageData);
                                        zipOut.closeEntry();
                                        successCount++;
                                    } else {
                                        failCount++;
                                    }
                                } catch (Exception e) {
                                    log.error("下载图片失败: {}", imageUrl, e);
                                    failCount++;
                                }
                            }
                        }
                    }
                }
                
                // 如果没有成功下载任何图片，添加一个说明文件
                if (successCount == 0) {
                    String errorInfo = "下载失败说明：\n" +
                            "- 总共尝试下载 " + failCount + " 张图片\n" +
                            "- 全部下载失败\n" +
                            "- 可能原因：网络超时、图片不存在、文件服务器异常\n" +
                            "- 建议：检查网络连接或联系管理员";
                    
                    ZipEntry errorEntry = new ZipEntry("下载失败说明.txt");
                    zipOut.putNextEntry(errorEntry);
                    zipOut.write(errorInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                } else if (failCount > 0) {
                    // 部分成功的情况，添加统计信息
                    String statsInfo = "下载统计：\n" +
                            "- 成功下载：" + successCount + " 张图片\n" +
                            "- 下载失败：" + failCount + " 张图片\n" +
                            "- 失败原因可能：网络超时、图片不存在等";
                    
                    ZipEntry statsEntry = new ZipEntry("下载统计.txt");
                    zipOut.putNextEntry(statsEntry);
                    zipOut.write(statsInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                }
                
                log.info("批量下载图片完成，成功：{}张，失败：{}张", successCount, failCount);
            }
        } catch (Exception e) {
            log.error("批量下载图片失败", e);
            // 设置错误响应
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("{\"code\":500,\"msg\":\"批量下载图片失败: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
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
                int successCount = 0;
                int failCount = 0;
                
                for (ErpGroupBuyingRespVO item : pageResult.getList()) {
                    if (item.getProductImage() != null && !item.getProductImage().trim().isEmpty()) {
                        String[] imageUrls = item.getProductImage().split(",");
                        
                        for (int i = 0; i < imageUrls.length; i++) {
                            String imageUrl = imageUrls[i].trim();
                            if (!imageUrl.isEmpty()) {
                                try {
                                    // 下载图片
                                    byte[] imageData = downloadImageFromUrl(imageUrl);
                                    if (imageData != null && imageData.length > 0) {
                                        // 生成文件名：编号_序号.扩展名
                                        String fileName = generateImageFileName(item.getNo(), i, imageUrl);
                                        
                                        // 添加到ZIP文件
                                        ZipEntry zipEntry = new ZipEntry(fileName);
                                        zipOut.putNextEntry(zipEntry);
                                        zipOut.write(imageData);
                                        zipOut.closeEntry();
                                        successCount++;
                                    } else {
                                        failCount++;
                                    }
                                } catch (Exception e) {
                                    log.error("下载图片失败: {}", imageUrl, e);
                                    failCount++;
                                }
                            }
                        }
                    }
                }
                
                // 如果没有成功下载任何图片，添加一个说明文件
                if (successCount == 0) {
                    String errorInfo = "下载失败说明：\n" +
                            "- 总共尝试下载 " + failCount + " 张图片\n" +
                            "- 全部下载失败\n" +
                            "- 可能原因：网络超时、图片不存在、文件服务器异常\n" +
                            "- 建议：检查网络连接或联系管理员";
                    
                    ZipEntry errorEntry = new ZipEntry("下载失败说明.txt");
                    zipOut.putNextEntry(errorEntry);
                    zipOut.write(errorInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                } else if (failCount > 0) {
                    // 部分成功的情况，添加统计信息
                    String statsInfo = "下载统计：\n" +
                            "- 成功下载：" + successCount + " 张图片\n" +
                            "- 下载失败：" + failCount + " 张图片\n" +
                            "- 失败原因可能：网络超时、图片不存在等";
                    
                    ZipEntry statsEntry = new ZipEntry("下载统计.txt");
                    zipOut.putNextEntry(statsEntry);
                    zipOut.write(statsInfo.getBytes("UTF-8"));
                    zipOut.closeEntry();
                }
                
                log.info("按查询条件下载图片完成，成功：{}张，失败：{}张", successCount, failCount);
            }
        } catch (Exception e) {
            log.error("按查询条件批量下载图片失败", e);
            // 设置错误响应
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("{\"code\":500,\"msg\":\"按查询条件批量下载图片失败: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
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
        // 增加超时时间设置，防止网络较慢时出现超时
        connection.setConnectTimeout(CONNECTION_TIMEOUT);  // 连接超时改为15秒
        connection.setReadTimeout(READ_TIMEOUT);     // 读取超时改为60秒
        // 设置User-Agent，避免某些服务器拒绝请求
        connection.setRequestProperty("User-Agent", USER_AGENT);
        
        // 检查HTTP响应码
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP请求失败，响应码: " + responseCode + ", URL: " + imageUrl);
        }
        
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE]; // 增加缓冲区大小提高效率
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
            // 记录详细的错误信息
            log.warn("下载图片失败，将跳过此图片: {}, 错误: {}", imageUrl, e.getMessage());
            return null; // 返回null表示此图片下载失败，但不中断整个流程
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
