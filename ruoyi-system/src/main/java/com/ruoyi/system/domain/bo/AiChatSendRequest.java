package com.ruoyi.system.domain.bo;

/**
 * AI聊天发送请求
 */
public class AiChatSendRequest
{
    private Long sessionId;

    private String content;

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
