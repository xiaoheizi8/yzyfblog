package cn.yzfy.blog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.util.SwipeBackHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 登录页（用户名 + 密码），对齐 uniapp pages/auth/login。
 * @datetime 2026-03-19
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 移动端“左滑返回”体验
        SwipeBackHelper.attach(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        String baseUrl = getString(R.string.api_base_url);
        BlogApi api = ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        api.login(body).enqueue(new Callback<Result<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<Result<Map<String, Object>>> call, Response<Result<Map<String, Object>>> response) {
                setLoading(false);
                Result<Map<String, Object>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    String msg = res != null && !TextUtils.isEmpty(res.message) ? res.message : "登录失败";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                Object tokenObj = res.data.get("token");
                Object userObj = res.data.get("user");
                if (tokenObj != null) {
                    SessionManager.saveToken(getApplicationContext(), String.valueOf(tokenObj));
                }
                if (userObj != null) {
                    // 简单保存 user 的 JSON 文本，方便“我的”页面展示
                    SessionManager.saveUserJson(getApplicationContext(), cn.yzfy.blog.util.JsonUtil.toJson(userObj));
                }
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Result<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

