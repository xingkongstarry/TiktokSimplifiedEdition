// ChatMessage.java
package com.starry.tiktoksimplifiededition.data.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final int TYPE_RECEIVED = 0; // 接收的消息
    public static final int TYPE_SENT = 1;     // 发送的消息

    private String content;
    private int type;
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
}
