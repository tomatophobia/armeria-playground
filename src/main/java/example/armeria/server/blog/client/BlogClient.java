package example.armeria.server.blog.client;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpMethod;

public final class BlogClient {
    public static void main(String[] args) {
        WebClient webClient = WebClient.builder("http://localhost:8080/")
                .decorator(LoggingClient.newDecorator())
                .build();
        webClient.post("/blogs", "{ \"title\": \"My first blog\", \"content\": \"Hello Armeria!\"}").aggregate().join();
        AggregatedHttpResponse response = webClient.get("/blogs").aggregate().join();
        System.out.println(response.contentUtf8());
    }
}
