package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.ResultMessage;
import com.example.websocketdemo.model.VoteMessage;
import com.example.websocketdemo.service.VoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VoteWebSocketHandler extends TextWebSocketHandler {

    private final VoteService voteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public VoteWebSocketHandler(VoteService voteService){
        this.voteService = voteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){

        sessions.add(session);

        String roomId = getParam(session, "roomId", "default");

        session.getAttributes().put("roomId",roomId);

        voteService.addUser(roomId);

        try{
            broadcast(roomId);
        }catch(Exception e){}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){

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
                        (String) session.getAttributes().get("username"),
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

        if(query!=null){
            String[] pairs = query.split("&");
            for(String pair : pairs){
                String[] kv = pair.split("=", 2);
                if(kv.length == 2 && key.equals(kv[0])){
                    return kv[1];
                }
            }
        }

        return defaultValue;

    }

}
