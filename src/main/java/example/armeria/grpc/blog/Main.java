package example.armeria.grpc.blog;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceFilter;
import com.linecorp.armeria.server.grpc.GrpcService;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server newServer(int port) throws Exception {
        final BlogPost exampleRequest = BlogPost.newBuilder()
                .setTitle("Example title")
                .setContent("Example content")
                .build();
        final DocService docService = DocService.builder()
                .exampleRequests(BlogServiceGrpc.SERVICE_NAME, "CreateBlogPost", exampleRequest)
                .exclude(DocServiceFilter.ofServiceName(ServerReflectionGrpc.SERVICE_NAME))
                .build();

        final GrpcService grpcService = GrpcService.builder()
                .addService(new BlogService())
                .enableUnframedRequests(true)
                .exceptionMapping(new GrpcExceptionHandler())
                .useBlockingTaskExecutor(true)
                .build();
        return Server.builder()
                .http(port)
                .service(grpcService)
                .serviceUnder("/docs", docService)
                .build();
    }

    public static void main(String[] args) throws Exception {
        final Server server = newServer(8080);

        server.closeOnJvmShutdown().thenRun(() -> {
            logger.info("Server has been stopped.");
        });

        server.start().join();
    }
}
