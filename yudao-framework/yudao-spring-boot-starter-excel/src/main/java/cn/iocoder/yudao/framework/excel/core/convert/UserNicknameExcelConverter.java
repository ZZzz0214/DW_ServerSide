package cn.iocoder.yudao.framework.excel.core.convert;




import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class UserNicknameExcelConverter implements Converter<String> {

    private final AdminUserApi adminUserApi;

    public UserNicknameExcelConverter(AdminUserApi adminUserApi) {
        this.adminUserApi = adminUserApi;
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String userId, ExcelContentProperty contentProperty,
                                             GlobalConfiguration globalConfiguration) {
        if (userId == null) {
            return new WriteCellData<>("");
        }
        String nickname = adminUserApi.getUser(Long.valueOf(userId)).getNickname();
        return new WriteCellData<>(nickname != null ? nickname : userId);
    }
}
