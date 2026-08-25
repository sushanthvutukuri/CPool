package com.utd.cpool;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseTest {

    @Bean
    CommandLineRunner testDatabase(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {

                System.out.println("========== DATABASE TEST ==========");

                System.out.println("DATABASE: "
                        + connection.getCatalog());

                System.out.println("SCHEMA: "
                        + connection.getSchema());

                System.out.println("URL: "
                        + connection.getMetaData().getURL());

                System.out.println("USER: "
                        + connection.getMetaData().getUserName());

                System.out.println("===================================");

                var statement = connection.createStatement();

var result = statement.executeQuery(
        "SELECT COUNT(*) FROM public.users"
);

if (result.next()) {
    System.out.println("USERS TABLE EXISTS!");
    System.out.println("User count: " + result.getInt(1));
}
            }
        };
    }
}
