package cn.iocoder.yudao.framework.excel.core.convert;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel转换错误信息持有器
 * 
 * 用于存储Excel转换过程中的错误信息，供后续处理使用
 * 
 * @author 芋道源码
 */
public class ConversionErrorHolder {

    private static final ThreadLocal<List<ConversionError>> ERROR_HOLDER = new ThreadLocal<>();
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
        List<ConversionError> errors = getErrors();
        errors.add(new ConversionError(fieldName, invalidValue, errorMessage));
    }

    /**
     * 添加转换错误（指定行号）
     */
    public static void addError(String fieldName, String invalidValue, String errorMessage, int rowIndex) {
        List<ConversionError> errors = getErrors();
        errors.add(new ConversionError(fieldName, invalidValue, errorMessage, rowIndex));
    }

    /**
     * 获取所有错误
     */
    public static List<ConversionError> getErrors() {
        List<ConversionError> errors = ERROR_HOLDER.get();
        if (errors == null) {
            errors = new ArrayList<>();
            ERROR_HOLDER.set(errors);
        }
        return errors;
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
        List<ConversionError> errors = getErrors();
        return !errors.isEmpty();
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
        List<ConversionError> allErrors = getErrors();
        List<ConversionError> rowErrors = new ArrayList<>();
        
        for (ConversionError error : allErrors) {
            if (error.getRowIndex() == rowIndex) {
                rowErrors.add(error);
            }
        }
        
        return rowErrors;
    }
} 