package com.starry.tiktoksimplifiededition.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // 记得导入 Button
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
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        Button btnTestAdd = findViewById(R.id.btn_test_add);

        // 初始化 Adapter
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 观察数据
        viewModel.getAllMessages().observe(this, messages -> {
            swipeRefreshLayout.setRefreshing(false);
            if (messages == null || messages.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setMessages(new ArrayList<>(messages)); // 创建新列表避免引用问题

                // 确保布局完成后再滚动
                recyclerView.post(() -> {
                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
                        layoutManager.scrollToPosition(0);
                    }
                });
            }
        });

        // 【新增】按钮点击事件：强制生成数据
        btnTestAdd.setOnClickListener(v -> {
            Toast.makeText(this, "正在生成数据...", Toast.LENGTH_SHORT).show();
            // 调用 ViewModel 的方法去插数据 (需要确保你在 ViewModel 里写了这个方法)
            viewModel.forceInitData();
        });

        // 点击空视图也可以生成
        emptyView.setOnClickListener(v -> {
            viewModel.forceInitData();
        });

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.postDelayed(() -> {
                viewModel.refresh();
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        // 列表滚动加载
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMore(++currentPage);
                }
            }
        });

        // 跳转逻辑
        adapter.setOnItemClickListener(msg -> {
            if(msg.unreadCount > 0) {
                msg.unreadCount = 0;
                // 使用异步方式更新数据库
                new Thread(() -> {
                    AppDatabase.getDatabase(this).messageDao().update(msg);
                    // 注意：这里更新UI需要回到主线程
                }).start();
            }
            // 跳转到聊天界面
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("msg_data", msg);
            startActivity(intent);
        });

        viewModel.getAllMessages().observe(this, messages -> {
            swipeRefreshLayout.setRefreshing(false);
            if (messages == null || messages.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                // 创建新的列表副本以确保数据更新
                List<Message> messageList = new ArrayList<>(messages);
                adapter.setMessages(messageList);

                // 强制刷新适配器
                adapter.notifyDataSetChanged();
            }
        });

    }
}