package cn.iocoder.yudao.module.erp.dal.dataobject.dropship;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_dropship_assist")
public class ErpDropshipAssistESDO {

    @Id
    private String id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "original_product", type = FieldType.Text)
    private String originalProduct;

    @Field(name = "original_spec", type = FieldType.Text)
    private String originalSpec;

    @Field(name = "original_quantity", type = FieldType.Integer)
    private Integer originalQuantity;

    @Field(name = "combo_product_id", type = FieldType.Keyword)
    private String comboProductId;

    @Field(name = "product_spec", type = FieldType.Text)
    private String productSpec;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    // BaseDO中的基础字段
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Keyword)
    private String creator;

    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;
}
