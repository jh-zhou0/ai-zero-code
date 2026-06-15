package org.zjh.aizerocode.ai.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.BeforeToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.zjh.aizerocode.ai.enums.StreamMessageTypeEnum;

/**
 * 工具调用消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage {

    private String id;

    private String name;

    private String arguments;

    public ToolRequestMessage(BeforeToolExecution beforeToolExecution) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        ToolExecutionRequest toolExecutionRequest = beforeToolExecution.request();
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}
