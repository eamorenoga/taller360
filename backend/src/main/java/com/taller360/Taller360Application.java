package com.taller360;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Taller360Application {
  public static void main(String[] args) {
    configureRenderDatabaseUrl();
    SpringApplication.run(Taller360Application.class, args);
  }

  private static void configureRenderDatabaseUrl() {
    if (hasValue(System.getenv("SPRING_DATASOURCE_URL"))) {
      return;
    }
    String databaseUrl = System.getenv("DATABASE_URL");
    if (!hasValue(databaseUrl)) {
      return;
    }

    URI uri = URI.create(databaseUrl);
    String scheme = uri.getScheme();
    if (!"postgresql".equals(scheme) && !"postgres".equals(scheme)) {
      return;
    }

    String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
    System.setProperty("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query);

    String userInfo = uri.getUserInfo();
    if (hasValue(userInfo)) {
      String[] parts = userInfo.split(":", 2);
      System.setProperty("spring.datasource.username", decode(parts[0]));
      if (parts.length > 1) {
        System.setProperty("spring.datasource.password", decode(parts[1]));
      }
    }
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }

  private static String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }
}
