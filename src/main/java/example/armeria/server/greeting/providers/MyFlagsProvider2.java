package example.armeria.server.greeting.providers;

import com.linecorp.armeria.common.FlagsProvider;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.common.util.Sampler;
import com.linecorp.armeria.common.util.TransportType;
import io.micrometer.core.instrument.MeterRegistry;

public class MyFlagsProvider2 implements FlagsProvider {
    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Long defaultRequestTimeoutMillis() {
        return 5000L;
    }

    @Override
    public TransportType transportType() {
        return TransportType.EPOLL;
    }

    @Override
    public Boolean reportBlockedEventLoop() {
        return true;
    }

    @Override
    public Sampler<? super RequestContext> requestContextLeakDetectionSampler() {
        // Samples all request contexts.
        return Sampler.always();
    }
}
