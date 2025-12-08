package com.starry.tiktoksimplifiededition.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.starry.tiktoksimplifiededition.data.model.Message;

@Database(entities = {Message.class}, version = 1, exportSchema = false)
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
                            .allowMainThreadQueries()
                            // 添加迁移配置
                            .addMigrations(MIGRATION_1_2)
                            // 去掉 fallbackToDestructiveMigration，否则迁移失败会清空数据
                            //.fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // 定义迁移策略：从版本 1 升级到 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 尝试添加列，如果列已存在（抛出异常），则忽略
            try {
                database.execSQL("ALTER TABLE message_table ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0");
            } catch (Exception e) {
                // 列可能已经存在了，打印日志并继续，不要崩溃
                e.printStackTrace();
            }
        }
    };
}