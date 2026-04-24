package com.ruoyi.system.domain.model;

/**
 * AI聊天响应事件输出
 */
public interface AiChatResponseEmitter
{
    void send(String eventName, Object data);

    void complete();
}
