package cn.iocoder.yudao.framework.mybatis.core.handler;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 通用参数填充实现类
 *
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author hexiaowu
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {



    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO) {
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();

            LocalDateTime current = LocalDateTime.now();
            // 创建时间为空，则以当前时间为插入时间
            if (Objects.isNull(baseDO.getCreateTime())) {
                baseDO.setCreateTime(current);
            }
            // 更新时间为空，则以当前时间为更新时间
            if (Objects.isNull(baseDO.getUpdateTime())) {
                baseDO.setUpdateTime(current);
            }

            Long userId = WebFrameworkUtils.getLoginUserId();
            if (Objects.nonNull(userId)) {
                String username = WebFrameworkUtils.getUsernameById(userId);

                if (Objects.isNull(baseDO.getCreator())) {
                    baseDO.setCreator(username);
                }
                if (Objects.isNull(baseDO.getUpdater())) {
                    baseDO.setUpdater(username);
                }
            }
            // 当前登录用户不为空，创建人为空，则当前登录用户为创建人
//            if (Objects.nonNull(userId) && Objects.isNull(baseDO.getCreator())) {
//                baseDO.setCreator(userId.toString());
//            }
//
//            // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
//            if (Objects.nonNull(userId) && Objects.isNull(baseDO.getUpdater())) {
//                baseDO.setUpdater(userId.toString());
//            }
        }
    }



// ... 其他代码保持不变 ...

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间为空，则以当前时间为更新时间
        Object modifyTime = getFieldValByName("updateTime", metaObject);
        if (Objects.isNull(modifyTime)) {
            setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

        // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
        Object modifier = getFieldValByName("updater", metaObject);
        Long userId = WebFrameworkUtils.getLoginUserId();
        if (Objects.nonNull(userId) && Objects.isNull(modifier)) {
            String username = WebFrameworkUtils.getUsernameById(userId);
            setFieldValByName("updater",username, metaObject);
        }
    }
}
