package cn.yzfy.blog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.dto.Comment;

/**
 * @author 一朝风月
 * @description 评论列表适配器。
 * @datetime 2026-03-19
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.VH> {

    private final List<Comment> items = new ArrayList<>();

    public void submit(List<Comment> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void addComment(Comment c) {
        items.add(c);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Comment c = items.get(position);
        String displayName = c.nickname;
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = c.userId == null ? "游客" : "用户 " + c.userId;
        }
        holder.tvUser.setText(displayName);
        holder.tvContent.setText(c.content == null ? "" : c.content);
        holder.tvTime.setText(c.createTime == null ? "" : c.createTime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser;
        TextView tvContent;
        TextView tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}

