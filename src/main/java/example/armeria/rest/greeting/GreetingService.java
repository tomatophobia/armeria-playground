package example.armeria.rest.greeting;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.ContextAwareEventLoop;
import com.linecorp.armeria.common.ContextAwareExecutor;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.internal.common.RequestContextUtil;
import com.linecorp.armeria.server.annotation.Get;

import io.netty.handler.codec.http.HttpHeaderValues;

public class GreetingService {
    private static Logger logger = LoggerFactory.getLogger(GreetingService.class);

    @Get("/hello")
    public HttpResponse getHello() {
        Greeting greeting = new Greeting(0, "hello");
        return HttpResponse.delayed(HttpResponse.ofJson(greeting), Duration.ofSeconds(100));
    }

    @Get("/test")
    public HttpResponse getTest(RequestContext context) {
        CompletableFuture<HttpResponse> result = new CompletableFuture<>();
        ContextAwareEventLoop eventLoop = context.eventLoop();

        // 요청 1
        WebClient client = WebClient.of("https://icanhazdadjoke.com");
        final HttpResponse response1 =
                client.execute(RequestHeaders.of(HttpMethod.GET, "/", HttpHeaderNames.ACCEPT,
                                                 HttpHeaderValues.APPLICATION_JSON)).mapData(
                        data -> HttpData.of(StandardCharsets.UTF_8,
                                            data.toStringUtf8().toLowerCase().contains("dad") ? "T" : "F"));
        final HttpResponse response2 =
                client.execute(RequestHeaders.of(HttpMethod.GET, "/", HttpHeaderNames.ACCEPT,
                                                 HttpHeaderValues.APPLICATION_JSON)).mapData(
                        data -> HttpData.of(StandardCharsets.UTF_8,
                                            data.toStringUtf8().toLowerCase().contains("dad") ? "T" : "F"));
        response1.aggregate().thenAcceptAsync(res -> {
            response2.aggregate().thenAcceptAsync(res2 -> {
                if ("T".equals(res.contentUtf8()) && "T".equals(res2.contentUtf8())) {
                    // 규합
                    result.complete(HttpResponse.of("dad & dad"));
                } else {
                    throw new IllegalStateException("error");
                }
            }, eventLoop);
        }, eventLoop);

        return HttpResponse.from(result);
    }
}
