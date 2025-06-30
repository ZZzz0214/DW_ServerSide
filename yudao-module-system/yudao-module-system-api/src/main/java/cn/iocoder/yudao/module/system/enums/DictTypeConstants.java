package cn.iocoder.yudao.module.system.enums;

/**
 * System 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    String USER_TYPE = "user_type"; // 用户类型
    String COMMON_STATUS = "common_status"; // 系统状态

    // ========== SYSTEM 模块 ==========

    String USER_SEX = "system_user_sex"; // 用户性别
    String DATA_SCOPE = "system_data_scope"; // 数据范围

    String LOGIN_TYPE = "system_login_type"; // 登录日志的类型
    String LOGIN_RESULT = "system_login_result"; // 登录结果

    String SMS_CHANNEL_CODE = "system_sms_channel_code"; // 短信渠道编码
    String SMS_TEMPLATE_TYPE = "system_sms_template_type"; // 短信模板类型
    String SMS_SEND_STATUS = "system_sms_send_status"; // 短信发送状态
    String SMS_RECEIVE_STATUS = "system_sms_receive_status"; // 短信接收状态

     // ========== ERP 模块 ==========
     String ERP_PRODUCT_BRAND = "erp_product_brand"; // ERP产品品牌
     String ERP_PRODUCT_CATEGORY = "erp_product_category"; // ERP产品品类
     String ERP_PRODUCT_STATUS = "erp_product_status"; // ERP产品状态

     String ERP_TRANSIT_PERSON = "erp_transit_person"; // ERP中转人员

    String SYSTEM_USER_LIST = "system_user_list";

    String ERP_NOTEBOOK_STATUS="erp_notebook_status";

    String ERP_SAMPLE_STATUS = "erp_sample_status";

    String ERP_GROUP_BUYING_STATUS = "status";

    // 团购信息相关字典类型
    String ERP_CUSTOMER_POSITION = "erp_customer_position"; // 客户职位
    String ERP_PLATFORM_NAME = "erp_platform_name"; // 平台名称
    String ERP_CUSTOMER_ATTRIBUTE = "erp_customer_attribute"; // 客户属性
    String ERP_CUSTOMER_CITY = "erp_customer_city"; // 客户城市
    String ERP_CUSTOMER_DISTRICT = "erp_customer_district"; // 客户区县

    // 私播信息相关字典类型（如果与团购信息不同，可以使用专用的字典类型）
    String ERP_PRIVATE_CUSTOMER_POSITION = "erp_private_customer_position"; // 私播信息客户职位
    String ERP_PRIVATE_PLATFORM_NAME = "erp_private_platform_name"; // 私播信息平台名称
    String ERP_PRIVATE_CUSTOMER_ATTRIBUTE = "erp_private_customer_attribute"; // 私播信息客户属性
    String ERP_PRIVATE_CUSTOMER_CITY = "erp_private_customer_city"; // 私播信息客户城市
    String ERP_PRIVATE_CUSTOMER_DISTRICT = "erp_private_customer_district"; // 私播信息客户区县

    // 私播货盘相关字典类型
    String ERP_PRIVATE_STATUS = "erp_privateStatus"; // 私播货盘状态

    // 直播货盘相关字典类型
    String ERP_LIVE_STATUS = "erp_live_status"; // 直播货盘状态

    // 直播信息相关字典类型
    String ERP_LIVE_CUSTOMER_POSITION = "erp_live_customer_position"; // 直播信息客户职位
    String ERP_LIVE_PLATFORM_NAME = "erp_live_platform_name"; // 直播信息平台名称
    String ERP_LIVE_CUSTOMER_ATTRIBUTE = "erp_live_customer_attribute"; // 直播信息客户属性
    String ERP_LIVE_CUSTOMER_CITY = "erp_live_customer_city"; // 直播信息客户城市
    String ERP_LIVE_CUSTOMER_DISTRICT = "erp_live_customer_district"; // 直播信息客户区县
    String ERP_LIVE_CUSTOMER_NAME = "erp_live_customer_name"; // 直播信息客户名称

    String ERP_DROPSHIP_STATUS="erp_dropship_status";

}
