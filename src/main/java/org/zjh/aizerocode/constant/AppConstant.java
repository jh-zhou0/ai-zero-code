package org.zjh.aizerocode.constant;

public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * HTML文件名
     */
    String HTML_FILE_NAME = "index.html";

    /**
     * CSS文件名
     */
    String CSS_FILE_NAME = "style.css";

    /**
     * JS文件名
     */
    String JS_FILE_NAME = "script.js";

    /**
     * 文件保存的根目录
     */
    String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 文件部署的根目录
     */
    String FILE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String APP_DEPLOY_DOMAIN = "http://localhost:8080";
}
