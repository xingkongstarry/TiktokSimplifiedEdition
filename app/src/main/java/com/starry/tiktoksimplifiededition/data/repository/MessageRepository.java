package com.starry.tiktoksimplifiededition.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.starry.tiktoksimplifiededition.data.db.AppDatabase;
import com.starry.tiktoksimplifiededition.data.model.ChatMessage;
import com.starry.tiktoksimplifiededition.data.model.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageRepository {
    private final AppDatabase db;
    private boolean isInitializing = false;
    public MessageRepository(Context context) {
        db = AppDatabase.getDatabase(context);
//        if (db.messageDao().getCount() == 0) {
//            initMockData();
//        }
    }

    public synchronized void initializeDataIfNeeded() {
        if (isInitializing) return;

        if (db.messageDao().getCount() == 0) {
            isInitializing = true;
            try {
                initMockData();
            } finally {
                isInitializing = false;
            }
        }
    }
    public LiveData<List<Message>> getAllMessages() {
        return Transformations.map(db.messageDao().getAllMessages(), messages -> {
            if (messages != null) {
                // 确保消息按时间倒序排列
                List<Message> sortedMessages = new ArrayList<>(messages);
                Collections.sort(sortedMessages, (m1, m2) ->
                        Long.compare(m2.getLatestTimestamp(), m1.getLatestTimestamp()));
                return sortedMessages;
            }
            return messages;
        });
    }

    public void updateMessage(Message message) {
        db.messageDao().update(message);
    }

    public void loadMoreMockData(int page, int pageSize) {
        // 检查是否已经存在数据，如果存在则不重复生成
        if (db.messageDao().getCount() > 0) {
            return; // 数据库已有数据，不需要再生成
        }

        List<Message> initial = new ArrayList<>();

        // 生成初始数据
        for (int i = 0; i < 30; i++) {
            Message msg = new Message();
            msg.userId = "init_" + i;
            msg.originalName = "初始好友 " + i;
            msg.content = "你好，这是一条测试消息...";
            msg.timestamp = System.currentTimeMillis() - (i * 1000 * 60 * 10);
            msg.unreadCount = i < 3 ? 1 : 0;
            msg.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + i;

            // 添加聊天记录
            List<ChatMessage> chatList = Arrays.asList(
                    new ChatMessage("你好，我是 " + msg.originalName, ChatMessage.TYPE_RECEIVED, msg.timestamp),
                    new ChatMessage("很高兴认识你！", ChatMessage.TYPE_SENT, msg.timestamp + 5000),
                    new ChatMessage("有什么我可以帮你的吗？", ChatMessage.TYPE_RECEIVED, msg.timestamp + 10000)
            );
            msg.setChatHistory(chatList);

            // 设置最新消息
            msg.setLatestContent(chatList.get(chatList.size() - 1).getContent());
            msg.setLatestTimestamp(chatList.get(chatList.size() - 1).getTimestamp());
            initial.add(msg);
        }

        // 特殊处理"三哥"和"小助手"的消息
        Message sanGe = new Message();
        sanGe.userId = "user_02";
        sanGe.originalName = "张三";
        sanGe.remarkName = "三哥";
        sanGe.content = "今晚出来吃饭吗？";
        sanGe.timestamp = System.currentTimeMillis() - 60 * 1000 * 5;
        sanGe.unreadCount = 3;
        sanGe.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=zhangsan";

        List<ChatMessage> sanGeChatList = Arrays.asList(
                new ChatMessage("你好！", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 300000),
                new ChatMessage("在干嘛呢？", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 240000),
                new ChatMessage("今晚出来吃饭吗？", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 180000)
        );
        sanGe.setChatHistory(sanGeChatList);
        sanGe.setLatestContent(sanGeChatList.get(sanGeChatList.size() - 1).getContent());
        sanGe.setLatestTimestamp(sanGeChatList.get(sanGeChatList.size() - 1).getTimestamp());
        initial.add(sanGe);

        // 添加"抖音小助手"的消息
        Message douyinHelper = new Message();
        douyinHelper.userId = "sys_01";
        douyinHelper.originalName = "抖音小助手";
        douyinHelper.content = "欢迎来到抖音简版！点击这里查看更多内容。";
        douyinHelper.timestamp = System.currentTimeMillis(); // 最新时间
        douyinHelper.unreadCount = 1;
        douyinHelper.type = 0; // 文本消息
        douyinHelper.avatarUrl = "https://p3-pc.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_f4d6c770c3298117765955030283626d.jpeg";

        List<ChatMessage> helperChatList = Arrays.asList(
                new ChatMessage("欢迎来到抖音简版！点击这里查看更多内容。", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis())
        );
        douyinHelper.setChatHistory(helperChatList);
        douyinHelper.setLatestContent(helperChatList.get(helperChatList.size() - 1).getContent());
        douyinHelper.setLatestTimestamp(helperChatList.get(helperChatList.size() - 1).getTimestamp());
        initial.add(douyinHelper);

        db.messageDao().insertAll(initial);
    }

    public void initMockData() {
        loadMoreMockData(1, 20); // 复用loadMoreMockData的逻辑
    }
}