package example.armeria.rest.blog;

import com.linecorp.armeria.common.DependencyInjector;
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;
import com.linecorp.armeria.internal.common.RequestContextExtension;
import com.linecorp.armeria.server.auth.AuthService;
import com.linecorp.armeria.server.healthcheck.HealthCheckService;
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
                .http(9090)
                .decorator(LoggingService.newDecorator())
                .defaultVirtualHost()
                .annotatedService(new BlogService())
                .and()
                .virtualHost(9090)
                .serviceUnder("/docs", docService) // 서로 다른 virtual host에 속하므로 동작하지 않는다.
                .and()
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
