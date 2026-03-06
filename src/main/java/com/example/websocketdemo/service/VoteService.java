package com.example.websocketdemo.service;

import com.example.websocketdemo.model.VoteRoom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoteService {

    private Map<String, VoteRoom> rooms = new ConcurrentHashMap<>();

    private VoteRoom getRoom(String roomId){

        return rooms.computeIfAbsent(roomId,k->new VoteRoom());

    }

    public boolean vote(String roomId,String userId,int option){

        return getRoom(roomId).vote(userId,option);

    }

    public void startVote(String roomId){

        getRoom(roomId).startVote();

    }

    public void stopVote(String roomId){

        getRoom(roomId).stopVote();

    }

    public Map<Integer,Integer> getResult(String roomId){

        return getRoom(roomId).getResult();

    }

    public boolean isVoting(String roomId){

        return getRoom(roomId).isVoting();

    }

    public void addUser(String roomId){

        getRoom(roomId).addUser();

    }

    public void removeUser(String roomId){

        getRoom(roomId).removeUser();

    }

    public int getOnline(String roomId){

        return getRoom(roomId).getOnlineCount();

    }

}