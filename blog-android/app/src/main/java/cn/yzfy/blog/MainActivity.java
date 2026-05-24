package cn.yzfy.blog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cn.yzfy.blog.ui.ArticlesFragment;
import cn.yzfy.blog.ui.HomeFragment;
import cn.yzfy.blog.ui.RankFragment;
import cn.yzfy.blog.ui.LoginActivity;
import cn.yzfy.blog.ui.ProfileFragment;
import cn.yzfy.blog.data.SessionManager;

/**
 * @author 一朝风月
 * @description 应用主入口：底部 Tab 导航（对齐 uniapp tabBar：首页/文章/排行榜/我的）。
 * @datetime 2026-03-18
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new HomeFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_articles) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new ArticlesFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_rank) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new RankFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                String token = SessionManager.getToken(getApplicationContext());
                if (token == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, new ProfileFragment())
                            .commit();
                }
                return true;
            }
            // 先占位：排行榜/我的后续补齐
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
            return true;
        });

        // 默认展示首页（与 uniapp tabBar 的 pages/index/index 对齐）
        nav.setSelectedItemId(R.id.nav_home);
    }
}