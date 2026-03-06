package com.example.websocketdemo.model;

import java.util.Map;

public class ResultMessage {

    private String roomId;

    private Map<Integer,Integer> result;

    private boolean voting;

    private int online;

    public ResultMessage(String roomId,
                         Map<Integer,Integer> result,
                         boolean voting,
                         int online){

        this.roomId=roomId;
        this.result=result;
        this.voting=voting;
        this.online=online;

    }

    public String getRoomId(){
        return roomId;
    }

    public Map<Integer,Integer> getResult(){
        return result;
    }

    public boolean isVoting(){
        return voting;
    }

    public int getOnline(){
        return online;
    }

}