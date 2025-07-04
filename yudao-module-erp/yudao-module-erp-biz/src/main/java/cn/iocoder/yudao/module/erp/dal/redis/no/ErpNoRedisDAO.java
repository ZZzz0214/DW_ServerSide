package cn.iocoder.yudao.module.erp.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.erp.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Erp 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class ErpNoRedisDAO {

    /**
     * 其它入库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockInDO}
     */
    public static final String STOCK_IN_NO_PREFIX = "QTRK";
    /**
     * 其它出库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutDO}
     */
    public static final String STOCK_OUT_NO_PREFIX = "QCKD";

    /**
     * 库存调拨 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveDO}
     */
    public static final String STOCK_MOVE_NO_PREFIX = "QCDB";

    /**
     * 库存盘点 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckDO}
     */
    public static final String STOCK_CHECK_NO_PREFIX = "QCPD";

    /**
     * 销售订单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO}
     */
    public static final String SALE_ORDER_NO_PREFIX = "XSDD";
    /**
     * 销售出库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO}
     */
    public static final String SALE_OUT_NO_PREFIX = "XSCK";
    /**
     * 销售退货 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO}
     */
    public static final String SALE_RETURN_NO_PREFIX = "XSTH";

    /**
     * 采购订单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO}
     */
    public static final String PURCHASE_ORDER_NO_PREFIX = "CGDD";
    public static final String DISTRIBUTION_NO_PREFIX = "DFJL";
    public static final String WHOLESALE_NO_PREFIX = "PFJL";
    /**
     * 采购入库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO}
     */
    public static final String PURCHASE_IN_NO_PREFIX = "CGRK";
    /**
     * 采购退货 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO}
     */
    public static final String PURCHASE_RETURN_NO_PREFIX = "CGTH";

    /**
     * 产品编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO}
     */
    public static final String PRODUCT_NO_PREFIX = "CPXX";

    /**
     * 组合产品编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO}
     */
    public static final String COMBO_PRODUCT_NO_PREFIX = "ZPXX";

    /**
     * 付款单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO}
     */
    public static final String FINANCE_PAYMENT_NO_PREFIX = "FKD";
    /**
     * 收款单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO}
     */
    public static final String FINANCE_RECEIPT_NO_PREFIX = "SKD";

    public static final String SALE_PRICE_NO_PREFIX = "XSJG";

    public static final String INVENTORY_NO_PREFIX = "CKJL";

    /**
     * 记事本编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO}
     */
    public static final String NOTEBOOK_NO_PREFIX = "JSB";

     /**
     * 样品编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO}
     */
    public static final String SAMPLE_NO_PREFIX = "YPB";

    /**
     * 团购货盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO}
     */
    public static final String GROUP_BUYING_NO_PREFIX = "TGHP";

    /**
     * 团购复盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO}
     */
    public static final String GROUP_BUYING_REVIEW_NO_PREFIX = "TGFP";

     /**
     * 团购信息编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.groupbuyinginfo.ErpGroupBuyingInfoDO}
     */
    public static final String GROUP_BUYING_INFO_NO_PREFIX = "TGXX";

    /**
     * 私播货盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO}
     */
    public static final String PRIVATE_BROADCASTING_NO_PREFIX = "SBHP";

    /**
     * 私播复盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO}
     */
    public static final String PRIVATE_BROADCASTING_REVIEW_NO_PREFIX = "SBFB";

    /**
     * 私播信息编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastinginfo.ErpPrivateBroadcastingInfoDO}
     */
    public static final String PRIVATE_BROADCASTING_INFO_NO_PREFIX = "SBXX";
    /**
     * 直播货盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO}
     */
    public static final String LIVE_BROADCASTING_NO_PREFIX = "ZBHP";

        /**
     * 直播复盘编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO}
     */
    public static final String LIVE_BROADCASTING_REVIEW_NO_PREFIX = "ZBFP";
    /**
     * 直播信息编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO}
     */
    public static final String LIVE_BROADCASTING_INFO_NO_PREFIX = "ZBXX";


    /**
     * 代发辅助编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO}
     */
    public static final String DROPSHIP_ASSIST_NO_PREFIX = "DFFZ";

        /**
     * 中转销售编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO}
     */
    public static final String TRANSIT_SALE_NO_PREFIX = "ZZXS";

    /**
     * 财务编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO}
     */
    public static final String FINANCE_NO_PREFIX = "CWJL";

    /**
     * 财务金额编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO}
     */
    public static final String FINANCE_AMOUNT_NO_PREFIX = "CWJE";
    
    /**
     * 采购人员编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO}
     */
    public static final String PURCHASER_NO_PREFIX = "CGRY";
    
    /**
     * 供应商编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO}
     */
    public static final String SUPPLIER_NO_PREFIX = "GYS";

    /**
     * 客户编号 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO}
     */
    public static final String CUSTOMER_NO_PREFIX = "KH";


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + yyyyMMdd + 6 位自增
     * 例如说：QTRK 202109 000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
//    public String generate(String prefix) {
//        // 递增序号
//        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
//        String key = RedisKeyConstants.NO + noPrefix;
//        Long no = stringRedisTemplate.opsForValue().increment(key);
//        // 设置过期时间
//        stringRedisTemplate.expire(key, Duration.ofDays(1L));
//        return noPrefix + String.format("%06d", no);
//    }
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String key = RedisKeyConstants.NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%06d", no);
    }




}
