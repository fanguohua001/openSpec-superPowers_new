package com.ruoyi.web.controller.tool;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.AiChatMessage;
import com.ruoyi.system.domain.AiChatSession;
import com.ruoyi.system.domain.bo.AiChatSendRequest;
import com.ruoyi.system.domain.model.AiChatResponseEmitter;
import com.ruoyi.system.service.IAiChatService;

/**
 * AI聊天接口
 */
@RestController
@RequestMapping("/tool/aiChat")
public class AiChatController extends BaseController
{
    @Autowired
    private IAiChatService aiChatService;

    @PreAuthorize("@ss.hasPermi('tool:aiChat:list')")
    @GetMapping("/session/list")
    public TableDataInfo listSessions()
    {
        startPage();
        List<AiChatSession> list = aiChatService.selectSessionList(getUserId());
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('tool:aiChat:list')")
    @GetMapping("/session/{sessionId}/messages")
    public AjaxResult listMessages(@PathVariable Long sessionId)
    {
        List<AiChatMessage> list = aiChatService.selectMessages(sessionId, getUserId());
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('tool:aiChat:remove')")
    @Log(title = "AI聊天", businessType = BusinessType.DELETE)
    @DeleteMapping("/session/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds)
    {
        return toAjax(aiChatService.deleteSessions(sessionIds, getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('tool:aiChat:send')")
    @Log(title = "AI聊天", businessType = BusinessType.OTHER)
    @PostMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@RequestBody AiChatSendRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        Long userId = getUserId();
        String username = getUsername();
        AiChatResponseEmitter responseEmitter = toResponseEmitter(emitter);
        CompletableFuture.runAsync(() -> {
            try
            {
                aiChatService.sendMessage(request, userId, username, responseEmitter);
            }
            catch (Exception e)
            {
                responseEmitter.send("error", e.getMessage());
                responseEmitter.complete();
            }
        });
        return emitter;
    }

    private AiChatResponseEmitter toResponseEmitter(SseEmitter emitter)
    {
        return new AiChatResponseEmitter()
        {
            @Override
            public void send(String eventName, Object data)
            {
                try
                {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                }
                catch (IOException e)
                {
                    throw new ServiceException("发送流式响应失败：" + e.getMessage());
                }
            }

            @Override
            public void complete()
            {
                emitter.complete();
            }
        };
    }
}
