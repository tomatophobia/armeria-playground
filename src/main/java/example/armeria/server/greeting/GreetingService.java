package example.armeria.server.greeting;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.decorator.RequestTimeout;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GreetingService {
    @Get("/hello")
    public HttpResponse getHello() {
        Greeting greeting = new Greeting(0, "hello");
        return HttpResponse.delayed(HttpResponse.ofJson(greeting), Duration.ofSeconds(100));
    }
}
