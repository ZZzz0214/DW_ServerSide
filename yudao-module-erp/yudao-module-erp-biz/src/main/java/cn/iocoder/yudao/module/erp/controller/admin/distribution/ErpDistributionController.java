package cn.iocoder.yudao.module.erp.controller.admin.distribution;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionService;
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

@Tag(name = "管理后台 - ERP 代发")
@RestController
@RequestMapping("/erp/distribution")
@Validated
public class ErpDistributionController {

    @Resource
    private ErpDistributionService distributionService;
    @Resource
    private ErpDistributionPurchaseMapper purchaseMapper;
    @Resource
    private ErpDistributionSaleMapper saleMapper;

    @PostMapping("/create")
    @Operation(summary = "创建代发")
    @PreAuthorize("@ss.hasPermission('erp:distribution:create')")
    public CommonResult<Long> createDistribution(@Valid @RequestBody ErpDistributionSaveReqVO createReqVO) {

        System.out.println("前端传递的数据"+createReqVO);
        return success(distributionService.createDistribution(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新代发")
    @PreAuthorize("@ss.hasPermission('erp:distribution:update')")
    public CommonResult<Boolean> updateDistribution(@Valid @RequestBody ErpDistributionSaveReqVO updateReqVO) {
        distributionService.updateDistribution(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除代发")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:distribution:delete')")
    public CommonResult<Boolean> deleteDistribution(@RequestParam("ids") List<Long> ids) {
        distributionService.deleteDistribution(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得代发")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<ErpDistributionRespVO> getDistribution(@RequestParam("id") Long id) {
        System.out.println(id);
        // 1. 获取基础信息
        ErpDistributionBaseDO distribution = distributionService.getDistribution(id);
        if (distribution == null) {
            return success(null);
        }

        // 2. 转换为RespVO
        ErpDistributionRespVO respVO = BeanUtils.toBean(distribution, ErpDistributionRespVO.class);

        // 3. 获取并合并采购信息
        ErpDistributionPurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        System.out.println("get后返回的采购数据："+purchase);
        if (purchase != null) {
            BeanUtils.copyProperties(purchase, respVO,"id");
        }

        // 4. 获取并合并销售信息
        ErpDistributionSaleDO sale = saleMapper.selectByBaseId(id);
        System.out.println("get后返回的销售数据："+sale);
        if (sale != null) {
            BeanUtils.copyProperties(sale, respVO,"id");
            respVO.setSaleShippingFee(sale.getShippingFee());
            respVO.setSaleOtherFees(sale.getOtherFees());
        }
        //respVO.setId(id);
        System.out.println("get后返回的数据：");
        System.out.println(respVO);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得代发分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionRespVO>> getDistributionPage(@Valid ErpDistributionPageReqVO pageReqVO) {
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得代发列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<List<ErpDistributionRespVO>> getDistributionListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpDistributionRespVO> list = distributionService.getDistributionVOList(ids);
        return success(list);
    }
}
