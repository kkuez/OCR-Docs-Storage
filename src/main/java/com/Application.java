package com;

import com.backend.DBDAO;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

@SpringBootApplication
public class Application {
    private static final int httpPort = 8088;

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        if(Arrays.stream(args).noneMatch(arg -> arg.equals("-newUser"))) {
            applicationContext = SpringApplication.run(Application.class, args);
        } else {
            System.out.println("New User\nName?");
            String name = "-";
            String password = "-";
            try(InputStream inputStream = System.in;
                Scanner scanner = new Scanner(inputStream)) {
                if(scanner.hasNextLine()) {
                    name = scanner.nextLine();
                }
                System.out.println("Password?");
                if(scanner.hasNextLine()) {
                    password = scanner.nextLine();
                }
                createUser(name, password);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createUser(String name, String password) {
        if(DBDAO.insertNewUser(name, password)) {
            System.out.println("Success");
        } else {
            System.out.println("Error!");
        }
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        return tomcat;
    }

    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }
}
