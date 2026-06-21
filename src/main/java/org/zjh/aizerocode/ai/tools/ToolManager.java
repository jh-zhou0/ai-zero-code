package org.zjh.aizerocode.ai.tools;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * @author kayson
 * @since 2026/6/19 14:12
 */
@Component
public class ToolManager {

    @Resource
    private Map<String, BaseTool> toolMap;

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具英文名称
     * @return 工具实例
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取已注册的工具集合
     *
     * @return 工具实例集合
     */
    public Collection<BaseTool> getAllTools() {
        return toolMap.values();
    }
}
