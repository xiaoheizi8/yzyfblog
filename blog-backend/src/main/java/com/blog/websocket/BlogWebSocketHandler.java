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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 博客群聊 WebSocket（支持文字、表情、图片等，前端解析 type）
 *
 * @author blog
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlogWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.put(session.getId(), session);
        log.debug("WebSocket 连接: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // 简单群聊广播：原样转发前端发送的内容（JSON 或纯文本）
        for (WebSocketSession s : SESSIONS.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 异常: {}", exception.getMessage());
        try {
            session.close();
        } catch (IOException ignored) {
        }
        SESSIONS.remove(session.getId());
    }
}
