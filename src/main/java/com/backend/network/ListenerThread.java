package com.backend.network;

import com.Main;
import com.bot.telegram.Bot;
import com.backend.DBDAO;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ListenerThread extends Thread {

    private boolean networkRun;
    private static String addListCMD = "<addList>";
    private static String addListCMDItemNr = "<addList>Item#";
    private Bot bot;
    private static int socketPort = 55555;
    private static Logger logger = Main.getLogger();

    public ListenerThread(Bot bot){
     this.bot = bot;
     this.setName("networkListener");
     this.networkRun = true;
    }

    @Override
    public void run() {
        while(networkRun){
            logger.info("Start listening on Port "+ socketPort);
            try(ServerSocket serverSocket = new ServerSocket(socketPort);
                Socket client = serverSocket.accept();
                Scanner incomingScan = new Scanner(client.getInputStream());
                PrintWriter outgoingWriter = new PrintWriter(client.getOutputStream())) {
                logger.info("New Client connected: " + client.getInetAddress().getHostAddress());
                while(incomingScan.hasNext()){
                    processIncomingStream(incomingScan.next());
                }
            } catch (IOException e) {
                logger.error("Failed activating bot", e);
            }
        }
    }

    private void processIncomingStream(String incomingString){
        logger.info("Processing incoming Stream: " + incomingString);
        if(incomingString.startsWith(addListCMDItemNr)){
            Map<Integer, String> itemMap = DBDAO.getQRItemMap();
            int itemNumber = Integer.parseInt(incomingString.replace(addListCMDItemNr, ""));
            String item = itemMap.get(itemNumber);
            if(item.equals("-")){
                return;
            }
            bot.getShoppingList().add(item);
            DBDAO.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
        }else {
            if (incomingString.startsWith(addListCMD)) {
                String item = incomingString.replace(addListCMD, "").replace('_', ' ');
                bot.getShoppingList().add(item);
                DBDAO.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
            }
        }
    }

    private void remoteShutdown() {
        logger.info("Performing remote shutdown...");
        System.exit(0);
    }
}
