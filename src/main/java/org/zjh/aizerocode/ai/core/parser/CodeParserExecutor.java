package org.zjh.aizerocode.ai.core.parser;

import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.ai.model.HtmlCodeResult;
import org.zjh.aizerocode.ai.model.MultiFileCodeResult;
import org.zjh.aizerocode.exception.BusinessException;
import org.zjh.aizerocode.exception.ErrorCode;

/**
 * 代码解析执行器
 *
 * @author kayson
 * @since 2026/6/8 21:06
 */
public class CodeParserExecutor {

    private static final CodeParser<HtmlCodeResult> htmlCodeParser = new HtmlCodeParser();

    private static final CodeParser<MultiFileCodeResult> multiFileCodeParser = new MultiFileCodeParser();

    public static Object parseCode(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parse(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parse(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
