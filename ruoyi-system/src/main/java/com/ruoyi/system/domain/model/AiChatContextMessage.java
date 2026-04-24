package com.ruoyi.system.domain.model;

/**
 * 发送给模型的上下文消息
 */
public class AiChatContextMessage
{
    private String role;

    private String content;

    public AiChatContextMessage()
    {
    }

    public AiChatContextMessage(String role, String content)
    {
        this.role = role;
        this.content = content;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
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
