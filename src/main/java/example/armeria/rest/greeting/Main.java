package example.armeria.rest.greeting;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(example.armeria.rest.blog.Main.class);

    static Server newServer(int port) {
        ServerBuilder sb = Server.builder();
        return sb.http(port)
                .annotatedService(new GreetingService())
                .build();
    }

    public static void main(String[] args) {
        Server server = newServer(8080);

        server.closeOnJvmShutdown();
        server.start().join();

        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}",
                server.activeLocalPort());
    }
}
