package com.network;

import com.Main;
import com.telegram.Bot;
import com.utils.DBUtil;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ListenerThread extends Thread {

    private boolean networkRun;
    private Bot bot;
    private static Logger logger = Main.getLogger();

    public ListenerThread(Bot bot){
     this.bot = bot;
     this.setName("networkListener");
     this.networkRun = true;
    }

    @Override
    public void run() {
        while(networkRun){
            int socketPort = 55555;
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
        String addListCMDItemNr = "<addList>Item#";
        if(incomingString.startsWith(addListCMDItemNr)){
            Map<Integer, String> itemMap = DBUtil.getQRItemMap();
            int itemNumber = Integer.parseInt(incomingString.replace(addListCMDItemNr, ""));
            String item = itemMap.get(itemNumber);
            if(item.equals("-")){
                return;
            }
            bot.getShoppingList().add(item);
            DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
        }else {
            String addListCMD = "<addList>";
            if (incomingString.startsWith(addListCMD)) {
                String item = incomingString.replace(addListCMD, "").replace('_', ' ');
                bot.getShoppingList().add(item);
                DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
            }
        }
    }
}
