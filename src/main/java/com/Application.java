package com;

import com.backend.DBDAO;
import com.backend.http.controller.Controller;
import org.apache.catalina.connector.RequestFacade;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.servlet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

@SpringBootApplication
public class Application {
    private static ApplicationContext applicationContext;

    public static void main(String[] args) throws InterruptedException {
        if(!Arrays.stream(args).anyMatch(arg -> arg.equals("-newUser"))) {
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
    public FilterRegistrationBean<Filter> testMethode() {
        FilterRegistrationBean<Filter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new Filter() {
            private Logger logger;

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                if(logger == null) {
                    logger = StartUp.createLogger(Controller.class);
                    PropertyConfigurator.configure(getClass().getResourceAsStream("/log4j.properties"));
                    logger.addAppender(new ConsoleAppender());
                }
                final RequestFacade requestFacade = (RequestFacade) request;
                final DBDAO dbdao = (DBDAO)applicationContext.getBean("DBDAO");
                logger.info(requestFacade.getRequestURI() + " from " + requestFacade.getHeader("userid"));
                if(dbdao.checkCredentials(requestFacade.getHeader("userid"),
                        requestFacade.getHeader("passw"))) {
                    chain.doFilter(request, response);
                }
            }
        });
        return filterRegBean;
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        };
    }
}
