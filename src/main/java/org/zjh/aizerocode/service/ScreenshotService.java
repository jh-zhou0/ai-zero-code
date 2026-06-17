package org.zjh.aizerocode.service;

/**
 * @author kayson
 * @since 2026/6/17 10:22
 */
public interface ScreenshotService {

    /**
     * 生成并上传截图
     *
     * @param webUrl 网页地址
     * @return 截图的 URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
