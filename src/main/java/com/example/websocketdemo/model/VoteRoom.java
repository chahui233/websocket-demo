package com.example.websocketdemo.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VoteRoom {

    private boolean open = false;

    private final Map<Integer, Integer> voteMap = new ConcurrentHashMap<>();
    private final Set<String> votedSessions = ConcurrentHashMap.newKeySet();

    public VoteRoom() {
        voteMap.put(1, 0);
        voteMap.put(2, 0);
        voteMap.put(3, 0);
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void vote(String sessionId, int option) {
        if (!open) {
            throw new IllegalStateException("投票未开启");
        }
        if (votedSessions.add(sessionId)) {
            voteMap.computeIfPresent(option, (k, v) -> v + 1);
        }
    }

    public Map<Integer, Integer> toResultMap() {
        return voteMap;
    }
}
