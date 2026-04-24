package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.AiChatMessage;

/**
 * AI聊天消息数据层
 */
public interface AiChatMessageMapper
{
    public List<AiChatMessage> selectAiChatMessageList(AiChatMessage message);

    public List<AiChatMessage> selectRecentMessages(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);

    public int insertAiChatMessage(AiChatMessage message);

    public int updateAiChatMessage(AiChatMessage message);
}
