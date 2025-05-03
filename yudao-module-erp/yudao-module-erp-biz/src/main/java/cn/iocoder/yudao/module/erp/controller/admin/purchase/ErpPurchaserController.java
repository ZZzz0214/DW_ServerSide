package cn.iocoder.yudao.module.erp.controller.admin.purchase;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaserService;
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

@Tag(name = "管理后台 - ERP 采购人员")
@RestController
@RequestMapping("/erp/purchaser")
@Validated
public class ErpPurchaserController {

    @Resource
    private ErpPurchaserService purchaserService;

    @PostMapping("/create")
    @Operation(summary = "创建采购人员")
    @PreAuthorize("@ss.hasPermission('erp:purchaser:create')")
    public CommonResult<Long> createPurchaser(@Valid @RequestBody ErpPurchaserSaveReqVO createReqVO) {
        return success(purchaserService.createPurchaser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购人员")
    @PreAuthorize("@ss.hasPermission('erp:purchaser:update')")
    public CommonResult<Boolean> updatePurchaser(@Valid @RequestBody ErpPurchaserSaveReqVO updateReqVO) {
        purchaserService.updatePurchaser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购人员")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchaser:delete')")
    public CommonResult<Boolean> deletePurchaser(@RequestParam("ids") List<Long> ids) {
        purchaserService.deletePurchaser(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchaser:query')")
    public CommonResult<ErpPurchaserRespVO> getPurchaser(@RequestParam("id") Long id) {
        ErpPurchaserDO purchaser = purchaserService.getPurchaser(id);
        return success(BeanUtils.toBean(purchaser, ErpPurchaserRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购人员分页")
    @PreAuthorize("@ss.hasPermission('erp:purchaser:query')")
    public CommonResult<PageResult<ErpPurchaserRespVO>> getPurchaserPage(@Valid ErpPurchaserPageReqVO pageReqVO) {
        PageResult<ErpPurchaserRespVO> pageResult = purchaserService.getPurchaserVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得采购人员列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchaser:query')")
    public CommonResult<List<ErpPurchaserRespVO>> getPurchaserListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPurchaserRespVO> list = purchaserService.getPurchaserVOList(ids);
        return success(list);
    }
}
