package lab.networkdb;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class NetworkDbApplication {
  private volatile String startupStatus = "starting";
  private volatile String startupDetail = "initializing";

  public static void main(String[] args) {
    SpringApplication.run(NetworkDbApplication.class, args);
  }

  @Bean
  RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(2))
        .setReadTimeout(Duration.ofSeconds(2))
        .build();
  }

  @GetMapping("/healthz")
  Map<String, String> health() {
    if (!"ready".equals(startupStatus)) {
      throw new IllegalStateException("Application dependencies are not ready");
    }
    return Map.of("status", "ok");
  }

  @GetMapping("/startupz")
  Map<String, String> startup() {
    return Map.of("status", startupStatus, "detail", startupDetail);
  }

  @GetMapping("/db-check")
  Map<String, String> dbCheck() throws Exception {
    DbResult result = verifyDatabaseConnection();
    return Map.of(
        "status", "connected",
        "host", result.host(),
        "resolvedIp", result.resolvedIp(),
        "port", Integer.toString(result.port()));
  }

  @Bean
  org.springframework.boot.CommandLineRunner validateDependenciesAtStartup() {
    return args -> {
      DbResult result = verifyDatabaseConnection();
      startupStatus = "ready";
      startupDetail = "database reachable at " + result.host() + ":" + result.port();
    };
  }

  private DbResult verifyDatabaseConnection() throws Exception {
    String host = required("DB_HOST");
    int port = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "3306"));
    String db = System.getenv().getOrDefault("DB_NAME", "lab");
    String user = required("DB_USER");
    String password = required("DB_PASSWORD");

    String dns = InetAddress.getByName(host).getHostAddress();
    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db
        + "?useSSL=true&requireSSL=true&connectTimeout=2000&socketTimeout=2000";

    try (Connection ignored = DriverManager.getConnection(jdbcUrl, user, password)) {
      return new DbResult(host, port, dns);
    } catch (SQLException ex) {
      startupStatus = "degraded";
      startupDetail = ex.getMessage();
      throw ex;
    }
  }

  private static String required(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required configuration");
    }
    return value;
  }

  private record DbResult(String host, int port, String resolvedIp) {}
}
