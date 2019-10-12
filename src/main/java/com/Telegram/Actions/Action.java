package com.Telegram.Actions;

import com.ObjectTemplates.User;

public abstract class Action {

    private String name;

    public abstract Action performAction(User user);

    //GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
