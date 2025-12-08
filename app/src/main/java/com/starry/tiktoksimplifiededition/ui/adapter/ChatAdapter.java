package com.starry.tiktoksimplifiededition.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.model.ChatMessage;
import com.starry.tiktoksimplifiededition.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    public void setMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    /**
     * 创建RecyclerView的ViewHolder实例
     * 根据不同的消息类型创建对应类型的ViewHolder
     *
     * @param parent 父容器ViewGroup
     * @param viewType 视图类型，决定创建哪种类型的ViewHolder
     * @return 返回对应类型的RecyclerView.ViewHolder实例
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ChatMessage.TYPE_IMAGE_RECEIVED:
                return new ImageChatViewHolder(inflater.inflate(R.layout.item_chat_image_received, parent, false));
            case ChatMessage.TYPE_OPERATION_RECEIVED:
                return new OperationChatViewHolder(inflater.inflate(R.layout.item_chat_operation_received, parent, false));
            case ChatMessage.TYPE_SENT:
                return new TextChatViewHolder(inflater.inflate(R.layout.item_chat_sent, parent, false));
            case ChatMessage.TYPE_RECEIVED:
            default:
                return new TextChatViewHolder(inflater.inflate(R.layout.item_chat_received, parent, false));
        }
    }

    /**
     * 绑定RecyclerView中指定位置的数据到对应的ViewHolder
     *
     * @param holder ViewHolder实例，用于显示聊天消息内容
     * @param position 当前数据在消息列表中的位置索引
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof TextChatViewHolder) {
            ((TextChatViewHolder) holder).bind(msg);
        } else if (holder instanceof ImageChatViewHolder) {
            ((ImageChatViewHolder) holder).bind(msg);
        } else if (holder instanceof OperationChatViewHolder) {
            ((OperationChatViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {return messages.size();}

    /**
     * 聊天文本消息 ViewHolder
     */
    static class TextChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        TextChatViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
        void bind(ChatMessage msg) {
            tvContent.setText(msg.getContent());
            tvTime.setText(TimeUtils.getFriendlyTime(msg.getTimestamp()));
        }
    }

    /**
     * 聊天图片消息 ViewHolder
     */
    static class ImageChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime;
        ImageChatViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_chat_image);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
        void bind(ChatMessage msg) {
            tvTime.setText(TimeUtils.getFriendlyTime(msg.getTimestamp()));
            Glide.with(itemView.getContext())
                    .load(msg.getImageUrl())
                    .transform(new RoundedCorners(16))
                    .into(ivImage);
        }
    }

    /**
     * 聊天操作消息 ViewHolder
     */
    static class OperationChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        Button btnAction;
        OperationChatViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnAction = itemView.findViewById(R.id.btn_chat_action);
        }
        void bind(ChatMessage msg) {
            tvContent.setText(msg.getContent());
            tvTime.setText(TimeUtils.getFriendlyTime(msg.getTimestamp()));
            // 设置按钮点击事件
            btnAction.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "奖励领取成功！", Toast.LENGTH_SHORT).show()
            );
        }
    }
}