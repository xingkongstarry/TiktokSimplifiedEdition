// ChatActivity.java
package com.starry.tiktoksimplifiededition.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private static final int REQUEST_CODE_REMARK = 1001;
    private androidx.activity.result.ActivityResultLauncher<Intent> remarkActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message = (Message) getIntent().getSerializableExtra("msg_data");

        // 初始化 RecyclerView
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        adapter = new ChatAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // 加载聊天记录：优先使用 message 对象中的记录
        if (message != null && message.getChatHistory() != null && !message.getChatHistory().isEmpty()) {
            chatMessages.addAll(message.getChatHistory());
            adapter.setMessages(chatMessages);
            rvMessages.scrollToPosition(chatMessages.size() - 1); // 滚动到底部
        } else {
            // 如果没有，则加载模拟数据
            loadMockChatData();
        }

        // 设置标题栏信息
        if (message != null) {
            // 加载头像
            Glide.with(this)
                    .load(message.avatarUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .transform(new CircleCrop())
                    .into((ImageView)findViewById(R.id.iv_avatar));

            // 设置名称
            ((android.widget.TextView)findViewById(R.id.tv_name)).setText(message.getDisplayName());
        }

        // 获取返回按钮并设置点击事件
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish()); // 返回上一个界面

        // 注册 ActivityResultLauncher 替代 startActivityForResult
        remarkActivityLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Message updatedMessage = (Message) data.getSerializableExtra("updated_msg_data");
                            if (updatedMessage != null) {
                                message = updatedMessage;
                                ((android.widget.TextView)findViewById(R.id.tv_name)).setText(message.getDisplayName());
                            }
                        }
                    }
                }
        );

        // 获取菜单按钮并设置点击事件
        ImageView ivMenu = findViewById(R.id.iv_menu);
        ivMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, RemarkActivity.class);
            intent.putExtra("msg_data", message);
            remarkActivityLauncher.launch(intent); // 使用新的 launcher 启动
        });

        // 发送消息按钮
        ImageButton btnSend = findViewById(R.id.btn_send);
        EditText etMessage = findViewById(R.id.et_message);
        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                ChatMessage sentMsg = new ChatMessage(content, ChatMessage.TYPE_SENT, System.currentTimeMillis());
                chatMessages.add(sentMsg);
                adapter.notifyItemInserted(chatMessages.size() - 1);
                rvMessages.scrollToPosition(chatMessages.size() - 1);
                etMessage.setText("");

                // 更新数据库中的消息记录
                updateMessageInDatabase(sentMsg);

                // 模拟回复
                simulateReply();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_REMARK && resultCode == RESULT_OK) {
            // 接收更新后的message对象
            Message updatedMessage = (Message) data.getSerializableExtra("updated_msg_data");
            if (updatedMessage != null) {
                message = updatedMessage;
                // 更新界面显示的名称
                ((android.widget.TextView)findViewById(R.id.tv_name)).setText(message.getDisplayName());
            }
        }
    }
    private void loadMockChatData() {
        // 添加一些初始的聊天记录
        chatMessages.add(new ChatMessage("你好！", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 30000));
        chatMessages.add(new ChatMessage("你好，有什么可以帮助你的吗？", ChatMessage.TYPE_SENT, System.currentTimeMillis() - 25000));
        chatMessages.add(new ChatMessage("我想了解一下你们的产品。", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis() - 20000));
        chatMessages.add(new ChatMessage("当然可以，请问您具体想了解哪方面呢？", ChatMessage.TYPE_SENT, System.currentTimeMillis() - 15000));

        adapter.setMessages(chatMessages);
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        rvMessages.scrollToPosition(chatMessages.size() - 1); // 滚动到底部
    }

    private void updateMessageInDatabase(ChatMessage newChatMessage) {
        new Thread(() -> {
            // 更新Message对象的chatHistory
            if (message.getChatHistory() == null) {
                message.setChatHistory(new ArrayList<>());
            }
            message.getChatHistory().add(newChatMessage);

            // 更新最新消息内容和时间
            message.setLatestContent(newChatMessage.getContent());
            message.setLatestTimestamp(newChatMessage.getTimestamp());

            // 更新数据库
            AppDatabase.getDatabase(this).messageDao().update(message);
        }).start();
    }

    private void simulateReply() {
        if (message != null && "抖音小助手".equals(message.originalName)) {
            // 模拟延迟后自动回复
            new android.os.Handler().postDelayed(() -> {
                RecyclerView rvMessages = findViewById(R.id.rv_messages);
                ChatMessage replyMsg = new ChatMessage("[自动回复] 您好，已收到您的消息，我们会尽快回复您。", ChatMessage.TYPE_RECEIVED, System.currentTimeMillis());
                chatMessages.add(replyMsg);
                adapter.notifyItemInserted(chatMessages.size() - 1);
                rvMessages.scrollToPosition(chatMessages.size() - 1);

                // 更新数据库中的消息记录
                updateMessageInDatabase(replyMsg);
            }, 1000); // 1秒后回复
        }
    }
}
