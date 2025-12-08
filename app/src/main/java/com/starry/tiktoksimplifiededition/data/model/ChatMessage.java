// ChatMessage.java
package com.starry.tiktoksimplifiededition.data.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final int TYPE_RECEIVED = 0;   // 接收的文本
    public static final int TYPE_SENT = 1;       // 发送的文本
    public static final int TYPE_IMAGE_RECEIVED = 2;  // 接收的图片 (系统/对方发来的图片)
    public static final int TYPE_OPERATION_RECEIVED = 3; // 接收的运营消息 (带按钮)

    private int type;
    private String imageUrl;
    private String content;
    private long timestamp;

    public ChatMessage(String content, int type, long timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setType(int type) { this.type = type; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
