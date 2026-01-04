package com.example.websocketdemo.model;

import java.util.Map;

public class ResultMessage {

    private String roomId;
    private Map<Integer, Integer> result;

    public ResultMessage(String roomId, Map<Integer, Integer> result) {
        this.roomId = roomId;
        this.result = result;
    }

    public String getRoomId() {
        return roomId;
    }

    public Map<Integer, Integer> getResult() {
        return result;
    }
}

