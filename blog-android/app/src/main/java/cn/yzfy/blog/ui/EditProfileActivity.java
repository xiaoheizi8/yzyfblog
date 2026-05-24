package cn.yzfy.blog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.data.net.dto.User;
import cn.yzfy.blog.util.JsonUtil;
import cn.yzfy.blog.util.SwipeBackHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 编辑资料页（对齐 uniapp pages/profile/edit.vue），支持修改昵称、邮箱、手机号。
 * @datetime 2026-03-20
 */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etNickname;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private Button btnSave;
    private Button btnCancel;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        SwipeBackHelper.attach(this);

        etNickname = findViewById(R.id.etNickname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progress = findViewById(R.id.progress);

        btnCancel.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSave.setOnClickListener(v -> saveProfile());

        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadProfile();
    }

    private BlogApi api() {
        String baseUrl = getString(R.string.api_base_url);
        return ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);
    }

    private void loadProfile() {
        progress.setVisibility(View.VISIBLE);
        api().profile().enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                progress.setVisibility(View.GONE);
                Result<User> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(EditProfileActivity.this, "加载资料失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = res.data;
                etNickname.setText(u.nickname == null ? "" : u.nickname);
                etEmail.setText(u.email == null ? "" : u.email);
                etPhone.setText(u.phone == null ? "" : u.phone);
            }

            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        Map<String, Object> body = new HashMap<>();
        body.put("nickname", toOrNull(etNickname.getText() == null ? null : etNickname.getText().toString().trim()));
        body.put("email", toOrNull(etEmail.getText() == null ? null : etEmail.getText().toString().trim()));
        body.put("phone", toOrNull(etPhone.getText() == null ? null : etPhone.getText().toString().trim()));

        progress.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        api().updateProfile(body).enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                progress.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Result<User> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(EditProfileActivity.this,
                            res != null && !TextUtils.isEmpty(res.message) ? res.message : "保存失败",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = res.data;
                SessionManager.saveUserJson(getApplicationContext(), JsonUtil.toJson(u));
                Toast.makeText(EditProfileActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Object toOrNull(String s) {
        if (s == null) return null;
        if (TextUtils.isEmpty(s)) return null;
        return s;
    }
}

