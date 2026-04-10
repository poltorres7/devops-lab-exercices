package lab.ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CiFailureApplicationTests {
  @Test
  void healthEndpointPathShouldStartWithApiPrefix() {
    String configuredPath = "/healthz";
    assertTrue(configuredPath.startsWith("/api/"), "Health endpoint path does not match the expected API prefix convention");
  }
}
