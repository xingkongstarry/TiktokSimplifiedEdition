package com.starry.tiktoksimplifiededition.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.starry.tiktoksimplifiededition.data.model.Message;
import com.starry.tiktoksimplifiededition.data.repository.MessageRepository;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final MessageRepository repository;
    private final LiveData<List<Message>> allMessages;
    private boolean isDataInitialized = false;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);
        // 确保在主线程中初始化数据
        allMessages = repository.getAllMessages();
    }

    public LiveData<List<Message>> getAllMessages() {
        if (!isDataInitialized) {
            isDataInitialized = true;
            new Thread(() -> repository.initializeDataIfNeeded()).start();
        }
        return allMessages;
    }

    public void loadMore(int page) {
        new Thread(() -> repository.loadMoreMockData(page, 10)).start();
    }

    public void refresh() {
        // 简单模拟刷新：重新插入一点数据或重新请求
        // 这里留空，因为 LiveData 会自动处理数据源变化
    }

    public void forceInitData() {
        new Thread(() -> {
            repository.initMockData();
        }).start();
    }
}