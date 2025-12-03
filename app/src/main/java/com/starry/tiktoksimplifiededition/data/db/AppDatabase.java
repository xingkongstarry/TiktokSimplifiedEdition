package com.starry.tiktoksimplifiededition.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.starry.tiktoksimplifiededition.data.model.Message;

@Database(entities = {Message.class}, version = 2, exportSchema = false)
@TypeConverters({ChatMessageConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tiktok_lite_db")
                            .allowMainThreadQueries() // 演示用，允许主线程查询
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}