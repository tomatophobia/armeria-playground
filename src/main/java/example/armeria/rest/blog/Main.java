package example.armeria.rest.blog;

import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.metric.MetricCollectingService;
import com.linecorp.armeria.server.metric.MetricCollectingServiceBuilder;
import com.linecorp.armeria.server.metric.PrometheusExpositionService;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocService;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server newServer(int port) {
        ServerBuilder sb = Server.builder();
        DocService docService =
                DocService.builder()
                        .exampleRequests(BlogService.class, "createBlogPost", "{\"title\":\"My first blog\", \"content\":\"Hello Armeria!\"}")
                        .build();
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        return sb.http(port)
                .decorator(LoggingService.newDecorator())
                .annotatedService(new BlogService())
                .serviceUnder("/docs", docService)
                .meterRegistry(meterRegistry)
                .service("/metrics", PrometheusExpositionService.of(meterRegistry.getPrometheusRegistry()))
                .decorator(MetricCollectingService.builder(MeterIdPrefixFunction.ofDefault("my.metric")).newDecorator())
                .build();
    }

    public static void main(String[] args) throws Exception {
        Server server = newServer(8080);

        server.closeOnJvmShutdown();

        server.start().join();

        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}",
                server.activeLocalPort());
    }
}
