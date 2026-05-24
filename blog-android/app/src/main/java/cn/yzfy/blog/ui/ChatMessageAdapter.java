package cn.yzfy.blog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.yzfy.blog.R;

/**
 * @author 一朝风月
 * @description 群聊消息列表适配器（用于群聊大厅气泡展示，支持自己/他人、群主标识）。
 * @datetime 2026-03-20
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.Holder> {

    public static class ChatMsg {
        public String id;
        public String nickname;
        public String content;
        public String time;
        public boolean isSelf;
        public boolean isOwner;

        public ChatMsg(String id, String nickname, String content, String time, boolean isSelf, boolean isOwner) {
            this.id = id;
            this.nickname = nickname;
            this.content = content;
            this.time = time;
            this.isSelf = isSelf;
            this.isOwner = isOwner;
        }
    }

    private final List<ChatMsg> list = new ArrayList<>();

    public void append(ChatMsg msg) {
        list.add(msg);
        notifyItemInserted(list.size() - 1);
    }

    public void submit(List<ChatMsg> items) {
        list.clear();
        if (items != null) list.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatMsg m = list.get(position);
        holder.tvUser.setText(m.nickname == null ? "游客" : m.nickname + (m.isOwner ? " [群主]" : ""));
        holder.tvTime.setText(m.time == null ? "" : m.time);
        holder.tvContent.setText(m.content == null ? "" : m.content);

        if (m.isSelf) {
            holder.root.setGravity(android.view.Gravity.END);
            holder.bubbleWrap.setGravity(android.view.Gravity.END);
            holder.tvContent.setBackgroundResource(R.drawable.bg_chat_bubble_self);
        } else {
            holder.root.setGravity(android.view.Gravity.START);
            holder.bubbleWrap.setGravity(android.view.Gravity.START);
            holder.tvContent.setBackgroundResource(R.drawable.bg_chat_bubble_other);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout root;
        final LinearLayout bubbleWrap;
        final TextView tvUser;
        final TextView tvTime;
        final TextView tvContent;

        Holder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            bubbleWrap = itemView.findViewById(R.id.bubbleWrap);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
