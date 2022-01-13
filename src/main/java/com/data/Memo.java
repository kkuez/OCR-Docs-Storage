package com.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

public class Memo {

    private  int id;
    private List<String> userNames;
    private String memoText;
    private LocalDateTime fromTime;

    public Memo(List<String> userNames, String memoText, LocalDateTime fromTime) {
        this.userNames = userNames;
        this.memoText = memoText;
        this.fromTime = fromTime;
    }

    public Memo(int id, List<String> userNames, String memoText, LocalDateTime fromTime) {
        this.id = id;
        this.userNames = userNames;
        this.memoText = memoText;
        this.fromTime = fromTime;
    }

    public int getId() {
        return id;
    }

    public List<String> getUserNames() {
        return userNames;
    }

    public String getMemoText() {
        return memoText;
    }

    @JsonIgnore
    public LocalDateTime getFromTime() {
        return fromTime;
    }

    public String getFromTimeString() {
        return fromTime.withNano(0).toString();
    }

    public void setUserNames(List<String> userNames) {
        this.userNames = userNames;
    }
}
