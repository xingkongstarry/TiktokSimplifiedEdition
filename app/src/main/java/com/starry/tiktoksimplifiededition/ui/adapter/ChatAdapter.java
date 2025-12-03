// ChatAdapter.java
package com.starry.tiktoksimplifiededition.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.starry.tiktoksimplifiededition.R;
import com.starry.tiktoksimplifiededition.data.model.ChatMessage;
import com.starry.tiktoksimplifiededition.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    public void setMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == ChatMessage.TYPE_RECEIVED) ?
                R.layout.item_chat_received : R.layout.item_chat_sent;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatMessage msg) {
            tvContent.setText(msg.getContent());
            tvTime.setText(TimeUtils.getFriendlyTime(msg.getTimestamp()));
        }
    }
}
