package cn.yzfy.blog.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Message;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.util.SwipeBackHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 留言弹幕墙（对齐 uniapp pages/message/wall.vue），展示留言列表并支持发送。
 *              包含横向滚动的弹幕动画效果。
 * @datetime 2026-03-20
 */
public class MessageWallActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageListAdapter adapter;
    private EditText etInput;
    private View loadingOverlay;
    private DanmakuView danmakuView;
    private boolean submitting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_wall);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        SwipeBackHelper.attach(this);

        recyclerView = findViewById(R.id.recyclerView);
        etInput = findViewById(R.id.etInput);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        danmakuView = findViewById(R.id.danmakuView);

        adapter = new MessageListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnSend).setOnClickListener(v -> submit());

        loadMessages();
    }

    private BlogApi api() {
        String baseUrl = getString(R.string.api_base_url);
        return ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);
    }

    private void loadMessages() {
        loadingOverlay.setVisibility(View.VISIBLE);
        api().messageList(100).enqueue(new Callback<Result<List<Message>>>() {
            @Override
            public void onResponse(Call<Result<List<Message>>> call, Response<Result<List<Message>>> response) {
                loadingOverlay.setVisibility(View.GONE);
                Result<List<Message>> res = response.body();
                if (response.isSuccessful() && res != null && res.isOk() && res.data != null) {
                    adapter.submit(res.data);
                    danmakuView.addMessages(res.data);
                } else {
                    Toast.makeText(MessageWallActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Message>>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(MessageWallActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submit() {
        String content = etInput.getText() == null ? null : etInput.getText().toString().trim();
        if (TextUtils.isEmpty(content) || submitting) return;

        submitting = true;
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);

        api().messageSubmit(body).enqueue(new Callback<Result<Message>>() {
            @Override
            public void onResponse(Call<Result<Message>> call, Response<Result<Message>> response) {
                submitting = false;
                Result<Message> res = response.body();
                if (response.isSuccessful() && res != null && res.isOk() && res.data != null) {
                    Message message = res.data;
                    adapter.prepend(message);
                    danmakuView.addMessage(message);
                    etInput.setText("");
                    recyclerView.smoothScrollToPosition(0);
                    Toast.makeText(MessageWallActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MessageWallActivity.this,
                            res != null && !TextUtils.isEmpty(res.message) ? res.message : "发送失败",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Message>> call, Throwable t) {
                submitting = false;
                Toast.makeText(MessageWallActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (danmakuView != null) {
            danmakuView.clear();
        }
    }
}
