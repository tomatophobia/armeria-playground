package example.armeria.rest.blog;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.metric.MetricCollectingService;
import com.linecorp.armeria.server.metric.PrometheusExpositionService;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server server1(int port) {
        final ServerBuilder sb = Server.builder();
        final DocService docService =
                DocService.builder()
                          .exampleRequests(BlogService.class, "createBlogPost",
                                           "{\"title\":\"My first blog\", \"content\":\"Hello Armeria!\"}")
                          .build();
        final PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

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
                 .decorator(MetricCollectingService.builder(MeterIdPrefixFunction.ofDefault("my.metric"))
                                                   .newDecorator())
                 .build();
    }

    static Server server2(int port) {
        final ServerBuilder sb = Server.builder();
        HttpService service = null;
        return sb.http(port)
                 .annotatedService(new BlogService())
                 .serviceUnder("/web",
                               service.decorate((delegate, ctx, req) -> {
                                   CompletableFuture<String> f = null;
                                   try {
                                       return HttpResponse.of(
                                               f.thenApplyAsync(data -> {
                                                                    // data를 이용해 req 변환
                                                                    try {
                                                                        return delegate.serve(ctx, req);
                                                                    } catch (Exception e) {
                                                                        throw new CompletionException(e);
                                                                    }
                                                                },
                                                                ctx.eventLoop())
                                       );
                                   } catch (CompletionException e) {
                                       throw (Exception) e.getCause();
                                   }
                               })
                 )
                 .build();
    }

    public static void main(String[] args) throws Exception {
        final Server server = server2(8080);

        server.closeOnJvmShutdown();

        server.start().join();

        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}",
                    server.activeLocalPort());
    }
}
