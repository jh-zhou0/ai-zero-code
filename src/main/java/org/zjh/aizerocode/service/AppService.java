package org.zjh.aizerocode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.zjh.aizerocode.model.dto.AppQueryRequest;
import org.zjh.aizerocode.model.entity.App;
import org.zjh.aizerocode.model.entity.User;
import org.zjh.aizerocode.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Kayson
 */
public interface AppService extends IService<App> {

    /**
     * 聊天并生成代码。
     *
     * @param appId     应用ID
     * @param message   聊天内容
     * @param loginUser 登录用户
     * @return 生成的代码流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

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
