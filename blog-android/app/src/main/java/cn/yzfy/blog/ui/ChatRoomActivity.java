package cn.yzfy.blog.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.dto.User;
import cn.yzfy.blog.util.JsonUtil;
import cn.yzfy.blog.util.SwipeBackHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author 一朝风月
 * @description 群聊大厅（对齐 uniapp pages/chat/room.vue），使用 OkHttp WebSocket 连接 ws://host/api/ws/chat。
 * @datetime 2026-03-20
 */
public class ChatRoomActivity extends AppCompatActivity {

    private static final int NORMAL_CLOSE = 1000;

    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private EditText etInput;
    private TextView tvStatus;
    private WebSocket webSocket;
    private final java.util.Set<String> sentIds = new java.util.HashSet<>();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        SwipeBackHelper.attach(this);

        recyclerView = findViewById(R.id.recyclerView);
        etInput = findViewById(R.id.etInput);
        tvStatus = findViewById(R.id.tvStatus);

        adapter = new ChatMessageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnSend).setOnClickListener(v -> send());

        connect();
    }

    private String getWsUrl() {
        String base = getString(R.string.api_base_url).trim();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String wsBase = base.replace("http://", "ws://").replace("https://", "wss://");
        return wsBase + "/ws/chat";
    }

    private void connect() {
        if (webSocket != null) return;
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("连接中...");

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(getWsUrl()).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                runOnUiThread(() -> {
                    tvStatus.setText("已连接");
                    tvStatus.setVisibility(View.GONE);
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> handleMessage(text));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                runOnUiThread(() -> {
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText("连接已断开");
                    ChatRoomActivity.this.webSocket = null;
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                runOnUiThread(() -> {
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText("连接失败：" + t.getMessage());
                    ChatRoomActivity.this.webSocket = null;
                });
            }
        });
    }

    private void handleMessage(String text) {
        try {
            JsonObject jo = gson.fromJson(text, JsonObject.class);
            String clientId = jo.has("clientId") ? jo.get("clientId").getAsString() : null;
            if (clientId != null && sentIds.contains(clientId)) return;
            if (clientId != null) sentIds.add(clientId);

            String nickname = jo.has("nickname") ? jo.get("nickname").getAsString() : "游客";
            String content = jo.has("content") ? jo.get("content").getAsString() : "";
            String time = jo.has("time") ? jo.get("time").getAsString() : "";
            boolean isOwner = jo.has("isOwner") && jo.get("isOwner").getAsBoolean();

            String myName = getMyNickname();
            boolean isSelf = myName != null && myName.equals(nickname);

            adapter.append(new ChatMessageAdapter.ChatMsg(
                    clientId != null ? clientId : String.valueOf(System.currentTimeMillis()),
                    nickname, content, time, isSelf, isOwner));
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        } catch (Exception e) {
            adapter.append(new ChatMessageAdapter.ChatMsg(
                    String.valueOf(System.currentTimeMillis()),
                    "系统", text, "", false, false));
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private String getMyNickname() {
        String json = SessionManager.getUserJson(getApplicationContext());
        if (TextUtils.isEmpty(json)) return null;
        try {
            User u = JsonUtil.fromJson(json, User.class);
            if (u.nickname != null && !u.nickname.isEmpty()) return u.nickname;
            return u.username;
        } catch (Exception e) {
            return null;
        }
    }

    private void send() {
        String text = etInput.getText() == null ? null : etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String nickname = getMyNickname();
        if (nickname == null) nickname = "我";
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        boolean isOwner = "管理员".equals(nickname) || "admin".equalsIgnoreCase(nickname);
        String clientId = Long.toString(System.currentTimeMillis(), 36) + "-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "TEXT");
        payload.addProperty("content", text);
        payload.addProperty("nickname", nickname);
        payload.addProperty("clientId", clientId);
        payload.addProperty("time", time);
        payload.addProperty("isOwner", isOwner);

        adapter.append(new ChatMessageAdapter.ChatMsg(clientId, nickname, text, time, true, isOwner));
        sentIds.add(clientId);
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        etInput.setText("");

        if (webSocket == null) connect();
        if (webSocket != null) {
            boolean sent = webSocket.send(gson.toJson(payload));
            if (!sent) Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未连接，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSE, "activity_destroy");
            webSocket = null;
        }
    }
}
