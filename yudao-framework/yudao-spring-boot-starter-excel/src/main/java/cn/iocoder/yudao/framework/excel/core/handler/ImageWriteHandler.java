package cn.iocoder.yudao.framework.excel.core.handler;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

/**
 * Excel图片写入处理器
 * 用于将图片数据写入Excel单元格
 *
 * @author yudao
 */
@Slf4j
public class ImageWriteHandler implements RowWriteHandler {

    private final String imageFieldName;
    private final int imageColumnIndex;
    private final int imageWidth;
    private final int imageHeight;

    /**
     * 构造函数
     *
     * @param imageFieldName   图片字段名称
     * @param imageColumnIndex 图片列索引（从0开始）
     * @param imageWidth       图片宽度（像素，默认100）
     * @param imageHeight      图片高度（像素，默认100）
     */
    public ImageWriteHandler(String imageFieldName, int imageColumnIndex, int imageWidth, int imageHeight) {
        this.imageFieldName = imageFieldName;
        this.imageColumnIndex = imageColumnIndex;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, 
                                org.apache.poi.ss.usermodel.Row row, Integer rowIndex, Boolean isHead) {
        // 图片处理在processImages方法中统一处理，这里不需要处理
    }

    /**
     * 处理图片插入（在sheet写入完成后调用）
     */
    public void processImages(org.apache.poi.ss.usermodel.Sheet sheet, org.apache.poi.ss.usermodel.Workbook workbook, java.util.List<?> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("数据列表为空，无法插入图片");
            return;
        }

        log.info("开始处理图片插入，数据行数: {}, 图片列索引: {}", dataList.size(), imageColumnIndex);

        // 创建绘图对象
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        
        // 遍历数据行，从第2行开始（索引1，因为第1行是表头）
        int insertedCount = 0;
        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            int rowIndex = i + 1; // 跳过表头行
            
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                log.warn("行号: {} 不存在，跳过", rowIndex + 1);
                continue;
            }
            
            // 打印当前行的所有单元格内容，用于调试
            if (log.isDebugEnabled()) {
                StringBuilder cellValues = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cellValues.append(String.format("列%d=%s, ", j, cell.toString()));
                    }
                }
                log.debug("行号: {}, 单元格内容: {}", rowIndex + 1, cellValues.toString());
            }
            
            // 从数据对象中提取图片数据
            byte[] imageData = extractImageData(data, imageFieldName);
            if (imageData == null || imageData.length == 0) {
                log.debug("行号: {}, 图片数据为空，跳过", rowIndex + 1);
                continue;
            }
            
            log.debug("行号: {}, 开始插入图片，大小: {} bytes", rowIndex + 1, imageData.length);
            try {
                // 检测图片类型
                int pictureType = Workbook.PICTURE_TYPE_JPEG;
                if (imageData.length >= 4) {
                    // 检查文件头判断图片类型
                    if (imageData[0] == (byte)0x89 && imageData[1] == (byte)0x50 && 
                        imageData[2] == (byte)0x4E && imageData[3] == (byte)0x47) {
                        // PNG: 89 50 4E 47
                        pictureType = Workbook.PICTURE_TYPE_PNG;
                    } else if (imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8) {
                        // JPEG: FF D8
                        pictureType = Workbook.PICTURE_TYPE_JPEG;
                    }
                    // 其他格式默认使用JPEG
                }
                
                log.debug("行号: {}, 图片类型: {}, 大小: {} bytes", rowIndex + 1, pictureType, imageData.length);
                
                // 使用ImageIO读取图片尺寸（避免插入临时图片）
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
                int originalWidthPx = bufferedImage.getWidth();
                int originalHeightPx = bufferedImage.getHeight();
                
                log.debug("行号: {}, 图片原始尺寸: {}x{} 像素", rowIndex + 1, originalWidthPx, originalHeightPx);
                
                // 计算缩放比例，保持宽高比
                double scaleX = (double) imageWidth / originalWidthPx;
                double scaleY = (double) imageHeight / originalHeightPx;
                double scale = Math.min(scaleX, scaleY); // 使用较小的比例，保持宽高比
                
                // 计算实际显示尺寸（像素）
                int actualWidthPx = (int) (originalWidthPx * scale);
                int actualHeightPx = (int) (originalHeightPx * scale);
                
                log.debug("行号: {}, 缩放比例: {}, 实际显示尺寸: {}x{} 像素", 
                         rowIndex + 1, scale, actualWidthPx, actualHeightPx);
                
                // 先设置行高和列宽，确保单元格足够大
                // 行高：像素转点，1像素 ≈ 0.75点（基于96 DPI），增加2点边距
                float rowHeightInPoints = actualHeightPx * 0.75f + 2.0f;
                row.setHeightInPoints(rowHeightInPoints);
                
                // 列宽：像素转字符宽度，1字符宽度 ≈ 7像素，列宽单位 = (像素/7) * 256
                int columnWidth = (int) ((actualWidthPx / 7.0) * 256.0) + 256; // 增加1个字符宽度的边距
                columnWidth = Math.min(columnWidth, 255 * 256); // Excel列宽最大限制
                sheet.setColumnWidth(imageColumnIndex, columnWidth);
                
                // 添加图片到Excel
                int pictureIdx = workbook.addPicture(imageData, pictureType);
                
                // 创建anchor
                ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                anchor.setCol1(imageColumnIndex);
                anchor.setRow1(rowIndex);
                anchor.setCol2(imageColumnIndex + 1);
                anchor.setRow2(rowIndex + 1);
                anchor.setDx1(0);  // 左上角偏移（EMU单位）
                anchor.setDy1(0);
                anchor.setDx2(0);  // 右下角偏移，设为0表示填充整个单元格
                anchor.setDy2(0);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                
                // 插入图片
                Picture picture = drawing.createPicture(anchor, pictureIdx);
                
                // 调整图片大小（保持宽高比）
                picture.resize(scale);
                
                // 清空单元格内容（因为图片已经插入）
                Cell imageCell = row.getCell(imageColumnIndex);
                if (imageCell != null) {
                    imageCell.setCellValue("");
                    log.debug("行号: {}, 已清空单元格内容", rowIndex + 1);
                }
                
                log.debug("行号: {}, 图片插入成功，行高: {} 点, 列宽: {} ({} 字符)", 
                         rowIndex + 1, rowHeightInPoints, columnWidth, columnWidth / 256.0);
                insertedCount++;
                
            } catch (Exception e) {
                log.error("插入图片失败，行号: {}, 错误: {}", rowIndex + 1, e.getMessage(), e);
            }
        }
        log.info("图片插入完成，成功插入: {} 张图片", insertedCount);
    }

    /**
     * 从数据对象中提取图片数据
     *
     * @param data 数据对象
     * @param fieldName 字段名称
     * @return 图片字节数组
     */
    private byte[] extractImageData(Object data, String fieldName) {
        if (data == null || fieldName == null) {
            return null;
        }
        
        try {
            Field field = data.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(data);
            
            if (value instanceof byte[]) {
                return (byte[]) value;
            }
            return null;
        } catch (Exception e) {
            log.warn("提取图片数据失败，字段: {}, 错误: {}", fieldName, e.getMessage());
            return null;
        }
    }
}

