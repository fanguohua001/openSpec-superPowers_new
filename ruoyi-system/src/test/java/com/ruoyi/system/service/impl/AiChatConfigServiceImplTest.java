package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.service.ISysConfigService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AiChatConfigServiceImplTest
{
    @Test
    public void loadConfigShouldRejectMissingBaseUrl()
    {
        ISysConfigService sysConfigService = mock(ISysConfigService.class);
        when(sysConfigService.selectConfigByKey("ai.chat.baseUrl")).thenReturn("");
        when(sysConfigService.selectConfigByKey("ai.chat.apiKey")).thenReturn("key");
        when(sysConfigService.selectConfigByKey("ai.chat.model")).thenReturn("demo-model");

        AiChatConfigServiceImpl service = new AiChatConfigServiceImpl(sysConfigService);

        ServiceException exception = assertThrows(ServiceException.class, service::loadConfig);
        assertEquals("AI服务地址未配置", exception.getMessage());
    }

    @Test
    public void loadConfigShouldRejectMissingApiKey()
    {
        ISysConfigService sysConfigService = mock(ISysConfigService.class);
        when(sysConfigService.selectConfigByKey("ai.chat.baseUrl")).thenReturn("https://api.example.com");
        when(sysConfigService.selectConfigByKey("ai.chat.apiKey")).thenReturn("");
        when(sysConfigService.selectConfigByKey("ai.chat.model")).thenReturn("demo-model");

        AiChatConfigServiceImpl service = new AiChatConfigServiceImpl(sysConfigService);

        ServiceException exception = assertThrows(ServiceException.class, service::loadConfig);
        assertEquals("AI服务密钥未配置", exception.getMessage());
    }

    @Test
    public void loadConfigShouldRejectMissingModel()
    {
        ISysConfigService sysConfigService = mock(ISysConfigService.class);
        when(sysConfigService.selectConfigByKey("ai.chat.baseUrl")).thenReturn("https://api.example.com");
        when(sysConfigService.selectConfigByKey("ai.chat.apiKey")).thenReturn("key");
        when(sysConfigService.selectConfigByKey("ai.chat.model")).thenReturn("");

        AiChatConfigServiceImpl service = new AiChatConfigServiceImpl(sysConfigService);

        ServiceException exception = assertThrows(ServiceException.class, service::loadConfig);
        assertEquals("AI模型名称未配置", exception.getMessage());
    }

    @Test
    public void loadConfigShouldUseConfiguredAndDefaultValues()
    {
        ISysConfigService sysConfigService = mock(ISysConfigService.class);
        when(sysConfigService.selectConfigByKey("ai.chat.baseUrl")).thenReturn("https://api.example.com/");
        when(sysConfigService.selectConfigByKey("ai.chat.apiKey")).thenReturn("key");
        when(sysConfigService.selectConfigByKey("ai.chat.model")).thenReturn("demo-model");
        when(sysConfigService.selectConfigByKey("ai.chat.temperature")).thenReturn("");
        when(sysConfigService.selectConfigByKey("ai.chat.timeoutSeconds")).thenReturn("");

        AiChatConfigServiceImpl service = new AiChatConfigServiceImpl(sysConfigService);

        AiChatConfig config = service.loadConfig();
        assertEquals("https://api.example.com", config.getBaseUrl());
        assertEquals("key", config.getApiKey());
        assertEquals("demo-model", config.getModel());
        assertEquals(Double.valueOf(0.7D), config.getTemperature());
        assertEquals(Integer.valueOf(60), config.getTimeoutSeconds());
    }
}
