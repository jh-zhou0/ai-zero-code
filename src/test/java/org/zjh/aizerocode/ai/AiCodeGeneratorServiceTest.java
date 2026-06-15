package org.zjh.aizerocode.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.zjh.aizerocode.ai.model.HtmlCodeResult;
import org.zjh.aizerocode.ai.model.MultiFileCodeResult;

/**
 * @author kayson
 * @since 2026/6/8 20:44
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult generatedHtmlCode = aiCodeGeneratorService.generateHtmlCode("生成登录页面，20行内");
        Assertions.assertNotNull(generatedHtmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult generatedMultiFileCode = aiCodeGeneratorService.generateMultiFileCode("生成登录页面，20行内");
        Assertions.assertNotNull(generatedMultiFileCode);
    }
}