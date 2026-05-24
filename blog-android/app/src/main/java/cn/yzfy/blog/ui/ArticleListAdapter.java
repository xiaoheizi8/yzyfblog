package cn.yzfy.blog.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.dto.Article;

/**
 * @author 一朝风月
 * @description 文章列表适配器（用于 portal/article/list）。
 * @datetime 2026-03-18
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.VH> {

    private final List<Article> items = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    /**
     * 提交新列表数据并刷新 UI。
     */
    public void submit(List<Article> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Article a = items.get(position);
        holder.title.setText(a.title == null ? "" : a.title);
        holder.summary.setText(a.summary == null ? "" : a.summary);
        String meta = "浏览 " + (a.viewCount == null ? 0 : a.viewCount) + (a.publishTime == null ? "" : " · " + a.publishTime);
        holder.meta.setText(meta);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(a);
            } else {
                // 默认行为：直接打开文章详情页
                if (a.id != null) {
                    Intent intent = new Intent(v.getContext(), ArticleDetailActivity.class);
                    intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE_ID, a.id);
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView summary;
        TextView meta;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            meta = itemView.findViewById(R.id.meta);
        }
    }
}

