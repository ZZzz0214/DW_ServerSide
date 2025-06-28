package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel Integer 转换器
 *
 * 处理Excel中的Integer类型转换，能够处理空值、文本等异常情况
 * 当数据不符合Integer类型时记录错误并返回null，用于数据校验
 *
 * @author 芋道源码
 */
@Slf4j
public class IntegerConvert implements Converter<Integer> {

    public IntegerConvert() {
        log.info("[IntegerConvert] 转换器被实例化");
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        log.info("[IntegerConvert] supportJavaTypeKey被调用，返回Integer.class");
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        log.info("[IntegerConvert] supportExcelTypeKey被调用，返回NUMBER");
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Integer convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {
        // 添加调试日志，确认转换器是否被调用
        String fieldName = contentProperty.getField().getName();
        log.info("[IntegerConvert] 转换器被调用 - 字段: {}, 类型: {}", fieldName, readCellData.getType());
        
        // 如果单元格为空，返回null（空值允许）
        if (readCellData.getType() == CellDataTypeEnum.EMPTY) {
            log.info("[IntegerConvert] 单元格为空，返回null");
            return null;
        }

        // 处理NUMBER类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.NUMBER) {
            Number numberValue = readCellData.getNumberValue();
            if (numberValue == null) {
                log.info("[IntegerConvert] NUMBER类型单元格值为null，返回null");
                return null;
            }
            Integer result = numberValue.intValue();
            log.info("[IntegerConvert] NUMBER类型转换成功: {} -> {}", numberValue, result);
            return result;
        }

        // 处理STRING类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.STRING) {
            String stringValue = readCellData.getStringValue();
            log.info("[IntegerConvert] STRING类型单元格值: '{}'", stringValue);
            
            // 如果字符串为空或只包含空白字符，返回null（空值允许）
            if (StrUtil.isBlank(stringValue)) {
                log.info("[IntegerConvert] 字符串为空，返回null");
                return null;
            }

            try {
                // 尝试转换为Integer
                Integer result = Integer.parseInt(stringValue.trim());
                log.info("[IntegerConvert] STRING类型转换成功: '{}' -> {}", stringValue, result);
                return result;
            } catch (NumberFormatException e) {
                // 记录错误信息，但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]不是有效的整数格式", fieldName, stringValue);
                log.warn("[convertToJavaData]{}", errorMsg);
                
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
        log.info("[IntegerConvert] 其他类型单元格值: '{}'", stringValue);
        
        if (StrUtil.isBlank(stringValue)) {
            log.info("[IntegerConvert] 其他类型字符串为空，返回null");
            return null;
        }

        try {
            Integer result = Integer.parseInt(stringValue.trim());
            log.info("[IntegerConvert] 其他类型转换成功: '{}' -> {}", stringValue, result);
            return result;
        } catch (NumberFormatException e) {
            // 记录错误信息，但不抛出异常，返回null让后续处理
            String errorMsg = String.format("字段[%s]的值[%s]不是有效的整数格式", fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            
            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
            
            // 返回null，不抛出异常
            return null;
        }
    }

    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value.toString());
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
