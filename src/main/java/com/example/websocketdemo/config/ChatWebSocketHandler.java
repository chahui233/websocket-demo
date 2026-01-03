package com.example.websocketdemo.config;

import com.example.websocketdemo.model.VoteMessage;
import com.example.websocketdemo.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {

        VoteMessage voteMessage = mapper.readValue(message.getPayload(), VoteMessage.class);

        if ("vote".equals(voteMessage.getType())) {

            if (!VoteService.hasVoted(session.getId())) {
                VoteService.vote(session.getId(), voteMessage.getOptionId());
                broadcastResult();
            } else {
                session.sendMessage(new TextMessage(
                        "{\"type\":\"error\",\"msg\":\"你已经投过票了\"}"
                ));
            }
        }
    }


    private void broadcastResult() throws IOException {
        Map<Integer, Integer> result = VoteService.getResult();

        String json = mapper.writeValueAsString(
                Map.of("type", "result", "result", result)
        );

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}
