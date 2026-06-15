package org.zjh.aizerocode.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zjh.aizerocode.ai.AiCodeGeneratorService;
import org.zjh.aizerocode.ai.AiCodeGeneratorServiceFactory;
import org.zjh.aizerocode.ai.enums.CodeGenTypeEnum;
import org.zjh.aizerocode.ai.message.AiResponseMessage;
import org.zjh.aizerocode.ai.message.ToolExecutedMessage;
import org.zjh.aizerocode.ai.message.ToolRequestMessage;
import org.zjh.aizerocode.core.parser.CodeParserExecutor;
import org.zjh.aizerocode.core.saver.CodeFileSaverExecutor;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        validateParams(userMessage, codeGenType);
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
        return switch (codeGenType) {
            case HTML ->
                    CodeFileSaverExecutor.saveCode(aiCodeGeneratorService.generateHtmlCode(userMessage), codeGenType, appId);
            case MULTI_FILE ->
                    CodeFileSaverExecutor.saveCode(aiCodeGeneratorService.generateMultiFileCode(userMessage), codeGenType, appId);
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }

    public Flux<String> generateCodeAndSaveStream(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        validateParams(userMessage, codeGenType);
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
        return switch (codeGenType) {
            case HTML ->
                    processCodeStream(aiCodeGeneratorService.generateHtmlCodeStream(userMessage), codeGenType, appId);
            case MULTI_FILE ->
                    processCodeStream(aiCodeGeneratorService.generateMultiFileCodeStream(userMessage), codeGenType, appId);
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
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

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink ->
                tokenStream.onPartialResponse((String partialResponse) -> {
                    AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                    sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                })
                .beforeToolExecution(beforeToolExecutionHandler -> {
                    ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecutionHandler);
                    sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                })
                .onToolExecuted(toolExecution -> {
                    ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                    sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                })
                .onCompleteResponse(response -> sink.complete())
                .onError((Throwable error) -> {
                    log.error("代码生成失败，error: {}", error.getMessage(), error);
                    sink.error(error);
                })
                .start());
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
