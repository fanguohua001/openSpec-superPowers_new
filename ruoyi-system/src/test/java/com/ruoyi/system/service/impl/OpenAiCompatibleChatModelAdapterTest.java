package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ruoyi.system.domain.model.AiChatStreamListener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenAiCompatibleChatModelAdapterTest
{
    @Test
    public void handleSseLineShouldEmitDeltaAndDone()
    {
        OpenAiCompatibleChatModelAdapter adapter = new OpenAiCompatibleChatModelAdapter();
        List<String> events = new ArrayList<>();

        adapter.handleSseLine("data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}",
                new AiChatStreamListener()
                {
                    @Override
                    public void onDelta(String delta)
                    {
                        events.add("delta:" + delta);
                    }

                    @Override
                    public void onDone()
                    {
                        events.add("done");
                    }
                });
        adapter.handleSseLine("data: [DONE]", new AiChatStreamListener()
        {
            @Override
            public void onDelta(String delta)
            {
                events.add("delta:" + delta);
            }

            @Override
            public void onDone()
            {
                events.add("done");
            }
        });

        assertEquals(Arrays.asList("delta:你好", "done"), events);
    }

    @Test
    public void buildCompletionsUrlShouldAcceptBaseUrlWithOrWithoutV1()
    {
        OpenAiCompatibleChatModelAdapter adapter = new OpenAiCompatibleChatModelAdapter();

        assertEquals("http://example.com/v1/chat/completions",
                adapter.buildCompletionsUrl("http://example.com"));
        assertEquals("http://example.com/v1/chat/completions",
                adapter.buildCompletionsUrl("http://example.com/v1"));
        assertEquals("http://example.com/v1/chat/completions",
                adapter.buildCompletionsUrl("http://example.com/v1/"));
    }

    @Test
    public void buildResponsesUrlShouldAcceptBaseUrlWithOrWithoutV1()
    {
        OpenAiCompatibleChatModelAdapter adapter = new OpenAiCompatibleChatModelAdapter();

        assertEquals("http://example.com/v1/responses",
                adapter.buildResponsesUrl("http://example.com"));
        assertEquals("http://example.com/v1/responses",
                adapter.buildResponsesUrl("http://example.com/v1"));
        assertEquals("http://example.com/v1/responses",
                adapter.buildResponsesUrl("http://example.com/v1/"));
    }

    @Test
    public void handleResponsesSseLineShouldEmitDeltaAndDone()
    {
        OpenAiCompatibleChatModelAdapter adapter = new OpenAiCompatibleChatModelAdapter();
        List<String> events = new ArrayList<>();

        adapter.handleResponsesSseLine("data: {\"type\":\"response.output_text.delta\",\"delta\":\"你好\"}",
                new AiChatStreamListener()
                {
                    @Override
                    public void onDelta(String delta)
                    {
                        events.add("delta:" + delta);
                    }

                    @Override
                    public void onDone()
                    {
                        events.add("done");
                    }
                });
        adapter.handleResponsesSseLine("data: {\"type\":\"response.completed\"}", new AiChatStreamListener()
        {
            @Override
            public void onDelta(String delta)
            {
                events.add("delta:" + delta);
            }

            @Override
            public void onDone()
            {
                events.add("done");
            }
        });

        assertEquals(Arrays.asList("delta:你好", "done"), events);
    }
}
