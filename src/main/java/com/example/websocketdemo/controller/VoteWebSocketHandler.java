package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.ResultMessage;
import com.example.websocketdemo.model.VoteMessage;
import com.example.websocketdemo.model.VoteRoom;
import com.example.websocketdemo.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoteWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    // 所有在线连接
    private static final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    // session -> roomId
    private static final Map<WebSocketSession, String> sessionRoomMap =
            new ConcurrentHashMap<>();

    private final VoteService voteService;

    public VoteWebSocketHandler(VoteService voteService) {
        this.voteService = voteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            System.out.println("收到消息：" + message.getPayload());

            VoteMessage voteMessage =
                    mapper.readValue(message.getPayload(), VoteMessage.class);

            String command = voteMessage.getCommand();
            String roomId = voteMessage.getRoomId();

            // 获取或创建房间
            VoteRoom room = voteService.getRoom(roomId);

            // 加入房间
            if ("join".equals(command)) {
                sessionRoomMap.put(session, roomId);
                return;
            }

            // 开启投票
            if ("open".equals(command)) {
                room.open();
                broadcastStatus(roomId, "OPEN");
                return;
            }

            // 关闭投票
            if ("close".equals(command)) {
                room.close();
                broadcastStatus(roomId, "CLOSED");
                return;
            }

            // 投票
            if ("vote".equals(command)) {
                if (!room.isOpen()) {
                    session.sendMessage(new TextMessage(
                            "{\"error\":\"投票尚未开启\"}"
                    ));
                    return;
                }

                room.vote(session.getId(), voteMessage.getOption());

                ResultMessage result =
                        new ResultMessage(roomId, room.toResultMap());

                broadcastResult(roomId, result);
            }

        } catch (Exception e) {
            // ❗关键：任何异常都不能让 WebSocket 断开
            System.err.println("WebSocket 处理异常（连接未断开）");
            e.printStackTrace();
        }
    }

    private void broadcastResult(String roomId, ResultMessage result) throws IOException {
        String json = mapper.writeValueAsString(result);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()
                    && roomId.equals(sessionRoomMap.get(session))) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    private void broadcastStatus(String roomId, String status) throws IOException {
        String json = "{\"status\":\"" + status + "\"}";

        for (WebSocketSession session : sessions) {
            if (session.isOpen()
                    && roomId.equals(sessionRoomMap.get(session))) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionRoomMap.remove(session);
        System.out.println("WebSocket closed: " + session.getId());
    }
}
