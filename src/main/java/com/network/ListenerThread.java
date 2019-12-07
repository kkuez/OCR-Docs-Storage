package com.network;

import com.Telegram.Bot;
import com.Utils.DBUtil;
import com.Utils.LogUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ListenerThread extends Thread {

    public static boolean networkRun = true;
    private static String addListCMD = "<addList>";
    private Bot bot;
    private static int socketPort = 55555;
    public ListenerThread(Bot bot){
     this.bot = bot;
     this.setName("networkListener");
    }

    @Override
    public void run() {
        while(networkRun){
            LogUtil.log("Start listening on Port "+ socketPort);
            try(ServerSocket serverSocket = new ServerSocket(socketPort);
                Socket client = serverSocket.accept();
                Scanner incomingScan = new Scanner(client.getInputStream());
                PrintWriter outgoingWriter = new PrintWriter(client.getOutputStream())) {
                LogUtil.log("New Client connected: " + client.getInetAddress().getHostAddress());
                while(incomingScan.hasNext()){
                    processIncomingStream(incomingScan.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processIncomingStream(String incomingString){
        LogUtil.log("Processing incoming Stream: " + incomingString);
        if(incomingString.startsWith(addListCMD)){
            String item  = incomingString.replace(addListCMD, "").replace("_", " ");
            bot.getShoppingList().add(item);
            DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
        }
    }
}
