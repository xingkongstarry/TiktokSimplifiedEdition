// MainViewModel.java

package com.starry.tiktoksimplifiededition.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.starry.tiktoksimplifiededition.data.model.Message;
import com.starry.tiktoksimplifiededition.data.repository.MessageRepository;
import com.starry.tiktoksimplifiededition.utils.Resource;

import java.util.List;
import java.util.Random;

public class MainViewModel extends AndroidViewModel {
    private final MessageRepository repository;
    private final LiveData<List<Message>> allMessages;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable systemMessageRunnable;

    private final MutableLiveData<Resource<List<Message>>> messagesResource = new MutableLiveData<>();

    public LiveData<Resource<List<Message>>> getMessagesResource() {
        return messagesResource;
    }

    public void loadData() {

        messagesResource.setValue(Resource.loading(null));

        Runnable timeoutRunnable = () -> {
            if (messagesResource.getValue() != null &&
                    messagesResource.getValue().status == Resource.Status.LOADING) {
                messagesResource.postValue(Resource.error("请求超时 (5s)", null));
            }
        };
        handler.postDelayed(timeoutRunnable, 5000);

        new Thread(() -> {
            try {
                long delay = 1000 + new java.util.Random().nextInt(5500);
                Thread.sleep(delay);

                handler.removeCallbacks(timeoutRunnable);

                // 模拟随机失败
                if (new java.util.Random().nextInt(10) < 2) {
                    messagesResource.postValue(Resource.error("模拟网络错误", null));
                    return;
                }
                repository.initializeDataIfNeeded();
                messagesResource.postValue(Resource.success(null));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);

        // 触发一次初始化检查
        repository.initializeDataIfNeeded();

        allMessages = repository.getAllMessages();

        // 启动系统消息定时器
        startSystemMessageScheduler();
    }

    public LiveData<List<Message>> getAllMessages() {
        return allMessages;
    }

    public void loadMore(int page) {
    }

    // 用于测试按钮，强制重新生成数据（注意：实际APP中这会清空数据再生成）
    public void forceInitData() {
        // 这里的逻辑需要配合数据库清空操作，或者直接调用初始化
        repository.initializeDataIfNeeded();
    }

    public void refresh() {
    }

    private void startSystemMessageScheduler() {
        systemMessageRunnable = new Runnable() {
            @Override
            public void run() {
                insertRandomSystemMessage();
                // 每隔5到10秒随机发送
                handler.postDelayed(this, 5000 + new Random().nextInt(10000));
            }
        };
        handler.postDelayed(systemMessageRunnable, 5000+ new Random().nextInt(10000));
    }

    private void insertRandomSystemMessage() {
        String[] messages = {
                "系统维护通知：服务器将在今晚02:00-04:00进行维护",
                "新功能上线：现在可以发送图片和语音消息了",
                "安全提醒：请勿向陌生人透露个人信息",
                "活动通知：参与活动可获得丰厚奖励"
        };

        Random random = new Random();
        String content = messages[random.nextInt(messages.length)];

        repository.insertRandomSystemMessage();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (systemMessageRunnable != null) {
            handler.removeCallbacks(systemMessageRunnable);
        }
    }
}