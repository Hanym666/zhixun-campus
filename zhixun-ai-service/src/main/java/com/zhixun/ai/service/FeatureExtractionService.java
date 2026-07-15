package com.zhixun.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixun.ai.dto.ItemFeatures;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class FeatureExtractionService {

    private static final String SYSTEM_PROMPT = """
            你是校园失物招领信息提取器。请从用户描述中提取用于匹配的客观特征。
            只返回 JSON，不要 Markdown，格式：
            {"normalizedText":"简洁规范的物品描述","keywords":["关键词1","关键词2"]}
            不要输出姓名、联系方式、学号或其他隐私信息，关键词最多 10 个。
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public FeatureExtractionService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public ItemFeatures extract(String content) {
        try {
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(content)
                    .call()
                    .content();
            return parse(response, content);
        } catch (RuntimeException exception) {
            return fallback(content);
        }
    }

    private ItemFeatures parse(String response, String fallbackContent) {
        if (response == null || response.isBlank()) {
            return fallback(fallbackContent);
        }
        String json = stripCodeFence(response.trim());
        try {
            JsonNode root = objectMapper.readTree(json);
            String normalized = root.path("normalizedText").asText(fallbackContent).trim();
            List<String> keywords = new ArrayList<>();
            root.path("keywords").forEach(node -> {
                String value = node.asText().trim();
                if (!value.isBlank() && keywords.size() < 10) {
                    keywords.add(value);
                }
            });
            return new ItemFeatures(normalized, keywords);
        } catch (Exception exception) {
            return fallback(fallbackContent);
        }
    }

    private ItemFeatures fallback(String content) {
        String normalized = content == null ? "" : content.replaceAll("\\s+", " ").trim();
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        Arrays.stream(normalized.split("[，。；、,.\\s]+"))
                .map(String::trim)
                .filter(value -> value.length() >= 2 && value.length() <= 20)
                .limit(10)
                .forEach(keywords::add);
        return new ItemFeatures(normalized, List.copyOf(keywords));
    }

    private String stripCodeFence(String response) {
        if (!response.startsWith("```")) {
            return response;
        }
        int firstLine = response.indexOf('\n');
        int lastFence = response.lastIndexOf("```");
        if (firstLine >= 0 && lastFence > firstLine) {
            return response.substring(firstLine + 1, lastFence).trim();
        }
        return response;
    }
}
