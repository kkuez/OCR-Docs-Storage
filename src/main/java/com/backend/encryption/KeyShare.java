package com.backend.encryption;

import com.backend.BackendFacade;
import com.backend.CustomProperties;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
public class KeyShare {


    private final static String EOLINE = ";;";

    private static org.apache.log4j.Logger logger = Logger.getLogger(KeyShare.class);
    private Runnable listenRun;
    private XORCrypt xorCrypt;

    @Autowired
    public KeyShare(CustomProperties properties, BackendFacade facade, XORCrypt xorCrypt) {
        this.xorCrypt = xorCrypt;
        startListening(facade, properties);
        facade.getExecutorService().submit(getKeyRenewRunnable(facade));
    }

    private void startListening(BackendFacade facade, CustomProperties properties) {
        listenRun = getSocketListenRunnable(facade, properties);
        facade.getExecutorService().submit(listenRun);
    }

    private Runnable getKeyRenewRunnable(BackendFacade facade) {
        return new Runnable() {
            @Override
            public void run() {
                LocalDate lastRefreshDate = facade.getLastKeyRenewalDate();
                while(true) {
                    Set<Integer> userIds = facade.getAllowedUsers().keySet();
                    Optional<Integer> userWithoutKey = userIds.stream().filter(id -> !facade.hasXORKey(id)).findAny();
                    if(!LocalDate.now().equals(lastRefreshDate) && userWithoutKey.isEmpty()) {
                        facade.setXORKey(xorCrypt.createNewKey());
                        userIds.forEach(id -> facade.setUserHasXORKey(id, false));
                        lastRefreshDate = LocalDate.now();
                    } else {
                        try {
                            Thread.sleep(3600000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    private Runnable getSocketListenRunnable(BackendFacade facade, CustomProperties properties) {
        return new Runnable() {
            @Override
            public void run() {
                try (ServerSocket socket = new ServerSocket(Integer.parseInt((String) properties.get("keySharePort")))) {
                    while (!socket.isClosed()) {
                        try (Socket client = socket.accept();
                             PrintWriter outputStream = new PrintWriter(client.getOutputStream(), true);
                             InputStreamReader scanner = new InputStreamReader(client.getInputStream())) {

                            String incoming = "";
                            while (!incoming.endsWith(EOLINE)) {
                                incoming += (char) scanner.read();
                            }
                            String[] content = incoming.split(";");
                            String toSend = "";
                            switch (content[0]) {
                                case "getKey":
                                    System.out.println("New getKey request");
                                    int userId = Integer.parseInt(content[1]);
                                    facade.setUserHasXORKey(userId, true);
                                    toSend = (String) properties.get("xorKey");
                                    break;
                                case "isValid":
                                    Set<Integer> userIds = facade.getAllowedUsers().keySet();
                                    boolean valid = userIds.stream().filter(id -> !facade.hasXORKey(id)).findAny()
                                            .isEmpty();
                                    toSend = valid ? "false" : "true";
                                    break;
                            }

                            outputStream.println(toSend);
                            System.out.println("send");
                        }
                    }
                } catch (IOException e) {
                    //TODO LOG
                    e.printStackTrace();
                }

            }
        };
    }
}
