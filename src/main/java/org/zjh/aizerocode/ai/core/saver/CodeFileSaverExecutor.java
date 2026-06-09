package org.zjh.aizerocode.ai.core.saver;

import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.ai.model.HtmlCodeResult;
import org.zjh.aizerocode.ai.model.MultiFileCodeResult;
import org.zjh.aizerocode.exception.BusinessException;
import org.zjh.aizerocode.exception.ErrorCode;

import java.io.File;

/**
 * 多文件代码保存器执行器
 *
 * @author kayson
 * @since 2026/6/8 21:26
 */
public class CodeFileSaverExecutor {

    private static final CodeFileSaverTemplate<HtmlCodeResult> htmlCodeFileSaver = new HtmlCodeFileSaver();

    private static final CodeFileSaverTemplate<MultiFileCodeResult> multiFileCodeFileSaver = new MultiFileCodeFileSaver();

    public static File saveCode(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) result, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) result, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
