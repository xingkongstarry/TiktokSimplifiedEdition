package com.starry.tiktoksimplifiededition.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.db.AppDatabase;
import com.starry.tiktoksimplifiededition.data.model.Message;

public class RemarkActivity extends AppCompatActivity {
    private Message message;
    private EditText etRemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);

        message = (Message) getIntent().getSerializableExtra("msg_data");

        TextView tvOriginalName = findViewById(R.id.tv_original_name);
        etRemark = findViewById(R.id.et_remark);
        Button btnSave = findViewById(R.id.btn_save);

        if (message != null) {
            tvOriginalName.setText("原名: " + message.originalName);
            if (message.remarkName != null) {
                etRemark.setText(message.remarkName);
            }
        }

        btnSave.setOnClickListener(v -> {
            String newRemark = etRemark.getText().toString().trim();
            message.remarkName = newRemark;

            new Thread(() -> {
                AppDatabase.getDatabase(this).messageDao().update(message);
                runOnUiThread(() -> {
                    // 修改完成后，将更新后的message对象返回给ChatActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated_msg_data", message);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }).start();
        });

        // 获取返回按钮并设置点击事件
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish()); // 返回上一个界面
    }
}