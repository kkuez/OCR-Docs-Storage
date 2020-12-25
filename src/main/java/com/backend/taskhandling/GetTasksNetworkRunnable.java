package com.backend.taskhandling;

import com.backend.BackendFacade;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;

public class GetTasksNetworkRunnable implements Runnable {

    private BackendFacade facade;

    public GetTasksNetworkRunnable(BackendFacade facade) {
        this.facade = facade;
    }

    @Override
    public void run() {
        while(true) {
            try(ServerSocket serverSocket = new ServerSocket(4444)) {
                try(Socket client = serverSocket.accept();
                    InputStream reader = client.getInputStream();
                    final PrintWriter printWriter = new PrintWriter(client.getOutputStream())) {
                    System.out.println("New client: " + client.getInetAddress().getHostAddress());
                    processIncoming(reader, printWriter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processIncoming(InputStream reader, PrintWriter printWriter) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        while (reader.available() > 0) {
            stringBuilder.append((char)reader.read());
        }
        final String requestedCmd = stringBuilder.toString();
        System.out.println(requestedCmd);
        switch (requestedCmd) {
            case "getTasks":
                facade.getTasks().forEach(task -> printWriter.print(parseTask(task) + "*"));
                break;
            default:
        }
    }

    private String parseTask(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.getName()).append(";");
        String userName;
        if(task.getForWhom().equals("ALL")) {
            userName = "Alle";
        } else {
            userName = facade.getAllowedUsers().get(Integer.parseInt(task.getForWhom())).getName();
        }
        builder.append(userName).append(";");
        builder.append(task.getTimeString()).append(";");
        LocalDate date = LocalDate.from(task.getExecutionStrategy().getTime());
        if(date.equals(LocalDate.now())) {
            builder.append("TODAY").append(";");
        }
        return builder.toString();
    }
}
