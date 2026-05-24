package com.blog.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 博客群聊 WebSocket（支持文字、表情、图片、通知等，前端解析 type）
 *
 * @author blog
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlogWebSocketHandler extends TextWebSocketHandler {

    /** 所有会话：sessionId -> session */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    
    /** 用户ID -> sessionId 映射（支持按用户推送） */
    private static final Map<Long, String> USER_SESSION_MAP = new ConcurrentHashMap<>();
    
    /** sessionId -> 用户信息 */
    private static final Map<String, Map<String, Object>> SESSION_USER_INFO = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.put(session.getId(), session);
        log.info("WebSocket 连接建立: sessionId={}, 当前在线人数={}", session.getId(), SESSIONS.size());
        
        // 广播在线用户列表
        broadcastOnlineUsers();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: {}", payload);
        
        try {
            // 解析消息
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.getOrDefault("type", "TEXT");
            
            // 处理用户信息注册
            if ("REGISTER".equals(type)) {
                Long userId = msg.get("userId") != null ? Long.parseLong(msg.get("userId").toString()) : null;
                if (userId != null) {
                    USER_SESSION_MAP.put(userId, session.getId());
                    
                    Map<String, Object> userInfo = new ConcurrentHashMap<>();
                    userInfo.put("userId", userId);
                    userInfo.put("nickname", msg.get("nickname"));
                    userInfo.put("avatar", msg.get("avatar"));
                    SESSION_USER_INFO.put(session.getId(), userInfo);
                    
                    log.info("用户注册 WebSocket: userId={}, nickname={}", userId, msg.get("nickname"));
                    
                    // 广播在线用户列表
                    broadcastOnlineUsers();
                }
                return;
            }
            
            // 群聊消息广播：原样转发前端发送的内容
            for (WebSocketSession s : SESSIONS.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            // 非 JSON 消息，直接广播
            log.warn("非 JSON 消息，直接广播: {}", payload);
            for (WebSocketSession s : SESSIONS.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 清理用户映射
        Map<String, Object> userInfo = SESSION_USER_INFO.remove(session.getId());
        if (userInfo != null && userInfo.get("userId") != null) {
            USER_SESSION_MAP.remove(userInfo.get("userId"));
        }
        
        SESSIONS.remove(session.getId());
        log.info("WebSocket 连接关闭: sessionId={}, 当前在线人数={}", session.getId(), SESSIONS.size());
        
        // 广播在线用户列表
        broadcastOnlineUsers();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 异常: sessionId={}, error={}", session.getId(), exception.getMessage());
        try {
            session.close();
        } catch (IOException ignored) {
        }
        
        // 清理用户映射
        Map<String, Object> userInfo = SESSION_USER_INFO.remove(session.getId());
        if (userInfo != null && userInfo.get("userId") != null) {
            USER_SESSION_MAP.remove(userInfo.get("userId"));
        }
        SESSIONS.remove(session.getId());
    }

    /**
     * 向指定用户发送通知（供 Service 层调用）
     *
     * @param userId  用户ID
     * @param message 通知消息
     */
    public void sendNotification(Long userId, Map<String, Object> message) {
        String sessionId = USER_SESSION_MAP.get(userId);
        if (sessionId != null) {
            WebSocketSession session = SESSIONS.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(message);
                    session.sendMessage(new TextMessage(json));
                    log.info("发送通知给用户: userId={}, message={}", userId, json);
                } catch (Exception e) {
                    log.error("发送通知失败: userId={}", userId, e);
                }
            }
        }
    }

    /**
     * 广播在线用户列表
     */
    private void broadcastOnlineUsers() {
        try {
            List<Map<String, Object>> onlineUsers = SESSION_USER_INFO.values().stream()
                    .filter(info -> info != null && info.containsKey("userId"))
                    .collect(Collectors.toList());

            Map<String, Object> broadcastMsg = new ConcurrentHashMap<>();
            broadcastMsg.put("type", "ONLINE_USERS");
            broadcastMsg.put("users", onlineUsers);
            broadcastMsg.put("count", onlineUsers.size());

            String json = objectMapper.writeValueAsString(broadcastMsg);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : SESSIONS.values()) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
            
            log.debug("广播在线用户列表: count={}", onlineUsers.size());
        } catch (Exception e) {
            log.error("广播在线用户列表失败", e);
        }
    }

    /**
     * 获取当前在线人数
     */
    public static int getOnlineCount() {
        return SESSIONS.size();
    }
}
