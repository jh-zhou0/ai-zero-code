package org.zjh.aizerocode.ai.core.saver;

import cn.hutool.core.util.StrUtil;
import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.ai.model.HtmlCodeResult;
import org.zjh.aizerocode.exception.BusinessException;
import org.zjh.aizerocode.exception.ErrorCode;

import static org.zjh.aizerocode.constant.AppConstant.HTML_FILE_NAME;

/**
 * HTML代码文件保存器
 *
 * @author kayson
 * @since 2026/6/8 21:16
 */
public class HtmlCodeFileSaver extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, HTML_FILE_NAME, result.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }
}
