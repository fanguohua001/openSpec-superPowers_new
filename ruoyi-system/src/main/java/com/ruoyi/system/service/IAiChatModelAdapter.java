package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.domain.model.AiChatContextMessage;
import com.ruoyi.system.domain.model.AiChatStreamListener;

/**
 * AI聊天模型适配器
 */
public interface IAiChatModelAdapter
{
    void streamChat(AiChatConfig config, List<AiChatContextMessage> messages, AiChatStreamListener listener);
}
