package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Excel 收付账号转换器
 * 只允许微信、支付宝、银行卡三个值，其他值返回null，并记录错误
 *
 * @author 芋道源码
 */
@Slf4j
public class AccountConvert implements Converter<String> {

    private static final Set<String> VALID_ACCOUNTS = new HashSet<>(Arrays.asList("微信", "支付宝", "银行卡"));

    public AccountConvert() {
        log.info("[AccountConvert] 转换器被实例化");
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        log.info("[AccountConvert] supportJavaTypeKey被调用，返回String.class");
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        log.info("[AccountConvert] supportExcelTypeKey被调用，返回NUMBER");
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public String convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 添加调试日志，确认转换器是否被调用
        String fieldName = contentProperty != null ? contentProperty.getField().getName() : "未知字段";
        log.info("[AccountConvert] 转换器被调用 - 字段: {}, 类型: {}", fieldName, readCellData.getType());
        
        // 如果单元格为空，返回null（空值允许）
        if (readCellData.getType() == CellDataTypeEnum.EMPTY) {
            log.info("[AccountConvert] 单元格为空，返回null");
            return null;
        }

        // 处理NUMBER类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.NUMBER) {
            Number numberValue = readCellData.getNumberValue();
            if (numberValue == null) {
                log.info("[AccountConvert] NUMBER类型单元格值为null，返回null");
                return null;
            }
            String value = numberValue.toString();
            log.info("[AccountConvert] NUMBER类型单元格值: '{}'", value);
            
            // 如果字符串为空或只包含空白字符，返回null（空值允许）
            if (StrUtil.isBlank(value)) {
                log.info("[AccountConvert] NUMBER类型字符串为空，返回null");
                return null;
            }

            String trimmed = value.trim();
            if (VALID_ACCOUNTS.contains(trimmed)) {
                log.info("[AccountConvert] NUMBER类型转换成功: '{}' -> {}", value, trimmed);
                return trimmed;
            } else {
                // 记录错误信息，但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]不是有效的收付账号，仅支持: 微信、支付宝、银行卡", fieldName, value);
                log.warn("[AccountConvert]{}", errorMsg);
                
                // 尝试从ReadCellData中获取行号信息
                int rowIndex = getRowIndexFromReadCellData(readCellData);
                
                // 将错误信息存储到ThreadLocal中，供后续处理使用
                ConversionErrorHolder.addError(fieldName, value, errorMsg, rowIndex);
                
                // 返回null，不抛出异常
                return null;
            }
        }

        // 处理STRING类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.STRING) {
            String stringValue = readCellData.getStringValue();
            log.info("[AccountConvert] STRING类型单元格值: '{}'", stringValue);
            
            // 如果字符串为空或只包含空白字符，返回null（空值允许）
            if (StrUtil.isBlank(stringValue)) {
                log.info("[AccountConvert] 字符串为空，返回null");
                return null;
            }

            String trimmed = stringValue.trim();
            if (VALID_ACCOUNTS.contains(trimmed)) {
                log.info("[AccountConvert] STRING类型转换成功: '{}' -> {}", stringValue, trimmed);
                return trimmed;
            } else {
                // 记录错误信息，但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]不是有效的收付账号，仅支持: 微信、支付宝、银行卡", fieldName, stringValue);
                log.warn("[AccountConvert]{}", errorMsg);
                
                // 尝试从ReadCellData中获取行号信息
                int rowIndex = getRowIndexFromReadCellData(readCellData);
                
                // 将错误信息存储到ThreadLocal中，供后续处理使用
                ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
                
                // 返回null，不抛出异常
                return null;
            }
        }

        // 其他类型，尝试转换为字符串再处理
        try {
            String stringValue = readCellData.getStringValue();
            log.info("[AccountConvert] 其他类型单元格值: '{}'", stringValue);
            
            if (StrUtil.isBlank(stringValue)) {
                log.info("[AccountConvert] 其他类型字符串为空，返回null");
                return null;
            }

            String trimmed = stringValue.trim();
            if (VALID_ACCOUNTS.contains(trimmed)) {
                log.info("[AccountConvert] 其他类型转换成功: '{}' -> {}", stringValue, trimmed);
                return trimmed;
            } else {
                // 记录错误信息，但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]不是有效的收付账号，仅支持: 微信、支付宝、银行卡", fieldName, stringValue);
                log.warn("[AccountConvert]{}", errorMsg);
                
                // 尝试从ReadCellData中获取行号信息
                int rowIndex = getRowIndexFromReadCellData(readCellData);
                
                // 将错误信息存储到ThreadLocal中，供后续处理使用
                ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
                
                // 返回null，不抛出异常
                return null;
            }
        } catch (Exception e) {
            log.warn("[AccountConvert] 无法获取其他类型单元格的字符串值: {}", e.getMessage());
            String errorMsg = String.format("字段[%s]无法转换为字符串: %s", fieldName, e.getMessage());
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            ConversionErrorHolder.addError(fieldName, "无法转换", errorMsg, rowIndex);
            return null;
        }
    }

    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value);
    }

    /**
     * 尝试从ReadCellData中获取行号信息
     */
    private int getRowIndexFromReadCellData(ReadCellData readCellData) {
        // 优先使用ConversionErrorHolder中的当前行号，这是最可靠的
        int currentRowIndex = ConversionErrorHolder.getCurrentRowIndex();
        log.debug("[AccountConvert] 使用ConversionErrorHolder中的当前行号: {}", currentRowIndex);
        
        // 如果当前行号有效（大于0），直接使用
        if (currentRowIndex > 0) {
            return currentRowIndex;
        }
        
        // 如果当前行号无效，尝试从ReadCellData中获取
        try {
            // 尝试通过反射获取行号信息
            java.lang.reflect.Field rowIndexField = readCellData.getClass().getDeclaredField("rowIndex");
            rowIndexField.setAccessible(true);
            Object rowIndexObj = rowIndexField.get(readCellData);
            if (rowIndexObj instanceof Integer) {
                int rowIndex = (Integer) rowIndexObj;
                // EasyExcel的行号从0开始，转换为从1开始
                int convertedRowIndex = rowIndex + 1;
                log.debug("[AccountConvert] 从ReadCellData获取行号: {} -> {}", rowIndex, convertedRowIndex);
                return convertedRowIndex;
            }
        } catch (Exception e) {
            // 如果无法获取行号，记录日志
            log.debug("[AccountConvert] 无法从ReadCellData获取行号: {}", e.getMessage());
        }

        // 如果都无法获取，返回1作为默认值（避免返回0）
        log.warn("[AccountConvert] 无法获取行号，使用默认值1");
        return 1;
    }
} 