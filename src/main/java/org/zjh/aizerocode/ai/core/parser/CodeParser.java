package org.zjh.aizerocode.ai.core.parser;

/**
 * 代码解析器
 *
 * @author kayson
 * @since 2026/6/8 20:57
 */
public interface CodeParser<T> {

    /**
     * 解析代码
     *
     * @param codeContent 代码内容
     * @return 解析结果
     */
    T parse(String codeContent);
}
