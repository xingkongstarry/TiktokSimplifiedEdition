package com.starry.tiktoksimplifiededition.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.db.AppDatabase;
import com.starry.tiktoksimplifiededition.data.model.ChatMessage;
import com.starry.tiktoksimplifiededition.data.model.Message;
import com.starry.tiktoksimplifiededition.ui.adapter.ChatAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private Message message;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message = (Message) getIntent().getSerializableExtra("msg_data");
        if (message == null) {
            finish();
            return;
        }

        rvMessages = findViewById(R.id.rv_messages);
        adapter = new ChatAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        if (message.getChatHistory() != null) {
            chatMessages.addAll(message.getChatHistory());
            adapter.setMessages(chatMessages);
            if (!chatMessages.isEmpty()) {
                rvMessages.scrollToPosition(chatMessages.size() - 1);
            }
        }


        AppDatabase.getDatabase(this).messageDao().getMessageLiveData(message.userId)
                .observe(this, updatedMessage -> {
                    if (updatedMessage != null && updatedMessage.getChatHistory() != null) {
                        // 更新本地对象引用
                        message = updatedMessage;

                        // 实时更新标题栏显示的昵称
                        // 这样从备注页修改完回来，或者后台更新了信息，标题会自动变
                        TextView tvName = findViewById(R.id.tv_name);
                        if (tvName != null) {
                            tvName.setText(updatedMessage.getDisplayName());
                        }

                        // 更新聊天列表 (只有当消息数量增加时才刷新，避免不必要的重绘)
                        if (updatedMessage.getChatHistory().size() > chatMessages.size()) {
                            chatMessages.clear();
                            chatMessages.addAll(updatedMessage.getChatHistory());
                            adapter.setMessages(chatMessages);
                            // 滚动到底部，看到最新消息
                            rvMessages.scrollToPosition(chatMessages.size() - 1);
                        }
                    }
                });

        setupViews();      // 设置头部视图和点击事件
        setupSendButton(); // 设置发送按钮逻辑
    }


    // 专门处理"离开页面"的逻辑，静默清除未读数
    @Override
    protected void onStop() {
        super.onStop();
        if (message != null) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(this);
                // 必须查最新的，因为后台可能刚改了它
                Message latestMsg = db.messageDao().findByUserId(message.userId);
                // 如果有未读，将其清零并保存
                if (latestMsg != null && latestMsg.unreadCount > 0) {
                    latestMsg.unreadCount = 0;
                    db.messageDao().update(latestMsg);
                }
            }).start();
        }
    }

    /**
     * 设置视图 (头部信息、返回按钮、菜单按钮)
     */
    private void setupViews() {
        // 1. 系统通知特殊处理 (隐藏菜单、禁用输入)
        if ("system_notification".equals(message.userId)) {
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
            findViewById(R.id.et_message).setEnabled(false);
            findViewById(R.id.btn_send).setEnabled(false);
        }

        // 2. 加载头像
        Glide.with(this)
                .load(message.avatarUrl)
                .transform(new CircleCrop())
                .into((ImageView)findViewById(R.id.iv_avatar));

        // 3. 设置初始标题
        ((TextView)findViewById(R.id.tv_name)).setText(message.getDisplayName());

        // 4. 返回按钮事件
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 5. 【修复】菜单按钮事件 -> 跳转备注页
        ImageView ivMenu = findViewById(R.id.iv_menu);
        ivMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, RemarkActivity.class);
            intent.putExtra("msg_data", message);
            // 直接跳转，修改完数据库后，LiveData 会自动刷新界面，不需要 setResult
            startActivity(intent);
        });
    }

    /**
     * 发送按钮的点击事件处理
     */
    private void setupSendButton() {
        ImageButton btnSend = findViewById(R.id.btn_send);
        EditText etMessage = findViewById(R.id.et_message);

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                ChatMessage sentMsg = new ChatMessage(content, ChatMessage.TYPE_SENT, System.currentTimeMillis());

                // 为了极速响应，先在本地列表添加并刷新 (双重保险)
                chatMessages.add(sentMsg);
                adapter.notifyItemInserted(chatMessages.size() - 1);
                rvMessages.scrollToPosition(chatMessages.size() - 1);
                etMessage.setText("");

                // 异步写入数据库 (写入后 LiveData 也会触发一次刷新，保证一致性)
                updateMessageInDatabase(sentMsg);

                // 触发模拟回复
                simulateReply();
            }
        });
    }

    /**
     * 更新数据库中的消息记录
     */
    private void updateMessageInDatabase(ChatMessage newChatMessage) {
        new Thread(() -> {
            // 注意：这里我们只负责更新内容，unreadCount 的清零交给 onStop
            if (message.getChatHistory() == null) message.setChatHistory(new ArrayList<>());
            message.getChatHistory().add(newChatMessage);
            message.setLatestContent(newChatMessage.getContent());
            message.setLatestTimestamp(newChatMessage.getTimestamp());
            AppDatabase.getDatabase(this).messageDao().update(message);
        }).start();
    }

    /**
     * 模拟回复逻辑
     */
    private void simulateReply() {
        if (message != null && "抖音小助手".equals(message.originalName)) {
            new android.os.Handler().postDelayed(() -> {
                ChatMessage replyMsg = new ChatMessage("[自动回复] 收到您的反馈，我们会尽快处理。", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis());
                // 本地模拟回复直接写库，LiveData 会自动刷新界面显示回复
                updateMessageInDatabase(replyMsg);
            }, 1000);
        }
    }
}