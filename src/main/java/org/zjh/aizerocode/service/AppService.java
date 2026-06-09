package org.zjh.aizerocode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.zjh.aizerocode.model.dto.AppQueryRequest;
import org.zjh.aizerocode.model.entity.App;
import org.zjh.aizerocode.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Kayson
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用视图对象。
     *
     * @param app 应用
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表。
     *
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件对象。
     *
     * @param appQueryRequest 应用查询条件
     * @return 查询条件对象
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
