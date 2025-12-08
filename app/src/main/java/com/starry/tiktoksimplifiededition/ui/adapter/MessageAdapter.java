package com.starry.tiktoksimplifiededition.ui.adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
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

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private OnItemClickListener listener;

    private OnItemLongClickListener longClickListener;
    private String currentSearchQuery = "";

    private static final int TYPE_TEXT = 0;

    public interface OnItemClickListener {
        void onItemClick(Message message);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Message message);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    // 设置搜索关键词并刷新列表
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        notifyDataSetChanged();
    }

    private String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public void setMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public Message getMessageAt(int position) {
        if (position >= 0 && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextMessageViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message != null) {
            if (holder instanceof TextMessageViewHolder) {
                ((TextMessageViewHolder) holder).bind(message);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * 核心方法：高亮匹配的文本
     * @param textView 需要设置文本的 TextView
     * @param text 原始完整文本
     * @param query 搜索关键词
     */
    private void highlightText(TextView textView, String text, String query) {
        if (text == null) text = "";

        if (query != null && !query.isEmpty()) {
            // 忽略大小写进行匹配
            int startIndex = text.toLowerCase().indexOf(query.toLowerCase());
            if (startIndex >= 0) {
                SpannableStringBuilder builder = new SpannableStringBuilder(text);
                // 使用蓝色高亮，比黄色在白底上更清晰
                ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
                builder.setSpan(span, startIndex, startIndex + query.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(builder);
            } else {
                textView.setText(text);
            }
        } else {
            textView.setText(text);
        }
    }

    /**
     * ViewHolder类，用于展示文本消息项。
     * 负责初始化视图组件、绑定数据以及处理点击与长按事件。
     */
    class TextMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime, tvBadge;
        ImageView ivAvatar;
        /**
         * 构造方法，初始化视图组件，并设置点击和长按事件监听器。
         *
         * @param itemView 该ViewHolder所代表的列表项布局视图
         */
        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvBadge = itemView.findViewById(R.id.tv_badge);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);

            // 普通点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && position < messages.size()) {
                    listener.onItemClick(messages.get(position));
                }
            });

            // 绑定长按事件
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION && position < messages.size()) {
                    longClickListener.onItemLongClick(messages.get(position));
                    return true; // 返回 true 表示事件已消费，不会再触发 onClick
                }
                return false;
            });
        }
        /**
         * 将消息对象的数据绑定到视图上。
         * 包括设置昵称、内容、时间、未读数及头像等UI元素。
         *
         * @param msg 需要绑定的消息对象
         */
        void bind(Message msg) {
            clearViews();
            if (msg == null) return;

            if (msg.isPinned) {
                itemView.setBackgroundColor(0xFFF5F5F5);
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF);
            }

            // 设置并高亮昵称
            highlightText(tvName, msg.getDisplayName(), getCurrentSearchQuery());

            // 准备内容文本
            String displayContent = "";
            if (msg.getLatestContent() != null && !msg.getLatestContent().isEmpty()) {
                displayContent = msg.getLatestContent();
            } else if (msg.content != null) {
                displayContent = msg.content;
            }

            // 根据消息类型替换显示的文本预览
            if (msg.type == 1) { // 图片消息
                displayContent = "[图片]";
            } else if (msg.type == 2) { // 运营消息
                displayContent = "[领取奖励]";
            }

            // 设置并高亮内容
            highlightText(tvContent, displayContent, getCurrentSearchQuery());

            // 设置时间
            long displayTime = msg.getLatestTimestamp() > 0 ?
                    msg.getLatestTimestamp() : msg.timestamp;
            if (displayTime > 0) {
                String timeText = TimeUtils.getFriendlyTime(displayTime);
                tvTime.setText(timeText);
            } else {
                tvTime.setText("");
            }

            // 设置未读红点
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

        private void clearViews() {
            tvName.setText("");
            tvContent.setText("");
            tvTime.setText("");
            tvBadge.setVisibility(View.GONE);
            ivAvatar.setImageResource(R.drawable.ic_launcher_background);
            // 每次复用前重置背景色（虽然 bind 里会覆盖，但这是一个好习惯）
            itemView.setBackgroundColor(0xFFFFFFFF);
        }
    }
}