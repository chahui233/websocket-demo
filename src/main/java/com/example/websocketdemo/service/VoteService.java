package com.example.websocketdemo.service;

import com.example.websocketdemo.model.VoteRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VoteService {

    private final Map<String, VoteRoom> rooms = new ConcurrentHashMap<>();

    public VoteRoom getRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, k -> new VoteRoom());
    }

    public void vote(String roomId, String voterId, int option) {
        VoteRoom room = getRoom(roomId);
        room.vote(voterId, option);
    }
}
