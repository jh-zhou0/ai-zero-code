package org.zjh.aizerocode.ai.model;

import lombok.Data;

/**
 * HTML 代码结果
 */
@Data
public class MultiFileCodeResult {

    /**
     * HTML 代码
     */
    private String htmlCode;

    /**
     * CSS 样式
     */
    private String cssCode;

    /**
     * JavaScript 代码
     */
    private String jsCode;

    /**
     * 描述
     */
    private String description;
}
