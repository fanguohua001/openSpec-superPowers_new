package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiChatMessage;
import com.ruoyi.system.domain.AiChatSession;
import com.ruoyi.system.domain.bo.AiChatSendRequest;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.domain.model.AiChatContextMessage;
import com.ruoyi.system.domain.model.AiChatResponseEmitter;
import com.ruoyi.system.domain.model.AiChatStreamListener;
import com.ruoyi.system.mapper.AiChatMessageMapper;
import com.ruoyi.system.mapper.AiChatSessionMapper;
import com.ruoyi.system.service.IAiChatConfigService;
import com.ruoyi.system.service.IAiChatModelAdapter;
import com.ruoyi.system.service.IAiChatService;

/**
 * AI聊天服务实现
 */
@Service
public class AiChatServiceImpl implements IAiChatService
{
    public static final String SESSION_STATUS_NORMAL = "0";

    public static final String SESSION_STATUS_DELETED = "1";

    public static final String MESSAGE_STATUS_NORMAL = "0";

    public static final String MESSAGE_STATUS_GENERATING = "1";

    public static final String MESSAGE_STATUS_FAILED = "2";

    public static final String ROLE_USER = "user";

    public static final String ROLE_ASSISTANT = "assistant";

    private static final int RECENT_MESSAGE_LIMIT = 20;

    private final AiChatSessionMapper sessionMapper;

    private final AiChatMessageMapper messageMapper;

    private final IAiChatConfigService configService;

    private final IAiChatModelAdapter modelAdapter;

    public AiChatServiceImpl(AiChatSessionMapper sessionMapper, AiChatMessageMapper messageMapper)
    {
        this(sessionMapper, messageMapper, null, null);
    }

    @Autowired
    public AiChatServiceImpl(AiChatSessionMapper sessionMapper, AiChatMessageMapper messageMapper,
            IAiChatConfigService configService, IAiChatModelAdapter modelAdapter)
    {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.configService = configService;
        this.modelAdapter = modelAdapter;
    }

    @Override
    public List<AiChatSession> selectSessionList(Long userId)
    {
        AiChatSession query = new AiChatSession();
        query.setUserId(userId);
        query.setStatus(SESSION_STATUS_NORMAL);
        return sessionMapper.selectAiChatSessionList(query);
    }

    @Override
    public List<AiChatMessage> selectMessages(Long sessionId, Long userId)
    {
        requireOwnedSession(sessionId, userId);
        AiChatMessage query = new AiChatMessage();
        query.setSessionId(sessionId);
        query.setUserId(userId);
        return messageMapper.selectAiChatMessageList(query);
    }

    @Override
    public int deleteSessions(Long[] sessionIds, Long userId)
    {
        return sessionMapper.deleteAiChatSessionByIds(sessionIds, userId);
    }

    @Override
    public void sendMessage(AiChatSendRequest request, Long userId, String username, AiChatResponseEmitter emitter)
    {
        if (request == null || StringUtils.isBlank(request.getContent()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        AiChatConfig config = configService.loadConfig();
        String content = StringUtils.trim(request.getContent());
        AiChatSession session = prepareSession(request.getSessionId(), userId, username, config, content);

        AiChatMessage userMessage = buildMessage(session.getSessionId(), userId, ROLE_USER, content, MESSAGE_STATUS_NORMAL);
        messageMapper.insertAiChatMessage(userMessage);

        AiChatMessage assistantMessage = buildMessage(session.getSessionId(), userId, ROLE_ASSISTANT, "",
                MESSAGE_STATUS_GENERATING);
        messageMapper.insertAiChatMessage(assistantMessage);

        sendEvent(emitter, "session", session);
        sendMessageEvent(emitter, userMessage, assistantMessage);

        StringBuilder answer = new StringBuilder();
        try
        {
            modelAdapter.streamChat(config, buildContextMessages(session.getSessionId(), userMessage),
                    new AiChatStreamListener()
                    {
                        @Override
                        public void onDelta(String delta)
                        {
                            answer.append(delta);
                            sendEvent(emitter, "delta", delta);
                        }

                        @Override
                        public void onDone()
                        {
                            sendEvent(emitter, "done", "");
                        }
                    });
            assistantMessage.setContent(answer.toString());
            assistantMessage.setStatus(MESSAGE_STATUS_NORMAL);
            messageMapper.updateAiChatMessage(assistantMessage);
        }
        catch (Exception e)
        {
            assistantMessage.setContent(answer.toString());
            assistantMessage.setStatus(MESSAGE_STATUS_FAILED);
            assistantMessage.setErrorMessage(e.getMessage());
            messageMapper.updateAiChatMessage(assistantMessage);
            sendEvent(emitter, "error", e.getMessage());
        }
        finally
        {
            emitter.complete();
        }
    }

    private AiChatSession requireOwnedSession(Long sessionId, Long userId)
    {
        AiChatSession session = sessionMapper.selectAiChatSessionById(sessionId);
        if (session == null || !userId.equals(session.getUserId())
                || StringUtils.equals(SESSION_STATUS_DELETED, session.getStatus()))
        {
            throw new ServiceException("无权访问该会话");
        }
        return session;
    }

    private AiChatSession prepareSession(Long sessionId, Long userId, String username, AiChatConfig config, String content)
    {
        if (sessionId != null)
        {
            return requireOwnedSession(sessionId, userId);
        }
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(buildTitle(content));
        session.setModel(config.getModel());
        session.setStatus(SESSION_STATUS_NORMAL);
        session.setCreateBy(username);
        sessionMapper.insertAiChatSession(session);
        return session;
    }

    private AiChatMessage buildMessage(Long sessionId, Long userId, String role, String content, String status)
    {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setStatus(status);
        return message;
    }

    private List<AiChatContextMessage> buildContextMessages(Long sessionId, AiChatMessage fallbackUserMessage)
    {
        List<AiChatMessage> recentMessages = messageMapper.selectRecentMessages(sessionId, RECENT_MESSAGE_LIMIT);
        if (recentMessages == null || recentMessages.isEmpty())
        {
            recentMessages = Collections.singletonList(fallbackUserMessage);
        }
        else
        {
            Collections.reverse(recentMessages);
        }

        List<AiChatContextMessage> contextMessages = new ArrayList<>();
        for (AiChatMessage message : recentMessages)
        {
            if (StringUtils.isNotBlank(message.getContent()))
            {
                contextMessages.add(new AiChatContextMessage(message.getRole(), message.getContent()));
            }
        }
        return contextMessages;
    }

    private void sendMessageEvent(AiChatResponseEmitter emitter, AiChatMessage userMessage, AiChatMessage assistantMessage)
    {
        Map<String, Long> messageIds = new HashMap<>();
        messageIds.put("userMessageId", userMessage.getMessageId());
        messageIds.put("assistantMessageId", assistantMessage.getMessageId());
        sendEvent(emitter, "message", messageIds);
    }

    private void sendEvent(AiChatResponseEmitter emitter, String eventName, Object data)
    {
        emitter.send(eventName, data);
    }

    private String buildTitle(String content)
    {
        String title = StringUtils.trim(content);
        if (title.length() > 30)
        {
            return title.substring(0, 30);
        }
        return title;
    }
}
