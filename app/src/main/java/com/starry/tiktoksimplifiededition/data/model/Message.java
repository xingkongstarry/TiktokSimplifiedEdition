// Message.java
package com.starry.tiktoksimplifiededition.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "message_table")
public class Message implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // 添加最新消息内容字段，用于在主页面显示
    public String latestContent;
    public long latestTimestamp;

    public String userId;        // 发送者ID
    public String originalName;  // 原始昵称
    public String remarkName;    // 备注名
    public String avatarUrl;     // 头像链接
    public String content;       // 消息内容 (可以保留作为最后一条消息预览)
    public long timestamp;       // 时间戳
    public int unreadCount;      // 未读数
    public int type;             // 0:文本, 1:图片, 2:运营

    // 新增：聊天历史记录
    public List<ChatMessage> chatHistory;

    public String getDisplayName() {
        return (remarkName != null && !remarkName.isEmpty()) ? remarkName : originalName;
    }

    // Getter and Setter for chatHistory
    public List<ChatMessage> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<ChatMessage> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public String getLatestContent() {
        return latestContent;
    }

    public void setLatestContent(String latestContent) {
        this.latestContent = latestContent;
    }

    public long getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(long latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}
