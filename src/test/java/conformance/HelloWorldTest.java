package conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelloWorldTest {
  @Test
  public void testHelloWorld() {
    assertEquals("Hello, World!", "Hello, World!");
  }
}
