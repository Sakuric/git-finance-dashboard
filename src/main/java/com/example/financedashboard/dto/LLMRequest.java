package com.example.financedashboard.dto;

import lombok.Data;
import java.util.List;

/**
 * 大模型请求DTO
 */
@Data
public class LLMRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;

    @Data
    public static class Message {
        private String role;
        private Object content;

        public Message(String role, String text) {
            this.role = role;
            this.content = text;
        }

        public Message(String role, String text, String imageUrl) {
            this.role = role;
            java.util.List<java.util.Map<String, Object>> contentList = new java.util.ArrayList<>();

            java.util.Map<String, Object> textPart = new java.util.HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", text);
            contentList.add(textPart);

            java.util.Map<String, Object> imagePart = new java.util.HashMap<>();
            imagePart.put("type", "image_url");
            java.util.Map<String, String> imageUrlMap = new java.util.HashMap<>();

            // 支持本地文件转 Base64
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                imageUrlMap.put("url", imageUrl);
            } else {
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imageUrl));
                    String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    String mimeType = imageUrl.toLowerCase().endsWith(".png") ? "image/png" :
                                     imageUrl.toLowerCase().endsWith(".gif") ? "image/gif" : "image/jpeg";
                    imageUrlMap.put("url", "data:" + mimeType + ";base64," + base64);
                } catch (Exception e) {
                    imageUrlMap.put("url", imageUrl);
                }
            }

            imagePart.put("image_url", imageUrlMap);
            contentList.add(imagePart);

            this.content = contentList;
        }
    }
}
