package com.starry.tiktoksimplifiededition.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button; // 记得导入 Button
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast; // 记得导入 Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.db.AppDatabase;
import com.starry.tiktoksimplifiededition.data.model.Message;
import com.starry.tiktoksimplifiededition.ui.adapter.MessageAdapter;
import com.starry.tiktoksimplifiededition.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private MessageAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private EditText etSearch;
    private RecyclerView recyclerView; // 提升作用域以便其他地方访问
    private int currentPage = 1;
    private List<Message> allMessages = new ArrayList<>();
    private List<Message> filteredMessages = new ArrayList<>();

    private View skeletonLayout;
    private View errorLayout;
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view); // 提前初始化
        emptyView = findViewById(R.id.empty_view);
        Button btnTestAdd = findViewById(R.id.btn_test_add);
        etSearch = findViewById(R.id.et_search);

        // 初始化 Adapter
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 初始化新视图
        skeletonLayout = findViewById(R.id.layout_skeleton);
        errorLayout = findViewById(R.id.layout_error);
        btnRetry = findViewById(R.id.btn_retry);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 统一的数据观察者
        viewModel.getAllMessages().observe(this, messages -> {
            swipeRefreshLayout.setRefreshing(false);

            // 更新原始数据源
            if (messages != null) {
                allMessages.clear();
                allMessages.addAll(messages);
            }

            // 执行过滤逻辑
            filterMessages(etSearch.getText().toString());

            // 处理空状态和滚动行为
            if (allMessages.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                boolean hasNewMessage = false;
                if (adapter.getItemCount() > 0 && !messages.isEmpty()) {
                    for (int i = 0; i < Math.min(messages.size(), adapter.getItemCount()); i++) {
                        Message oldMsg = adapter.getMessageAt(i);
                        Message newMsg = messages.get(i);
                        if (newMsg != null && oldMsg != null &&
                                newMsg.unreadCount > oldMsg.unreadCount) {
                            hasNewMessage = true;
                            break;
                        }
                    }
                } else if (adapter.getItemCount() == 0 && !messages.isEmpty()) {
                    hasNewMessage = true;
                }

                List<Message> messageList = new ArrayList<>(messages);
                adapter.setMessages(messageList);

                if (hasNewMessage) {
                    recyclerView.post(() -> layoutManager.scrollToPositionWithOffset(0, 0));
                }
            }
        });

        // 统一的数据观察者
        viewModel.getMessagesResource().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    // 加载中：显示骨架屏，隐藏其他
                    skeletonLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    // 加载成功：隐藏骨架屏和错误页
                    skeletonLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    // 显示内容 (根据是否有数据决定显示列表还是空页)
                    updateContentVisibility();
                    break;

                case ERROR:
                    // 加载失败：显示错误页，隐藏其他
                    skeletonLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // 重试按钮
        btnRetry.setOnClickListener(v -> viewModel.loadData());

        // 首次进入加载数据
        viewModel.loadData();

        // 测试添加按钮点击事件
        btnTestAdd.setOnClickListener(v -> {
            Toast.makeText(this, "正在生成数据...", Toast.LENGTH_SHORT).show();
            viewModel.forceInitData();
        });

        // 点击空视图也可以生成
        emptyView.setOnClickListener(v -> viewModel.forceInitData());

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            viewModel.refresh(); // 调用 ViewModel 的方法
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "刷新完成", Toast.LENGTH_SHORT).show();
                }
            }, 1500); // 1.5秒后停止，确保比ViewModel的模拟延时长
        });

        // 列表滚动加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMore(++currentPage);
                }
            }
        });

        // 设置 Item 点击跳转
        adapter.setOnItemClickListener(msg -> {
            if (msg.unreadCount > 0) {
                msg.unreadCount = 0;
                new Thread(() -> {
                    AppDatabase.getDatabase(this).messageDao().update(msg);
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }).start();
            }

            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("msg_data", msg);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // 搜索输入监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterMessages(s.toString());
            }
        });

        adapter.setOnItemLongClickListener(msg -> {
            // 1. 切换置顶状态
            msg.isPinned = !msg.isPinned;

            // 2. 更新数据库 (在子线程)
            new Thread(() -> {
                AppDatabase.getDatabase(this).messageDao().update(msg);

                // 3. UI 提示
                runOnUiThread(() -> {
                    String status = msg.isPinned ? "已置顶" : "已取消置顶";
                    Toast.makeText(MainActivity.this, status, Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
    }

    /**
     * 根据搜索条件过滤消息
     */
    private void filterMessages(String query) {
        filteredMessages.clear();

        if (query.isEmpty()) {
            filteredMessages.addAll(allMessages);
        } else {
            // 统一转小写进行匹配
            String lowerQuery = query.toLowerCase();
            for (Message message : allMessages) {
                boolean nameMatch = message.getDisplayName() != null &&
                        message.getDisplayName().toLowerCase().contains(lowerQuery);

                // 检查最新内容或原始内容
                String content = message.getLatestContent() != null ? message.getLatestContent() : message.content;
                // 对于特殊类型的消息，也需要检查其替换文本
                if (message.type == 1) content = "[图片]";
                if (message.type == 2) content = "[领取奖励]";

                boolean contentMatch = content != null && content.toLowerCase().contains(lowerQuery);

                if (nameMatch || contentMatch) {
                    filteredMessages.add(message);
                }
            }
        }
        updateUI(query);
    }

    /**
     * 更新 UI
     */
    private void updateUI(String searchQuery) {
        if (filteredMessages.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setMessages(new ArrayList<>(filteredMessages));
            adapter.setSearchQuery(searchQuery); // 设置搜索关键词以启用高亮
        }
    }

    /**
     * 更新内容可见性
     */
    private void updateContentVisibility() {
        // 如果 Skeleton 或 Error 正在显示，则不操作，避免冲突
        if (skeletonLayout.getVisibility() == View.VISIBLE ||
                errorLayout.getVisibility() == View.VISIBLE) {
            return;
        }

        if (adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
}