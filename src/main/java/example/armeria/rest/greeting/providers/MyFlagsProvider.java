package example.armeria.rest.greeting.providers;

import com.linecorp.armeria.common.FlagsProvider;
import com.linecorp.armeria.common.annotation.Nullable;

public class MyFlagsProvider implements FlagsProvider {
    @Override
    public int priority() {
        return 30;
    }

    @Override
    public @Nullable Long defaultRequestTimeoutMillis() {
        return 1000L;
    }
}
