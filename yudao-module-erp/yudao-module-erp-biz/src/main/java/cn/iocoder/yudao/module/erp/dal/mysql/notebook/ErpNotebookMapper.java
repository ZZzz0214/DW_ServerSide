package cn.iocoder.yudao.module.erp.dal.mysql.notebook;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpNotebookMapper extends BaseMapperX<ErpNotebookDO> {

    default PageResult<ErpNotebookRespVO> selectPage(ErpNotebookPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpNotebookDO> query = new MPJLambdaWrapperX<ErpNotebookDO>()
                .likeIfPresent(ErpNotebookDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpNotebookDO::getTaskName, reqVO.getTaskName())
                .eqIfPresent(ErpNotebookDO::getTaskStatus, reqVO.getTaskStatus())
                .likeIfPresent(ErpNotebookDO::getTaskPerson, reqVO.getTaskPerson())
                .betweenIfPresent(ErpNotebookDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpNotebookDO::getId)
                // 记事本表字段映射
                .selectAs(ErpNotebookDO::getId, ErpNotebookRespVO::getId)
                .selectAs(ErpNotebookDO::getNo, ErpNotebookRespVO::getNo)
                .selectAs(ErpNotebookDO::getCarouselImage, ErpNotebookRespVO::getCarouselImage)
                .selectAs(ErpNotebookDO::getTaskName, ErpNotebookRespVO::getTaskName)
                .selectAs(ErpNotebookDO::getTaskStatus, ErpNotebookRespVO::getTaskStatus)
                .selectAs(ErpNotebookDO::getTaskPerson, ErpNotebookRespVO::getTaskPerson)
                .selectAs(ErpNotebookDO::getRemark, ErpNotebookRespVO::getRemark)
                .selectAs(ErpNotebookDO::getCreator, ErpNotebookRespVO::getCreator)
                .selectAs(ErpNotebookDO::getCreateTime, ErpNotebookRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpNotebookRespVO.class, query);
    }

    default ErpNotebookDO selectByNo(String no) {
        return selectOne(ErpNotebookDO::getNo, no);
    }

    default List<ErpNotebookDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpNotebookDO::getNo, nos);
    }

    default void insertBatch(List<ErpNotebookDO> list) {
        list.forEach(this::insert);
    }
}
