package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.AiChatSession;

/**
 * AI聊天会话数据层
 */
public interface AiChatSessionMapper
{
    public AiChatSession selectAiChatSessionById(Long sessionId);

    public List<AiChatSession> selectAiChatSessionList(AiChatSession session);

    public int insertAiChatSession(AiChatSession session);

    public int updateAiChatSession(AiChatSession session);

    public int deleteAiChatSessionByIds(@Param("sessionIds") Long[] sessionIds, @Param("userId") Long userId);
}
