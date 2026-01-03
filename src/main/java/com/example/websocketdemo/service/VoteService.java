package com.example.websocketdemo.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VoteService {

    private static final Map<Integer, Integer> voteMap = new ConcurrentHashMap<>();
    private static final Set<String> votedSessions = ConcurrentHashMap.newKeySet();

    static {
        voteMap.put(1, 0);
        voteMap.put(2, 0);
        voteMap.put(3, 0);
    }

    public static boolean hasVoted(String sessionId) {
        return votedSessions.contains(sessionId);
    }

    public static void vote(String sessionId, Integer optionId) {
        if (votedSessions.add(sessionId)) {
            voteMap.computeIfPresent(optionId, (k, v) -> v + 1);
        }
    }

    public static Map<Integer, Integer> getResult() {
        return voteMap;
    }
    private static volatile boolean open = false;

    public static void openVote() {
        open = true;
    }

    public static void closeVote() {
        open = false;
    }

    public static boolean isOpen() {
        return open;
    }

}
