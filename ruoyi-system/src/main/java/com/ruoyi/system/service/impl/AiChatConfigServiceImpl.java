package com.ruoyi.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.service.IAiChatConfigService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * AI聊天配置读取服务实现
 */
@Service
public class AiChatConfigServiceImpl implements IAiChatConfigService
{
    private static final String BASE_URL_KEY = "ai.chat.baseUrl";

    private static final String API_KEY_KEY = "ai.chat.apiKey";

    private static final String MODEL_KEY = "ai.chat.model";

    private static final String TEMPERATURE_KEY = "ai.chat.temperature";

    private static final String TIMEOUT_SECONDS_KEY = "ai.chat.timeoutSeconds";

    private final ISysConfigService sysConfigService;

    @Autowired
    public AiChatConfigServiceImpl(ISysConfigService sysConfigService)
    {
        this.sysConfigService = sysConfigService;
    }

    @Override
    public AiChatConfig loadConfig()
    {
        String baseUrl = trimTrailingSlash(sysConfigService.selectConfigByKey(BASE_URL_KEY));
        String apiKey = StringUtils.trim(sysConfigService.selectConfigByKey(API_KEY_KEY));
        String model = StringUtils.trim(sysConfigService.selectConfigByKey(MODEL_KEY));

        if (StringUtils.isBlank(baseUrl))
        {
            throw new ServiceException("AI服务地址未配置");
        }
        if (StringUtils.isBlank(apiKey))
        {
            throw new ServiceException("AI服务密钥未配置");
        }
        if (StringUtils.isBlank(model))
        {
            throw new ServiceException("AI模型名称未配置");
        }

        AiChatConfig config = new AiChatConfig();
        config.setBaseUrl(baseUrl);
        config.setApiKey(apiKey);
        config.setModel(model);
        config.setTemperature(parseDouble(sysConfigService.selectConfigByKey(TEMPERATURE_KEY), 0.7D));
        config.setTimeoutSeconds(parseInteger(sysConfigService.selectConfigByKey(TIMEOUT_SECONDS_KEY), 60));
        return config;
    }

    private String trimTrailingSlash(String value)
    {
        String trimmed = StringUtils.trim(value);
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private Double parseDouble(String value, Double defaultValue)
    {
        if (StringUtils.isBlank(value))
        {
            return defaultValue;
        }
        return Double.valueOf(value);
    }

    private Integer parseInteger(String value, Integer defaultValue)
    {
        if (StringUtils.isBlank(value))
        {
            return defaultValue;
        }
        return Integer.valueOf(value);
    }
}
