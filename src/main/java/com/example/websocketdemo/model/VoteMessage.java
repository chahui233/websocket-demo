package com.example.websocketdemo.model;

public class VoteMessage {

    private String command;

    private int option;

    public String getCommand(){
        return command;
    }

    public void setCommand(String command){
        this.command=command;
    }

    public int getOption(){
        return option;
    }

    public void setOption(int option){
        this.option=option;
    }

}