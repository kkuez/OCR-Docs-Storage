package com.data;

import java.time.LocalDateTime;
import java.util.List;

public class Memo {

    private List<User> users;
    private String memoText;
    private LocalDateTime fromTime;

    public Memo(List<User> users, String memoText, LocalDateTime fromTime) {
        this.users = users;
        this.memoText = memoText;
        this.fromTime = fromTime;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getMemoText() {
        return memoText;
    }

    public LocalDateTime getFromTime() {
        return fromTime;
    }
}
