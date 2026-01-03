package com.example.websocketdemo.model;

import java.util.Map;

public class ResultMessage {

    private String type;
    private Map<Integer, Integer> result;

    public ResultMessage(String type, Map<Integer, Integer> result) {
        this.type = type;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public Map<Integer, Integer> getResult() {
        return result;
    }
}
