package cn.yzfy.blog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;

/**
 * @author 一朝风月
 * @description 首页（对齐 uniapp：pages/index/index），提供写文章、留言弹幕墙、群聊大厅等快捷入口卡片。
 * @datetime 2026-03-18
 */
public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("首页");

        // 对齐 uniapp 首页卡片入口：写文章 -> 发布文章
        view.findViewById(R.id.card_publish).setOnClickListener(v -> {
            if (SessionManager.getToken(requireContext().getApplicationContext()) == null) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }
            startActivity(new Intent(requireContext(), ArticleEditActivity.class));
        });

        view.findViewById(R.id.card_wall).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MessageWallActivity.class)));

        view.findViewById(R.id.card_chat).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChatRoomActivity.class)));
    }
}

