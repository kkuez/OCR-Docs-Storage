package com.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Memo {

    private  int id;
    private List<String> userNames;
    private String memoText;
    private LocalDateTime fromTime;

    public Memo(int id, String memoText, LocalDateTime fromTime) {
        this.id = id;
        userNames = new ArrayList<>();
        this.memoText = memoText;
        this.fromTime = fromTime;
    }

    public Memo(List<String> userNames, String memoText, LocalDateTime fromTime) {
        this.userNames = userNames;
        this.memoText = memoText;
        this.fromTime = fromTime;
    }

    public void addUserName(String name) {
        userNames.add(name);
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
}
