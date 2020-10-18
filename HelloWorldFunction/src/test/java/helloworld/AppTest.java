package helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.Test;

public class AppTest {

  @Test
  public void successfulResponse() {

    final App app = new App();
    final APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
    assertEquals(result.getStatusCode().intValue(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    final String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains("\"title\""));
    assertTrue(content.contains("\"date\""));
  }
}
