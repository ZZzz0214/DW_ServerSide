package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Excel LocalDateTime 转换器
 * 
 * 处理Excel中的LocalDateTime类型转换，能够处理空值、文本等异常情况
 * 当数据不符合日期格式时记录错误并返回null，用于数据校验
 * 
 * @author 芋道源码
 */
@Slf4j
public class LocalDateTimeConvert implements Converter<LocalDateTime> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDateTime convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                           GlobalConfiguration globalConfiguration) {
        // 如果单元格为空，返回null（空值允许）
        if (readCellData.getType() == CellDataTypeEnum.EMPTY) {
            return null;
        }
        
        String stringValue = readCellData.getStringValue();
        // 如果字符串为空或只包含空白字符，返回null（空值允许）
        if (StrUtil.isBlank(stringValue)) {
            return null;
        }
        
        try {
            // 尝试多种日期格式解析
            String trimmedValue = stringValue.trim();
            
            // 1. 尝试解析为yyyy/M/d格式
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                return LocalDateTime.parse(trimmedValue, formatter);
            } catch (DateTimeParseException e1) {
                // 继续尝试其他格式
            }
            
            // 2. 尝试解析为yyyy-M-d格式
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                return LocalDateTime.parse(trimmedValue, formatter);
            } catch (DateTimeParseException e2) {
                // 继续尝试其他格式
            }
            
            // 3. 尝试解析为yyyy/M/d HH:mm:ss格式
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");
                return LocalDateTime.parse(trimmedValue, formatter);
            } catch (DateTimeParseException e3) {
                // 继续尝试其他格式
            }
            
            // 4. 尝试解析为yyyy-M-d HH:mm:ss格式
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
                return LocalDateTime.parse(trimmedValue, formatter);
            } catch (DateTimeParseException e4) {
                // 继续尝试其他格式
            }
            
            // 5. 使用hutool的DateUtil尝试解析
            try {
                Date date = DateUtil.parse(trimmedValue);
                return DateUtil.toLocalDateTime(date);
            } catch (Exception e5) {
                // 所有格式都解析失败
            }
            
            // 记录错误信息，返回null而不是抛出异常
            String fieldName = contentProperty.getField().getName();
            String errorMsg = String.format("字段[%s]的值[%s]不是有效的日期格式，支持的格式：yyyy/M/d、yyyy-M-d、yyyy/M/d HH:mm:ss、yyyy-M-d HH:mm:ss", 
                                          fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            
            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
            
            return null;
            
        } catch (Exception e) {
            // 其他异常
            String fieldName = contentProperty.getField().getName();
            String errorMsg = String.format("字段[%s]的值[%s]日期格式解析失败", fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg, e);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            
            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
            
            return null;
        }
    }

    @Override
    public WriteCellData<String> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        // 使用标准格式输出
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
        return new WriteCellData<>(value.format(formatter));
    }

    /**
     * 尝试从ReadCellData中获取行号信息
     */
    private int getRowIndexFromReadCellData(ReadCellData readCellData) {
        try {
            // 尝试通过反射获取行号信息
            java.lang.reflect.Field rowIndexField = readCellData.getClass().getDeclaredField("rowIndex");
            rowIndexField.setAccessible(true);
            Object rowIndexObj = rowIndexField.get(readCellData);
            if (rowIndexObj instanceof Integer) {
                return (Integer) rowIndexObj;
            }
        } catch (Exception e) {
            // 如果无法获取行号，使用当前设置的行号
            log.debug("无法从ReadCellData获取行号，使用当前设置的行号: {}", e.getMessage());
        }
        
        // 如果无法获取行号，使用当前设置的行号
        return ConversionErrorHolder.getCurrentRowIndex();
    }

} 