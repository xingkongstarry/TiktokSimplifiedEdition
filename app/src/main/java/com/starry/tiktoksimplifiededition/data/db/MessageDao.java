package com.starry.tiktoksimplifiededition.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.starry.tiktoksimplifiededition.data.model.Message;
import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM message_table ORDER BY timestamp DESC")
    LiveData<List<Message>> getAllMessages();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Message> messages);

    @Update
    void update(Message message);

    @Query("SELECT COUNT(*) FROM message_table")
    int getCount();

    @Query("SELECT * FROM message_table WHERE userId = :userId LIMIT 1")
    Message findByUserId(String userId);

    @Query("SELECT * FROM message_table WHERE userId = :userId LIMIT 1")
    LiveData<Message> getMessageLiveData(String userId);
}