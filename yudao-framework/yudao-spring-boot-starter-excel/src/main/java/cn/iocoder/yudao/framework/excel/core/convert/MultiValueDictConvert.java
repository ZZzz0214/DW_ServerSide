package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Excel 多值数据字典转换器
 *
 * 支持逗号分隔的多值字段（如 "1,2,3"）与字典标签（如 "在售 / 新品 / 热卖"）之间的互转。
 * 单值场景同样兼容。
 */
@Slf4j
public class MultiValueDictConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String type = getType(contentProperty);
        String label = readCellData.getStringValue();
        if (label == null || label.trim().isEmpty()) {
            return null;
        }
        String result = Arrays.stream(label.split("[/／]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(l -> {
                    String value = DictFrameworkUtils.parseDictDataValue(type, l);
                    return value != null ? value : l;
                })
                .collect(Collectors.joining(","));
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, result);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (object == null) {
            return new WriteCellData<>("");
        }
        String type = getType(contentProperty);
        String value = String.valueOf(object);
        String result = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(v -> {
                    String dictLabel = DictFrameworkUtils.getDictDataLabel(type, v);
                    if (dictLabel == null) {
                        log.error("[convertToExcelData][type({}) 转换不了 value({})]", type, v);
                        return v;
                    }
                    return dictLabel;
                })
                .collect(Collectors.joining(" / "));
        return new WriteCellData<>(result);
    }

    private static String getType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }
}
