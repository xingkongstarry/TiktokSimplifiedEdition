package com.starry.tiktoksimplifiededition.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.model.Message;
import com.starry.tiktoksimplifiededition.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Message message);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime, tvBadge;
        ImageView ivAvatar;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvBadge = itemView.findViewById(R.id.tv_badge);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && position < messages.size()) {
                    listener.onItemClick(messages.get(position));
                }
            });
        }

        void bind(Message msg) {
            // 关键修改：在绑定新数据前先清除旧数据，防止视图复用导致的显示异常
            tvName.setText("");
            tvContent.setText("");
            tvTime.setText("");
            tvBadge.setVisibility(View.GONE);
            ivAvatar.setImageResource(R.drawable.ic_launcher_background);

            if (msg == null) return;

            // 设置实际数据
            tvName.setText(msg.getDisplayName());

            String displayContent = "";
            if (msg.getLatestContent() != null && !msg.getLatestContent().isEmpty()) {
                displayContent = msg.getLatestContent();
            } else if (msg.content != null) {
                displayContent = msg.content;
            }
            tvContent.setText(displayContent);

            // 显示时间
            long displayTime = msg.getLatestTimestamp() > 0 ?
                    msg.getLatestTimestamp() : msg.timestamp;
            if (displayTime > 0) {
                // 强制更新时间显示
                String timeText = TimeUtils.getFriendlyTime(displayTime);
                tvTime.setText(timeText);
            } else {
                tvTime.setText("");
            }

            // 设置未读数
            if (msg.unreadCount > 0) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(msg.unreadCount));
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            // 加载头像
            Glide.with(itemView.getContext())
                    .load(msg.avatarUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .transform(new CircleCrop())
                    .into(ivAvatar);
        }
    }
}