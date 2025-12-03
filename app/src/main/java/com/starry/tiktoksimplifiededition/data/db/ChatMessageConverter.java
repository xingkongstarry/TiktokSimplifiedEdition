// ChatMessageConverter.java
package com.starry.tiktoksimplifiededition.data.db;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.starry.tiktoksimplifiededition.data.model.ChatMessage; // 确保正确导入
import java.lang.reflect.Type;
import java.util.List;

public class ChatMessageConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static List<ChatMessage> stringToChatMessageList(String data) {
        if (data == null) {
            return null;
        }
        Type listType = new TypeToken<List<ChatMessage>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String chatMessageListToString(List<ChatMessage> chatMessages) {
        if (chatMessages == null) {
            return null;
        }
        return gson.toJson(chatMessages);
    }
}
