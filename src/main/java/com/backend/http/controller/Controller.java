package com.backend.http.controller;

import com.backend.encryption.XORCrypt;

public abstract class Controller {
    private final XORCrypt xorCrypt;

    public Controller(XORCrypt xorCrypt) {
        this.xorCrypt = xorCrypt;
    }

    public XORCrypt getXorCrypt() {
        return xorCrypt;
    }
}
