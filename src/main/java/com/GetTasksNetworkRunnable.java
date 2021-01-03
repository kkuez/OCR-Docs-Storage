package com;

import com.backend.BackendFacade;
import com.backend.taskHandling.Task;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class GetTasksNetworkRunnable implements Runnable {

    private BackendFacade facade;

    private Logger logger = Main.getLogger();
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
                    logger.info("New client: " + client.getInetAddress().getHostAddress());
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
        final String incoming = stringBuilder.toString();
        logger.info("Request: " + incoming);
        final String requestedCmd = incoming.contains(";") ? incoming.split(";")[0] : incoming;
        switch (requestedCmd) {
            case "getTasks":
                // Only send 13 Temins to prevent heap overflow on ESP
                facade.getTasksForAll(facade, null, 13)
                        .forEach(task -> printWriter.print(parseTask(task) + "*"));
                break;
            case "timeTo":
                final String[] split = incoming.split(";");
                final int hour = Integer.parseInt(split[1]);
                final int minutes = Integer.parseInt(split[2]);
                final LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                final LocalTime plus = LocalTime.of(hour, minutes);
                final Duration between = Duration.between(now, plus);
                printWriter.print(String.valueOf(between.getSeconds() * 1000));
                break;
            default:
        }
    }

    private String parseTask(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.getName()).append(";");
        String userName;
        if(task.getUserList().size() > 1) {
            userName = "Alle";
        } else {
            userName = task.getUserList().get(0).getName();
        }
        builder.append(userName).append(";");
        builder.append(task.getExecutionStrategy().getTime().toString()).append(";");
        LocalDate date = LocalDate.from(task.getExecutionStrategy().getTime());
        if(date.equals(LocalDate.now())) {
            builder.append("TODAY").append(";");
        }
        return builder.toString();
    }
}
