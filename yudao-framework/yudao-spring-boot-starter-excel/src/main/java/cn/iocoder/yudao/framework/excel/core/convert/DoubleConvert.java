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
 * Excel Double 转换器
 * 
 * 处理Excel中的Double类型转换，能够处理空值、文本等异常情况
 * 当数据不符合Double类型时记录错误并返回null，用于数据校验
 * 
 * @author 芋道源码
 */
@Slf4j
public class DoubleConvert implements Converter<Double> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Double.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Double convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
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
            // 尝试转换为Double
            return Double.parseDouble(stringValue.trim());
        } catch (NumberFormatException e) {
            // 记录错误信息，返回null而不是抛出异常
            String fieldName = contentProperty.getField().getName();
            String errorMsg = String.format("字段[%s]的值[%s]不是有效的数字格式", fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            
            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
            
            return null;
        }
    }

    @Override
    public WriteCellData<String> convertToExcelData(Double value, ExcelContentProperty contentProperty,
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