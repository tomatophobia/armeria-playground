package example.armeria.rest.greeting;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;

import java.time.Duration;

public class GreetingService {
    @Get("/hello")
    public HttpResponse getHello() {
        Greeting greeting = new Greeting(0, "hello");
        return HttpResponse.delayed(HttpResponse.ofJson(greeting), Duration.ofSeconds(100));
    }
}
