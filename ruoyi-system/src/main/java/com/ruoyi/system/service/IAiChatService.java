package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiChatMessage;
import com.ruoyi.system.domain.AiChatSession;
import com.ruoyi.system.domain.bo.AiChatSendRequest;
import com.ruoyi.system.domain.model.AiChatResponseEmitter;

/**
 * AI聊天服务
 */
public interface IAiChatService
{
    public List<AiChatSession> selectSessionList(Long userId);

    public List<AiChatMessage> selectMessages(Long sessionId, Long userId);

    public int deleteSessions(Long[] sessionIds, Long userId);

    public void sendMessage(AiChatSendRequest request, Long userId, String username, AiChatResponseEmitter emitter);
}
