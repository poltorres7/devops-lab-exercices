package lab.restart;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestartLoopApplication {
  public static void main(String[] args) {
    SpringApplication.run(RestartLoopApplication.class, args);
  }

  @Bean
  CommandLineRunner initializeScheduleWindow() {
    return args -> {
      String windowStart = System.getenv().getOrDefault("WINDOW_START", "2026-13-01T08:00:00Z");
      try {
        OffsetDateTime.parse(windowStart);
      } catch (DateTimeParseException ex) {
        throw new IllegalStateException("Invalid scheduling window configuration", ex);
      }
    };
  }
}
