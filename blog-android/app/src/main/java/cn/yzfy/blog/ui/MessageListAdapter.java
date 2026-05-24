package cn.yzfy.blog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.dto.Message;

/**
 * @author 一朝风月
 * @description 留言墙列表适配器（展示留言卡片，支持多色背景）。
 * @datetime 2026-03-20
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.Holder> {

    private static final int[] CARD_COLORS = {
            0x20FF9A9E, 0x20FBC2EB, 0x20A1C4FD, 0x2084FAB0, 0x20FFE082, 0x20B39DDB
    };

    private final List<Message> list = new ArrayList<>();

    public void submit(List<Message> items) {
        list.clear();
        if (items != null) list.addAll(items);
        notifyDataSetChanged();
    }

    public void prepend(Message msg) {
        list.add(0, msg);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Message m = list.get(position);
        holder.tvContent.setText(m.content == null ? "" : m.content);
        int color = CARD_COLORS[position % CARD_COLORS.length];
        if (holder.itemView instanceof MaterialCardView) {
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvContent;

        Holder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
