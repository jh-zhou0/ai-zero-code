package org.zjh.aizerocode.ai.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.core.AiCodeGeneratorFacade;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

/**
 * @author kayson
 * @since 2026/6/8 22:10
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateCodeAndSave() {
        File file = aiCodeGeneratorFacade.generateCodeAndSave("生成登录页面，20行内", CodeGenTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeAndSaveStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateCodeAndSaveStream("生成代办事项页面，不超过200行", CodeGenTypeEnum.VUE_PROJECT, 1L);
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(result);
    }
}