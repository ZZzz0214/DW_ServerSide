package cn.iocoder.yudao.module.erp.dal.dataobject.product;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Document(indexName = "erp_combo_product_items")
@Setting(shards = 3, replicas = 1)
public class ErpComboProductItemES {

    @Id
    @Field(name = "id", type = FieldType.Long)
    private Long id;

    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId; // 关联主表ID

    @Field(name = "item_product_id", type = FieldType.Long)
    private Long itemProductId;

    @Field(name = "item_quantity", type = FieldType.Integer)
    private Integer itemQuantity;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;
}
