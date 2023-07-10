package example.armeria.rest.blog;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.SimpleDecoratingHttpService;
import com.linecorp.armeria.server.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Function;

public class LoggingDecoratingService extends SimpleDecoratingHttpService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingDecoratingService.class);

    protected LoggingDecoratingService(HttpService delegate) {
        super(delegate);
    }

    public static Function<? super HttpService, LoggingDecoratingService> newDecorator() {
        return LoggingDecoratingService::new;
    }

    @Override
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
        logger.debug("before MDC");
        MDC.put("traceId", UUID.randomUUID().toString());
        logger.debug("before delegate");

        final HttpService delegate = (HttpService) unwrap();
        final HttpResponse response = delegate.serve(ctx, req);

        logger.debug("after delegate");
        MDC.clear();
        logger.debug("after clear MDC");

        return response;
    }
}
