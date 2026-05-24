package cn.yzfy.blog.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.yzfy.blog.R;

/**
 * @author 一朝风月
 * @description 选图缩略图适配器：展示本地预览 Uri，并支持删除。
 * @datetime 2026-03-19
 */
public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.VH> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<Uri> items;
    private final OnRemoveClickListener removeListener;

    public SelectedImageAdapter(List<Uri> items, OnRemoveClickListener removeListener) {
        this.items = items;
        this.removeListener = removeListener;
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Uri uri = items.get(position);
        holder.img.setImageURI(uri);
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

