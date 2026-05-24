package cn.yzfy.blog.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.yzfy.blog.R;

/**
 * @author 一朝风月
 * @description 排行榜列表适配器（用于 portal/coin/ranking）。
 * @datetime 2026-03-18
 */
public class RankListAdapter extends RecyclerView.Adapter<RankListAdapter.VH> {

    private final List<Map<String, Object>> items = new ArrayList<>();

    /**
     * 提交新列表数据并刷新 UI。
     */
    public void submit(List<Map<String, Object>> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rank, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Map<String, Object> m = items.get(position);
        int rank = position + 1;
        holder.rank.setText(String.valueOf(rank));
        String nickname = toStr(m.get("nickname"));
        holder.name.setText(nickname);
        holder.sub.setText("用户ID：" + toStr(m.get("userId")));
        holder.coin.setText(toStr(m.get("coin")));

        Object avatarObj = m.get("avatar");
        String avatar = avatarObj != null ? avatarObj.toString().trim() : "";
        if (!TextUtils.isEmpty(avatar)) {
            holder.avatarText.setVisibility(View.GONE);
            holder.avatarImg.setVisibility(View.VISIBLE);
            String url = normalizeAvatarUrl(holder.itemView.getContext().getString(R.string.api_base_url), avatar);
            Glide.with(holder.itemView.getContext()).load(url).circleCrop().into(holder.avatarImg);
        } else {
            holder.avatarImg.setVisibility(View.GONE);
            holder.avatarText.setVisibility(View.VISIBLE);
            String first = (nickname != null && !nickname.isEmpty()) ? nickname.substring(0, 1) : "风";
            holder.avatarText.setText(first);
        }
    }

    private String normalizeAvatarUrl(String base, String avatar) {
        if (avatar == null) return "";
        String u = avatar.replace("`", "").replace("[", "").replace("]", "").replace(" ", "").trim();
        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        if (base == null) base = "";
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (u.startsWith("/")) return base + u;
        return base + "/" + u;
    }

    private String toStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView rank;
        ImageView avatarImg;
        TextView avatarText;
        TextView name;
        TextView sub;
        TextView coin;

        VH(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            avatarImg = itemView.findViewById(R.id.avatarImg);
            avatarText = itemView.findViewById(R.id.avatarText);
            name = itemView.findViewById(R.id.name);
            sub = itemView.findViewById(R.id.sub);
            coin = itemView.findViewById(R.id.coin);
        }
    }
}

