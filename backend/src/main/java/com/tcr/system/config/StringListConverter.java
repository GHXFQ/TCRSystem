package com.tcr.system.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于处理前端传递的数组到后端字符串的转换
 * 主要用于将前端传递的courseIds数组转换为逗号分隔的字符串
 */
@JsonComponent
public class StringListConverter {

    /**
     * 将JSON数组反序列化为逗号分隔的字符串
     */
    public static class Deserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            
            // 如果是字符串类型，直接返回
            if (node.isTextual()) {
                return node.asText();
            }
            
            // 如果是数组，将其转换为逗号分隔的字符串
            if (node.isArray()) {
                List<String> values = new ArrayList<>();
                node.forEach(item -> values.add(item.asText()));
                return values.stream().collect(Collectors.joining(","));
            }
            
            // 其他情况，返回空字符串
            return "";
        }
    }
} 