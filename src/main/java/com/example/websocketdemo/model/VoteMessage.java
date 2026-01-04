package com.example.websocketdemo.model;

public class VoteMessage {

    private String type;
    private Integer optionId;
    private String command;
    private String roomId;
    private int option;

    public String getRoomId() {
        return roomId;
    }
    public int getOption() {
        return option;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
