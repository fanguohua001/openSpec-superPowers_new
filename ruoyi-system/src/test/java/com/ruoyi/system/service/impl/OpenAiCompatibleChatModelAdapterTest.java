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
}
