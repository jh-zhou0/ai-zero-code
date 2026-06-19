package org.zjh.aizerocode.core.parser;

import cn.hutool.core.util.StrUtil;
import org.zjh.aizerocode.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 代码解析器
 *
 * @author kayson
 * @since 2026/6/8 20:59
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parse(String codeContent) {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        String htmlCode = extractHtmlCode(codeContent);
        if (StrUtil.isNotBlank(htmlCode)) {
            htmlCodeResult.setHtmlCode(htmlCode.trim());
        }
        return htmlCodeResult;
    }

    /**
     * 提取 HTML 代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
