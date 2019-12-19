package com.luo123.androidlab.update;


import java.util.Map;

public class UpdateMessageListModel {
    public int latestVersionCode;

    public int getLatestVersionCode() {
        return latestVersionCode;
    }

    public void setLatestVersionCode(int latestVersionCode) {
        this.latestVersionCode = latestVersionCode;
    }

    public Map<Integer, UpdateMessageModel> getMessageList() {
        return messageList;
    }

    public void setMessageList(Map<Integer, UpdateMessageModel> messageList) {
        this.messageList = messageList;
    }

    public Map<Integer, UpdateMessageModel> messageList;

    public UpdateMessageModel getLatested(){
        return messageList.get(latestVersionCode);
    }
}
