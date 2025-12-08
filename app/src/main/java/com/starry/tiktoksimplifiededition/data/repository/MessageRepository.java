// MessageRepository.java

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
    }

    /**
     * 初始化数据
     */
    public synchronized void initializeDataIfNeeded() {
        if (isInitializing) return;

        new Thread(() -> {
            if (db.messageDao().getCount() == 0) {
                isInitializing = true;
                try {
                    initMockData();
                } finally {
                    isInitializing = false;
                }
            }
        }).start();
    }

    /**
     * 获取所有消息
     * @return
     */
    public LiveData<List<Message>> getAllMessages() {
        return Transformations.map(db.messageDao().getAllMessages(), messages -> {
            if (messages != null) {
                List<Message> sortedMessages = new ArrayList<>(messages);
                // 使用 Message 类中更新后的 compareTo (支持置顶)
                sortedMessages.sort(Message::compareTo);
                return sortedMessages;
            }
            return messages;
        });
    }

    /**
     * 重构初始化方法，将所有模拟数据和系统用户一次性创建
     */
    private void initMockData() {
        List<Message> initial = new ArrayList<>();

        // 1. 生成初始好友数据
        for (int i = 0; i < 30; i++) {
            Message msg = new Message();
            msg.userId = "init_" + i;
            msg.originalName = "初始好友 " + i;
            msg.content = "你好，这是一条测试消息...";
            msg.timestamp = System.currentTimeMillis() - (i * 1000L * 60 * 10);
            msg.unreadCount = i < 3 ? 1 : 0;
            msg.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + i;
            msg.type = 0; // 文本消息

            List<ChatMessage> chatList = Arrays.asList(
                    new ChatMessage("你好，我是 " + msg.originalName, ChatMessage.TYPE_RECEIVED, msg.timestamp),
                    new ChatMessage("很高兴认识你！", ChatMessage.TYPE_SENT, msg.timestamp + 5000),
                    new ChatMessage("有什么我可以帮你的吗？", ChatMessage.TYPE_RECEIVED, msg.timestamp + 10000)
            );
            msg.setChatHistory(chatList);
            msg.setLatestContent(chatList.get(chatList.size() - 1).getContent());
            msg.setLatestTimestamp(chatList.get(chatList.size() - 1).getTimestamp());
            initial.add(msg);
        }

        // 2. 特殊处理 "三哥"
        Message sanGe = new Message();
        sanGe.userId = "user_02";
        sanGe.originalName = "张三";
        sanGe.remarkName = "三哥";
        sanGe.content = "今晚出来吃饭吗？";
        sanGe.timestamp = System.currentTimeMillis() - 60L * 1000 * 5;
        sanGe.unreadCount = 3;
        sanGe.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=zhangsan";
        sanGe.type = 0;
        List<ChatMessage> sanGeChatList = Arrays.asList(
                new ChatMessage("你好！", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 300000),
                new ChatMessage("在干嘛呢？", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 240000),
                new ChatMessage("今晚出来吃饭吗？", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 180000)
        );
        sanGe.setChatHistory(sanGeChatList);
        sanGe.setLatestContent(sanGeChatList.get(sanGeChatList.size() - 1).getContent());
        sanGe.setLatestTimestamp(sanGeChatList.get(sanGeChatList.size() - 1).getTimestamp());
        initial.add(sanGe);

        // 3. 添加 "抖音小助手"
        Message douyinHelper = new Message();
        douyinHelper.userId = "sys_01";
        douyinHelper.originalName = "抖音小助手";
        douyinHelper.content = "欢迎来到抖音简版！点击这里查看更多内容。";
        douyinHelper.timestamp = System.currentTimeMillis();
        douyinHelper.unreadCount = 1;
        douyinHelper.type = 0;
        douyinHelper.avatarUrl = "https://p3-pc.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_f4d6c770c3298117765955030283626d.jpeg";
        List<ChatMessage> helperChatList = Arrays.asList(
                new ChatMessage("欢迎来到抖音简版！点击这里查看更多内容。", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis())
        );
        douyinHelper.setChatHistory(helperChatList);
        douyinHelper.setLatestContent(helperChatList.get(helperChatList.size() - 1).getContent());
        douyinHelper.setLatestTimestamp(helperChatList.get(helperChatList.size() - 1).getTimestamp());
        initial.add(douyinHelper);

        // 4. 创建空的系统通知用户
        Message systemMessage = new Message();
        systemMessage.userId = "system_notification";
        systemMessage.originalName = "系统通知";
        systemMessage.avatarUrl = "https://api.dicebear.com/7.x/bottts/png?seed=system";
        systemMessage.type = 2; // 运营类消息
        systemMessage.setChatHistory(new ArrayList<>());
        systemMessage.setLatestContent("暂无系统通知");
        systemMessage.setLatestTimestamp(System.currentTimeMillis() - 1000000); //给一个较早的时间戳，让它排在后面
        systemMessage.unreadCount = 0;
        initial.add(systemMessage);

        db.messageDao().insertAll(initial);
    }

    /**
     * 插入一个随机的系统消息
     */
    public void insertRandomSystemMessage() {
        new Thread(() -> {
            try {
                Message systemMsg = db.messageDao().findByUserId("system_notification");
                if (systemMsg == null) {
                    Log.w("MessageRepository", "System notification user not found!");
                    return;
                }

                // 随机决定消息类型：0=文本, 1=图片, 2=运营
                int messageType = new java.util.Random().nextInt(3);

                String content;
                ChatMessage chatMsg;
                long timestamp = System.currentTimeMillis();

                switch (messageType) {
                    case 1: // 图片消息
                        content = "新功能上线啦，快来看看！";
                        String imageUrl = "https://images.unsplash.com/photo-1511367461989-f85a21fda167?w=500&q=80";
                        chatMsg = new ChatMessage(content, ChatMessage.TYPE_IMAGE_RECEIVED, timestamp);
                        chatMsg.setImageUrl(imageUrl);
                        //chatMsg.setType(1); // 标记为图片类型

                        // 更新主记录
                        systemMsg.setLatestContent(content);
                        systemMsg.type = 1; // 主界面根据此类型显示 "[图片]"
                        break;

                    case 2: // 运营消息
                        content = "恭喜你获得专属奖励，点击领取！";
                        chatMsg = new ChatMessage(content, ChatMessage.TYPE_OPERATION_RECEIVED, timestamp);
                        //chatMsg.setType(2); // 标记为运营类型

                        // 更新主记录
                        systemMsg.setLatestContent(content);
                        systemMsg.type = 2; // 主界面根据此类型显示 "[领取奖励]"
                        break;

                    case 0: // 文本消息
                    default:
                        String[] messages = {
                                "系统维护通知：服务器将在今晚02:00-04:00进行维护",
                                "安全提醒：请勿向陌生人透露个人信息"
                        };
                        content = messages[new java.util.Random().nextInt(messages.length)];
                        chatMsg = new ChatMessage(content, ChatMessage.TYPE_RECEIVED, timestamp);
                        chatMsg.setType(0); // 标记为文本类型

                        // 更新主记录
                        systemMsg.setLatestContent(content);
                        systemMsg.type = 0; // 文本类型
                        break;
                }

                if (systemMsg.getChatHistory() == null) {
                    systemMsg.setChatHistory(new ArrayList<>());
                }
                systemMsg.getChatHistory().add(chatMsg);

                systemMsg.setLatestTimestamp(timestamp);
                systemMsg.unreadCount++;

                db.messageDao().update(systemMsg);

            } catch (Exception e) {
                Log.e("MessageRepository", "Failed to insert system message", e);
            }
        }).start();
    }
}