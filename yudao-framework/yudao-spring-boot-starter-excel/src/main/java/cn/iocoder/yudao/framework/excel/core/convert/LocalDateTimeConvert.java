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

    public LocalDateTimeConvert() {
        log.info("[LocalDateTimeConvert] 转换器被实例化");
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        log.info("[LocalDateTimeConvert] supportJavaTypeKey被调用，返回LocalDateTime.class");
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        log.info("[LocalDateTimeConvert] supportExcelTypeKey被调用，返回NUMBER");
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public LocalDateTime convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                           GlobalConfiguration globalConfiguration) {
        // 添加调试日志，确认转换器是否被调用
        String fieldName = contentProperty.getField().getName();
        log.info("[LocalDateTimeConvert] 转换器被调用 - 字段: {}, 类型: {}", fieldName, readCellData.getType());
        
        // 如果单元格为空，返回null（空值允许）
        if (readCellData.getType() == CellDataTypeEnum.EMPTY) {
            log.info("[LocalDateTimeConvert] 单元格为空，返回null");
            return null;
        }

        // 处理NUMBER类型的单元格（Excel日期数字）
        if (readCellData.getType() == CellDataTypeEnum.NUMBER) {
            Number numberValue = readCellData.getNumberValue();
            if (numberValue == null) {
                log.info("[LocalDateTimeConvert] NUMBER类型单元格值为null，返回null");
                return null;
            }
            
            try {
                // Excel日期数字转换为LocalDateTime
                // Excel的日期是从1900年1月1日开始的天数
                double excelDate = numberValue.doubleValue();
                if (excelDate < 1) {
                    log.info("[LocalDateTimeConvert] Excel日期数字小于1，返回null");
                    return null;
                }
                
                // 转换为Java日期
                java.util.Date javaDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelDate);
                LocalDateTime result = javaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                log.info("[LocalDateTimeConvert] NUMBER类型转换成功: {} -> {}", excelDate, result);
                return result;
            } catch (Exception e) {
                log.warn("[LocalDateTimeConvert] Excel日期数字转换失败: {}, 错误: {}", numberValue, e.getMessage());
                return null;
            }
        }

        // 处理STRING类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.STRING) {
            String stringValue = readCellData.getStringValue();
            log.info("[LocalDateTimeConvert] STRING类型单元格值: '{}'", stringValue);
            
            // 如果字符串为空或只包含空白字符，返回null（空值允许）
            if (StrUtil.isBlank(stringValue)) {
                log.info("[LocalDateTimeConvert] 字符串为空，返回null");
                return null;
            }

            try {
                // 尝试多种日期格式解析
                String trimmedValue = stringValue.trim();

                // 1. 尝试解析为yyyy/M/d格式
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                    LocalDateTime result = LocalDateTime.parse(trimmedValue, formatter);
                    log.info("[LocalDateTimeConvert] STRING类型转换成功(yyyy/M/d): '{}' -> {}", stringValue, result);
                    return result;
                } catch (DateTimeParseException e1) {
                    // 继续尝试其他格式
                }

                // 2. 尝试解析为yyyy-M-d格式
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                    LocalDateTime result = LocalDateTime.parse(trimmedValue, formatter);
                    log.info("[LocalDateTimeConvert] STRING类型转换成功(yyyy-M-d): '{}' -> {}", stringValue, result);
                    return result;
                } catch (DateTimeParseException e2) {
                    // 继续尝试其他格式
                }

                // 3. 尝试解析为yyyy/M/d HH:mm:ss格式
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");
                    LocalDateTime result = LocalDateTime.parse(trimmedValue, formatter);
                    log.info("[LocalDateTimeConvert] STRING类型转换成功(yyyy/M/d HH:mm:ss): '{}' -> {}", stringValue, result);
                    return result;
                } catch (DateTimeParseException e3) {
                    // 继续尝试其他格式
                }

                // 4. 尝试解析为yyyy-M-d HH:mm:ss格式
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
                    LocalDateTime result = LocalDateTime.parse(trimmedValue, formatter);
                    log.info("[LocalDateTimeConvert] STRING类型转换成功(yyyy-M-d HH:mm:ss): '{}' -> {}", stringValue, result);
                    return result;
                } catch (DateTimeParseException e4) {
                    // 继续尝试其他格式
                }

                // 5. 使用hutool的DateUtil尝试解析
                try {
                    Date date = DateUtil.parse(trimmedValue);
                    LocalDateTime result = DateUtil.toLocalDateTime(date);
                    log.info("[LocalDateTimeConvert] STRING类型转换成功(DateUtil): '{}' -> {}", stringValue, result);
                    return result;
                } catch (Exception e5) {
                    // 所有格式都解析失败，记录错误信息但不抛出异常，返回null让后续处理
                    String errorMsg = String.format("字段[%s]的值[%s]不是有效的日期格式，支持的格式：yyyy/M/d、yyyy-M-d、yyyy/M/d HH:mm:ss、yyyy-M-d HH:mm:ss",
                                                  fieldName, stringValue);
                    log.warn("[convertToJavaData]{}", errorMsg, e5);

                    // 尝试从ReadCellData中获取行号信息
                    int rowIndex = getRowIndexFromReadCellData(readCellData);

                    // 将错误信息存储到ThreadLocal中，供后续处理使用
                    ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);

                    // 返回null，不抛出异常
                    return null;
                }

            } catch (Exception e) {
                // 其他异常，记录错误信息但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]日期格式解析失败", fieldName, stringValue);
                log.warn("[convertToJavaData]{}", errorMsg, e);

                // 尝试从ReadCellData中获取行号信息
                int rowIndex = getRowIndexFromReadCellData(readCellData);

                // 将错误信息存储到ThreadLocal中，供后续处理使用
                ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);

                // 返回null，不抛出异常
                return null;
            }
        }

        // 其他类型，尝试转换为字符串再处理
        String stringValue = readCellData.getStringValue();
        log.info("[LocalDateTimeConvert] 其他类型单元格值: '{}'", stringValue);
        
        if (StrUtil.isBlank(stringValue)) {
            log.info("[LocalDateTimeConvert] 其他类型字符串为空，返回null");
            return null;
        }

        try {
            // 使用hutool的DateUtil尝试解析
            Date date = DateUtil.parse(stringValue.trim());
            LocalDateTime result = DateUtil.toLocalDateTime(date);
            log.info("[LocalDateTimeConvert] 其他类型转换成功: '{}' -> {}", stringValue, result);
            return result;
        } catch (Exception e) {
            // 其他异常，记录错误信息但不抛出异常，返回null让后续处理
            String errorMsg = String.format("字段[%s]的值[%s]日期格式解析失败", fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg, e);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);

            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);

            // 返回null，不抛出异常
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
