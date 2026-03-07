package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.*;
import com.example.websocketdemo.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

public class VoteWebSocketHandler extends TextWebSocketHandler {

    private VoteService voteService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<WebSocketSession> sessions = new ArrayList<>();

    public VoteWebSocketHandler(VoteService voteService){
        this.voteService = voteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){

        sessions.add(session);

        String roomId = getParam(session, "roomId", "default");
        String role = getParam(session, "role", "user");


        session.getAttributes().put("roomId",roomId);
        session.getAttributes().put("role", role);

        voteService.addUser(roomId);

        try{
            broadcast(roomId);
        }catch(Exception e){}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,CloseStatus status){

        sessions.remove(session);

        String roomId=(String)session.getAttributes().get("roomId");

        voteService.removeUser(roomId);

        try{
            broadcast(roomId);
        }catch(Exception e){}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session,TextMessage message) throws Exception{

        VoteMessage voteMessage = objectMapper.readValue(
                message.getPayload(),
                VoteMessage.class
        );

        String roomId = (String) session.getAttributes().get("roomId");

        if("vote".equals(voteMessage.getCommand())){

            if(voteService.isVoting(roomId)){

                boolean success = voteService.vote(
                        roomId,
                        session.getId(),
                        voteMessage.getOption()
                );

                if(!success){

                    session.sendMessage(new TextMessage("already"));

                    return;

                }

            }

        }

        if("start".equals(voteMessage.getCommand())){

            if(!"admin".equals(session.getAttributes().get("role"))){
                session.sendMessage(new TextMessage("forbidden"));
                return;
            }

            voteService.startVote(roomId);
        }

        if("stop".equals(voteMessage.getCommand())){
            if(!"admin".equals(session.getAttributes().get("role"))){
                session.sendMessage(new TextMessage("forbidden"));
                return;
            }
            voteService.stopVote(roomId);

        }

        broadcast(roomId);

    }

    private void broadcast(String roomId) throws Exception{

        ResultMessage resultMessage = new ResultMessage(
                roomId,
                voteService.getResult(roomId),
                voteService.isVoting(roomId),
                voteService.getOnline(roomId)
        );

        String json = objectMapper.writeValueAsString(resultMessage);

        for(WebSocketSession s:sessions){

            if(s.isOpen()){

                String r=(String)s.getAttributes().get("roomId");

                if(roomId.equals(r)){

                    s.sendMessage(new TextMessage(json));

                }

            }

        }

    }

    private String getParam(WebSocketSession session, String key, String defaultValue){

        String query=session.getUri().getQuery();

        if(query!=null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && key.equals(kv[0])) {
                    return kv[1];
                }
            }
        }

            return defaultValue;
        }
}