package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.AiChatMessage;
import com.ruoyi.system.domain.AiChatSession;
import com.ruoyi.system.domain.bo.AiChatSendRequest;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.domain.model.AiChatResponseEmitter;
import com.ruoyi.system.mapper.AiChatMessageMapper;
import com.ruoyi.system.mapper.AiChatSessionMapper;
import com.ruoyi.system.service.IAiChatConfigService;
import com.ruoyi.system.service.IAiChatModelAdapter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AiChatServiceImplTest
{
    private AiChatSessionMapper sessionMapper;

    private AiChatMessageMapper messageMapper;

    @Before
    public void setUp()
    {
        sessionMapper = mock(AiChatSessionMapper.class);
        messageMapper = mock(AiChatMessageMapper.class);
    }

    @Test
    public void selectMessagesShouldRejectOtherUsersSession()
    {
        AiChatSession session = new AiChatSession();
        session.setSessionId(10L);
        session.setUserId(2L);

        when(sessionMapper.selectAiChatSessionById(10L)).thenReturn(session);

        AiChatServiceImpl service = new AiChatServiceImpl(sessionMapper, messageMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.selectMessages(10L, 1L));

        assertEquals("无权访问该会话", exception.getMessage());
        verify(messageMapper, never()).selectAiChatMessageList(any());
    }

    @Test
    public void sendMessageShouldPersistFailedAssistantWhenModelFails()
    {
        IAiChatConfigService configService = mock(IAiChatConfigService.class);
        IAiChatModelAdapter modelAdapter = mock(IAiChatModelAdapter.class);
        AiChatConfig config = new AiChatConfig();
        config.setModel("demo-model");
        List<AiChatMessage> insertedSnapshots = new ArrayList<>();
        when(configService.loadConfig()).thenReturn(config);
        when(sessionMapper.insertAiChatSession(any())).thenAnswer(invocation -> {
            AiChatSession session = invocation.getArgument(0);
            session.setSessionId(20L);
            return 1;
        });
        when(messageMapper.insertAiChatMessage(any())).thenAnswer(invocation -> {
            AiChatMessage message = invocation.getArgument(0);
            insertedSnapshots.add(copyMessage(message));
            if ("user".equals(message.getRole()))
            {
                message.setMessageId(100L);
            }
            else
            {
                message.setMessageId(101L);
            }
            return 1;
        });
        doThrow(new ServiceException("模型不可用"))
                .when(modelAdapter).streamChat(any(), anyList(), any());

        AiChatServiceImpl service = new AiChatServiceImpl(sessionMapper, messageMapper, configService, modelAdapter);
        AiChatSendRequest request = new AiChatSendRequest();
        request.setContent("你好");

        service.sendMessage(request, 1L, "admin", new NoopResponseEmitter());

        verify(messageMapper, times(2)).insertAiChatMessage(any());
        assertEquals("user", insertedSnapshots.get(0).getRole());
        assertEquals("你好", insertedSnapshots.get(0).getContent());
        assertEquals("0", insertedSnapshots.get(0).getStatus());
        assertEquals("assistant", insertedSnapshots.get(1).getRole());
        assertEquals("1", insertedSnapshots.get(1).getStatus());

        ArgumentCaptor<AiChatMessage> updateCaptor = ArgumentCaptor.forClass(AiChatMessage.class);
        verify(messageMapper).updateAiChatMessage(updateCaptor.capture());
        assertEquals(Long.valueOf(101L), updateCaptor.getValue().getMessageId());
        assertEquals("2", updateCaptor.getValue().getStatus());
        assertEquals("模型不可用", updateCaptor.getValue().getErrorMessage());
    }

    private AiChatMessage copyMessage(AiChatMessage source)
    {
        AiChatMessage message = new AiChatMessage();
        message.setMessageId(source.getMessageId());
        message.setSessionId(source.getSessionId());
        message.setUserId(source.getUserId());
        message.setRole(source.getRole());
        message.setContent(source.getContent());
        message.setStatus(source.getStatus());
        message.setErrorMessage(source.getErrorMessage());
        return message;
    }

    private static class NoopResponseEmitter implements AiChatResponseEmitter
    {
        @Override
        public void send(String eventName, Object data)
        {
        }

        @Override
        public void complete()
        {
        }
    }
}
