package com.ruoyi.system.domain.model;

/**
 * AI聊天流式回调
 */
public interface AiChatStreamListener
{
    void onDelta(String delta);

    void onDone();
}
