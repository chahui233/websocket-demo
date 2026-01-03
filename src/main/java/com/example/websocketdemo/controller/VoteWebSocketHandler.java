package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.VoteMessage;
import com.example.websocketdemo.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.example.websocketdemo.model.ResultMessage;

public class VoteWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = new HashSet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {

        VoteMessage voteMessage =
                mapper.readValue(message.getPayload(), VoteMessage.class);

        switch (voteMessage.getType()) {

            case "vote":
                if (!VoteService.isOpen()) {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"error\",\"msg\":\"投票尚未开始或已结束\"}"
                    ));
                    return;
                }

                if (!VoteService.hasVoted(session.getId())) {
                    VoteService.vote(session.getId(), voteMessage.getOptionId());
                    broadcastResult();
                } else {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"error\",\"msg\":\"你已经投过票了\"}"
                    ));
                }
                break;

            case "control":
                if ("open".equals(voteMessage.getCommand())) {
                    VoteService.openVote();
                    broadcastStatus("OPEN");
                }
                if ("close".equals(voteMessage.getCommand())) {
                    VoteService.closeVote();
                    broadcastStatus("CLOSED");
                }
                break;
        }
    }


    private void broadcastResult() throws IOException {
        String resultJson = mapper.writeValueAsString(
                new ResultMessage("result", VoteService.getResult())
        );

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(resultJson));
            }
        }
    }
    private void broadcastStatus(String status) throws IOException {
        String json = "{\"type\":\"status\",\"status\":\"" + status + "\"}";
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

}
