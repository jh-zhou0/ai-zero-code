package org.zjh.aizerocode.ai.core;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zjh.aizerocode.ai.core.parser.CodeParserExecutor;
import org.zjh.aizerocode.ai.core.saver.CodeFileSaverExecutor;
import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.exception.BusinessException;
import org.zjh.aizerocode.exception.ErrorCode;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成服务门面类
 *
 * @author kayson
 * @since 2026/6/8 21:28
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        validateParams(userMessage, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML ->
                    CodeFileSaverExecutor.saveCode(aiCodeGeneratorService.generateHtmlCode(userMessage), codeGenTypeEnum, appId);
            case MULTI_FILE ->
                    CodeFileSaverExecutor.saveCode(aiCodeGeneratorService.generateMultiFileCode(userMessage), codeGenTypeEnum, appId);
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }

    public Flux<String> generateCodeAndSaveStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        validateParams(userMessage, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML ->
                    processCodeStream(aiCodeGeneratorService.generateHtmlCodeStream(userMessage), codeGenTypeEnum, appId);
            case MULTI_FILE ->
                    processCodeStream(aiCodeGeneratorService.generateMultiFileCodeStream(userMessage), codeGenTypeEnum, appId);
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenTypeEnum 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.parseCode(completeCode, codeGenTypeEnum);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.saveCode(parsedResult, codeGenTypeEnum, appId);
                log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

    private static void validateParams(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (StrUtil.isBlank(userMessage)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入不能为空");
        }
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成模式不能为空");
        }
    }

}
