package cn.yzfy.blog.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.util.SwipeBackHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 注册页（用户名 + 昵称 + 密码），对齐 uniapp pages/auth/register。
 * @datetime 2026-03-19
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etNickname;
    private EditText etPassword;
    private EditText etPassword2;
    private Button btnRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 移动端“左滑返回”体验
        SwipeBackHelper.attach(this);

        etUsername = findViewById(R.id.etUsername);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String password2 = etPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.equals(password, password2)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        String baseUrl = getString(R.string.api_base_url);
        BlogApi api = ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        if (!TextUtils.isEmpty(nickname)) {
            body.put("nickname", nickname);
        }

        api.register(body).enqueue(new Callback<Result<Void>>() {
            @Override
            public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                setLoading(false);
                Result<Void> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk()) {
                    String msg = res != null && !TextUtils.isEmpty(res.message) ? res.message : "注册失败";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Result<Void>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

