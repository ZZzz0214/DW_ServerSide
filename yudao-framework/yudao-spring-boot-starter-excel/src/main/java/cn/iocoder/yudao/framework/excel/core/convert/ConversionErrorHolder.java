package cn.iocoder.yudao.framework.excel.core.convert;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel转换错误信息持有器
 * 
 * 用于存储Excel转换过程中的错误信息，供后续处理使用
 * 
 * @author 芋道源码
 */
public class ConversionErrorHolder {

    private static final ThreadLocal<Map<Integer, List<ConversionError>>> ERROR_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_ROW_INDEX = new ThreadLocal<>();

    @Data
    public static class ConversionError {
        private String fieldName;
        private String invalidValue;
        private String errorMessage;
        private int rowIndex;

        public ConversionError(String fieldName, String invalidValue, String errorMessage) {
            this.fieldName = fieldName;
            this.invalidValue = invalidValue;
            this.errorMessage = errorMessage;
            this.rowIndex = getCurrentRowIndex();
        }

        public ConversionError(String fieldName, String invalidValue, String errorMessage, int rowIndex) {
            this.fieldName = fieldName;
            this.invalidValue = invalidValue;
            this.errorMessage = errorMessage;
            this.rowIndex = rowIndex;
        }
    }

    /**
     * 设置当前处理的行号
     */
    public static void setCurrentRowIndex(int rowIndex) {
        CURRENT_ROW_INDEX.set(rowIndex);
    }

    /**
     * 获取当前处理的行号
     */
    public static int getCurrentRowIndex() {
        Integer rowIndex = CURRENT_ROW_INDEX.get();
        return rowIndex != null ? rowIndex : 0;
    }

    /**
     * 添加转换错误（使用当前行号）
     */
    public static void addError(String fieldName, String invalidValue, String errorMessage) {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        int rowIndex = getCurrentRowIndex();
        
        errorMap.computeIfAbsent(rowIndex, k -> new ArrayList<>())
                .add(new ConversionError(fieldName, invalidValue, errorMessage, rowIndex));
    }

    /**
     * 添加转换错误（指定行号）
     */
    public static void addError(String fieldName, String invalidValue, String errorMessage, int rowIndex) {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        errorMap.computeIfAbsent(rowIndex, k -> new ArrayList<>())
                .add(new ConversionError(fieldName, invalidValue, errorMessage, rowIndex));
    }

    /**
     * 获取错误映射表（按行号分组）
     */
    public static Map<Integer, List<ConversionError>> getAllErrors() {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        return new HashMap<>(errorMap);
    }

    /**
     * 获取所有错误（扁平化列表）
     */
    public static List<ConversionError> getErrors() {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        List<ConversionError> allErrors = new ArrayList<>();
        
        for (List<ConversionError> rowErrors : errorMap.values()) {
            allErrors.addAll(rowErrors);
        }
        
        return allErrors;
    }

    /**
     * 获取错误映射表
     */
    private static Map<Integer, List<ConversionError>> getErrorMap() {
        Map<Integer, List<ConversionError>> errorMap = ERROR_HOLDER.get();
        if (errorMap == null) {
            errorMap = new HashMap<>();
            ERROR_HOLDER.set(errorMap);
        }
        return errorMap;
    }

    /**
     * 清除错误信息
     */
    public static void clearErrors() {
        ERROR_HOLDER.remove();
        CURRENT_ROW_INDEX.remove();
    }

    /**
     * 检查是否有错误
     */
    public static boolean hasErrors() {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        return !errorMap.isEmpty();
    }

    /**
     * 获取错误数量
     */
    public static int getErrorCount() {
        List<ConversionError> errors = getErrors();
        return errors.size();
    }

    /**
     * 按行号分组获取错误
     */
    public static List<ConversionError> getErrorsByRowIndex(int rowIndex) {
        Map<Integer, List<ConversionError>> errorMap = getErrorMap();
        List<ConversionError> rowErrors = errorMap.get(rowIndex);
        return rowErrors != null ? new ArrayList<>(rowErrors) : new ArrayList<>();
    }
} 