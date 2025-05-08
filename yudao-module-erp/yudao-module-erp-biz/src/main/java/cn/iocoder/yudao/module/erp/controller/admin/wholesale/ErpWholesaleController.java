package cn.iocoder.yudao.module.erp.controller.admin.wholesale;

import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 批发")
@RestController
@RequestMapping("/erp/wholesale")
@Validated
public class ErpWholesaleController {

    @Resource
    private ErpWholesaleService wholesaleService;

    @Resource
    private ErpWholesalePurchaseMapper purchaseMapper;

    @Resource
    private ErpWholesaleSaleMapper saleMapper;

    @PostMapping("/create")
    @Operation(summary = "创建批发")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:create')")
    public CommonResult<Long> createWholesale(@Valid @RequestBody ErpWholesaleSaveReqVO createReqVO) {
        return success(wholesaleService.createWholesale(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批发")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update')")
    public CommonResult<Boolean> updateWholesale(@Valid @RequestBody ErpWholesaleSaveReqVO updateReqVO) {
        wholesaleService.updateWholesale(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批发")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale:delete')")
    public CommonResult<Boolean> deleteWholesale(@RequestParam("ids") List<Long> ids) {
        wholesaleService.deleteWholesale(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得批发")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesaleRespVO> getWholesale(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }

        // 2. 转换为RespVO
        ErpWholesaleRespVO respVO = BeanUtils.toBean(wholesale, ErpWholesaleRespVO.class);

        // 3. 获取并合并采购信息
        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);


        if (purchase != null) {
            BeanUtils.copyProperties(purchase, respVO, "id");
        }

        // 4. 获取并合并销售信息
        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);

        if (sale != null) {
            BeanUtils.copyProperties(sale, respVO, "id");
            respVO.setSaleTruckFee(sale.getTruckFee());
            respVO.setSaleLogisticsFee(sale.getLogisticsFee());
            respVO.setSaleOtherFees(sale.getOtherFees());
        }

        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得批发分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<PageResult<ErpWholesaleRespVO>> getWholesalePage(@Valid ErpWholesalePageReqVO pageReqVO) {
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得批发列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<List<ErpWholesaleRespVO>> getWholesaleListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpWholesaleRespVO> list = wholesaleService.getWholesaleVOList(ids);
        return success(list);
    }
}
