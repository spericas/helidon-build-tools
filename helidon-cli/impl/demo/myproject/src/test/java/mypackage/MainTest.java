
package mypackage;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import io.helidon.media.common.MediaSupport;
import io.helidon.media.jsonp.common.JsonProcessing;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MainTest {

    private static WebServer webServer;
    private static final JsonBuilderFactory JSON_BUILDER = Json.createBuilderFactory(Collections.emptyMap());

    @BeforeAll
    public static void startTheServer() throws Exception {
        webServer = Main.startServer();

        long timeout = 2000; // 2 seconds should be enough to start the server
        long now = System.currentTimeMillis();

        while (!webServer.isRunning()) {
            Thread.sleep(100);
            if ((System.currentTimeMillis() - now) > timeout) {
                Assertions.fail("Failed to start webserver");
            }
        }
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (webServer != null) {
            webServer.shutdown()
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testHelloWorld() throws Exception {
        JsonProcessing jsonProcessing = JsonProcessing.create();
        WebClient webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .mediaSupport(MediaSupport.builder()
                        .registerDefaults()
                        .registerReader(jsonProcessing.newReader())
                        .registerWriter(jsonProcessing.newWriter())
                        .build())
                .build();

        webClient.get()
                .path("/greet")
                .request(JsonObject.class)
                .thenAccept(jsonObject -> Assertions.assertEquals("Hello World!", jsonObject.getString("message")))
                .exceptionally(throwable -> {
                    Assertions.fail(throwable);
                    return null;
                })
                .toCompletableFuture()
                .get();

        webClient.get()
                .path("/health")
                .request()
                .thenAccept(response -> Assertions.assertEquals(200, response.status().code()))
                .exceptionally(throwable -> {
                    Assertions.fail(throwable);
                    return null;
                })
                .toCompletableFuture()
                .get();

        webClient.get()
                .path("/metrics")
                .request()
                .thenAccept(response -> Assertions.assertEquals(200, response.status().code()))
                .exceptionally(throwable -> {
                    Assertions.fail(throwable);
                    return null;
                })
                .toCompletableFuture()
                .get();
    }
}