package com.ruoyi.system.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.model.AiChatConfig;
import com.ruoyi.system.domain.model.AiChatContextMessage;
import com.ruoyi.system.domain.model.AiChatStreamListener;
import com.ruoyi.system.service.IAiChatModelAdapter;

/**
 * OpenAI兼容聊天模型适配器
 */
@Service
public class OpenAiCompatibleChatModelAdapter implements IAiChatModelAdapter
{
    @Override
    public void streamChat(AiChatConfig config, List<AiChatContextMessage> messages, AiChatStreamListener listener)
    {
        try
        {
            streamChatCompletions(config, messages, listener);
        }
        catch (EndpointNotFoundException e)
        {
            streamResponses(config, messages, listener);
        }
    }

    private void streamChatCompletions(AiChatConfig config, List<AiChatContextMessage> messages,
            AiChatStreamListener listener)
    {
        streamRequest(config, buildCompletionsUrl(config.getBaseUrl()), buildChatCompletionsRequestBody(config, messages),
                line -> handleSseLine(line, listener));
    }

    private void streamResponses(AiChatConfig config, List<AiChatContextMessage> messages,
            AiChatStreamListener listener)
    {
        streamRequest(config, buildResponsesUrl(config.getBaseUrl()), buildResponsesRequestBody(config, messages),
                line -> handleResponsesSseLine(line, listener));
    }

    private void streamRequest(AiChatConfig config, String requestUrl, String requestBody, Consumer<String> lineHandler)
    {
        HttpURLConnection connection = null;
        try
        {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.getTimeoutSeconds() * 1000);
            connection.setReadTimeout(config.getTimeoutSeconds() * 1000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");

            byte[] body = requestBody.getBytes(StandardCharsets.UTF_8);
            try (OutputStream outputStream = connection.getOutputStream())
            {
                outputStream.write(body);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST)
            {
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND)
                {
                    throw new EndpointNotFoundException();
                }
                throw new ServiceException("AI服务请求失败：" + readBody(connection.getErrorStream()));
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    lineHandler.accept(line);
                }
            }
        }
        catch (IOException e)
        {
            throw new ServiceException("调用AI服务失败：" + e.getMessage());
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    void handleSseLine(String line, AiChatStreamListener listener)
    {
        if (StringUtils.isBlank(line) || !line.startsWith("data:"))
        {
            return;
        }
        String data = line.substring("data:".length()).trim();
        if ("[DONE]".equals(data))
        {
            listener.onDone();
            return;
        }

        JSONObject payload = JSON.parseObject(data);
        JSONArray choices = payload.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            return;
        }
        JSONObject choice = choices.getJSONObject(0);
        JSONObject delta = choice.getJSONObject("delta");
        if (delta != null)
        {
            String content = delta.getString("content");
            if (content != null)
            {
                listener.onDelta(content);
            }
        }
        if (choice.getString("finish_reason") != null)
        {
            listener.onDone();
        }
    }

    String buildCompletionsUrl(String baseUrl)
    {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        if (normalizedBaseUrl.endsWith("/v1"))
        {
            return normalizedBaseUrl + "/chat/completions";
        }
        return normalizedBaseUrl + "/v1/chat/completions";
    }

    String buildResponsesUrl(String baseUrl)
    {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        if (normalizedBaseUrl.endsWith("/v1"))
        {
            return normalizedBaseUrl + "/responses";
        }
        return normalizedBaseUrl + "/v1/responses";
    }

    void handleResponsesSseLine(String line, AiChatStreamListener listener)
    {
        if (StringUtils.isBlank(line) || !line.startsWith("data:"))
        {
            return;
        }
        String data = line.substring("data:".length()).trim();
        if ("[DONE]".equals(data))
        {
            listener.onDone();
            return;
        }

        JSONObject payload = JSON.parseObject(data);
        String type = payload.getString("type");
        if ("response.output_text.delta".equals(type))
        {
            String delta = payload.getString("delta");
            if (delta != null)
            {
                listener.onDelta(delta);
            }
        }
        if ("response.completed".equals(type))
        {
            listener.onDone();
        }
        if ("response.failed".equals(type))
        {
            JSONObject response = payload.getJSONObject("response");
            JSONObject error = response == null ? null : response.getJSONObject("error");
            String message = error == null ? "AI服务响应失败" : error.getString("message");
            throw new ServiceException(message);
        }
    }

    private String buildChatCompletionsRequestBody(AiChatConfig config, List<AiChatContextMessage> messages)
    {
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("stream", true);
        body.put("temperature", config.getTemperature());

        JSONArray messageArray = new JSONArray();
        for (AiChatContextMessage message : messages)
        {
            JSONObject item = new JSONObject();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            messageArray.add(item);
        }
        body.put("messages", messageArray);
        return body.toJSONString();
    }

    private String buildResponsesRequestBody(AiChatConfig config, List<AiChatContextMessage> messages)
    {
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("stream", true);
        body.put("temperature", config.getTemperature());

        JSONArray messageArray = new JSONArray();
        for (AiChatContextMessage message : messages)
        {
            JSONObject item = new JSONObject();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            messageArray.add(item);
        }
        body.put("input", messageArray);
        return body.toJSONString();
    }

    private String normalizeBaseUrl(String baseUrl)
    {
        String normalizedBaseUrl = StringUtils.trim(baseUrl);
        while (normalizedBaseUrl.endsWith("/"))
        {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl;
    }

    private String readBody(InputStream inputStream) throws IOException
    {
        if (inputStream == null)
        {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static class EndpointNotFoundException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
}
