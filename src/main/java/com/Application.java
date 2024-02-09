package com;

import com.backend.DBDAO;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.servlet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
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
    public FilterRegistrationBean<Filter> testMethode() {
        FilterRegistrationBean<Filter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new Filter() {

            final Logger logger = LoggerFactory.getLogger(Filter.class);
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                final RequestFacade requestFacade = (RequestFacade) request;
                final DBDAO dbdao = (DBDAO)applicationContext.getBean("DBDAO");
                final String userid = "userid";
                if(requestFacade.getRequestURI().toLowerCase(Locale.ROOT).contains("ldap")
                || requestFacade.getHeader(userid) != null && requestFacade.getHeader(userid).toLowerCase(Locale.ROOT).contains("ldap")) {
                    logger.error("Found invalid ldap String!!!");
                    System.exit(3);
                }

                logger.info("{} from {}", requestFacade.getRequestURI(),requestFacade.getHeader(userid));
                if(dbdao.checkCredentials(requestFacade.getParameterMap(), requestFacade.getHeader(userid),
                        requestFacade.getHeader("passw"))) {
                    chain.doFilter(request, response);
                }
            }
        });
        return filterRegBean;
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
