package com.example.websocketdemo.model;

import java.util.*;

public class VoteRoom {

    private Map<Integer,Integer> result = new HashMap<>();

    private boolean voting = false;

    private Set<String> votedUsers = new HashSet<>();

    private int onlineCount = 0;

    public VoteRoom(){

        result.put(1,0);
        result.put(2,0);
        result.put(3,0);

    }

    public boolean vote(String userId,int option){

        if(votedUsers.contains(userId)){
            return false;
        }

        votedUsers.add(userId);

        result.put(option,result.get(option)+1);

        return true;

    }

    public void startVote(){

        voting=true;

        votedUsers.clear();

        result.put(1,0);
        result.put(2,0);
        result.put(3,0);

    }

    public void stopVote(){
        voting=false;
    }

    public Map<Integer,Integer> getResult(){
        return result;
    }

    public boolean isVoting(){
        return voting;
    }

    public void addUser(){
        onlineCount++;
    }

    public void removeUser(){
        if(onlineCount>0) onlineCount--;
    }

    public int getOnlineCount(){
        return onlineCount;
    }

}