package org.zjh.aizerocode.core.saver;

import cn.hutool.core.util.StrUtil;
import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.ai.model.MultiFileCodeResult;
import org.zjh.aizerocode.exception.BusinessException;
import org.zjh.aizerocode.exception.ErrorCode;

import static org.zjh.aizerocode.constant.AppConstant.*;

/**
 * 多文件代码文件保存器
 *
 * @author kayson
 * @since 2026/6/8 21:21
 */
public class MultiFileCodeFileSaver extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, HTML_FILE_NAME, result.getHtmlCode());
        writeToFile(baseDirPath, CSS_FILE_NAME, result.getCssCode());
        writeToFile(baseDirPath, JS_FILE_NAME, result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}
