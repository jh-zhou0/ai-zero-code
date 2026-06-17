package org.zjh.aizerocode.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author kayson
 * @since 2026/6/17 10:46
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为 ZIP 文件
     *
     * @param projectPath 项目路径
     * @param downloadFileName 下载文件名
     * @param response 响应
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
