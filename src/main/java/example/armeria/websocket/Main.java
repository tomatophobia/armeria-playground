package example.armeria.websocket;

import com.linecorp.armeria.common.ByteBufAccessMode;
import com.linecorp.armeria.common.websocket.*;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.websocket.WebSocketService;
import com.linecorp.armeria.server.websocket.WebSocketServiceBuilder;
import com.linecorp.armeria.server.websocket.WebSocketServiceHandler;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server newServer(int port) {
        ServerBuilder sb = Server.builder();

        WebSocketServiceHandler webSocketServiceHandler = (ctx, messages) -> {
            WebSocketWriter webSocketWriter = WebSocket.streaming();
            messages.subscribe(new Subscriber<WebSocketFrame>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(WebSocketFrame webSocketFrame) {
                    try (WebSocketFrame frame = webSocketFrame) {
                        switch (frame.type()) {
                            case TEXT:
                                webSocketWriter.write(frame.text());
                                break;
                            case BINARY:
                                // do nothing
                                break;
                            case CLOSE:
                                final CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
                                webSocketWriter.close(closeFrame.status(), closeFrame.reasonPhrase());
                                break;
                            default:
                                // no-op
                        }
                    } catch (Throwable t) {
                        webSocketWriter.close(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    webSocketWriter.close(t);
                }

                @Override
                public void onComplete() {
                    webSocketWriter.close();
                }
            });
            return webSocketWriter;
        };

        return sb.http(port)
                .decorator(LoggingService.newDecorator())
                .service("/chat", WebSocketService.builder(webSocketServiceHandler).allowedOrigins("*").build())
                .requestTimeout(Duration.ofSeconds(100))
                .build();
    }

    static Server newServerUsingAbstractWebSocketHandler(int port) {
        ServerBuilder sb = Server.builder();

        final WebSocketServiceHandler webSocketServiceHandler = new AbstractWebSocketHandler() {
            @Override
            void onOpen(WebSocketWriter writer) {
                writer.write("Connection Opened!\n");
            }

            @Override
            void onText(WebSocketWriter writer, String message) {
                writer.write("Echo: " + message + "\n");
            }

            @Override
            void onBinary(WebSocketWriter writer, byte[] message) {
                super.onBinary(writer, message);
                logger.warn("Binary Also Worked");
            }

            @Override
            void onClose(WebSocketWriter writer, WebSocketCloseStatus status, String reason) {
                writer.write("Connection Closed!\n");
                writer.close(status, reason);
            }
        };

        return sb.http(port)
                .decorator(LoggingService.newDecorator())
                .service("/chat", WebSocketService.builder(webSocketServiceHandler).allowedOrigins("*").build())
                .requestTimeout(Duration.ofSeconds(100))
                .build();
    }

    public static void main(String[] args) throws Exception {
        Server server = newServer(8080);

        server.closeOnJvmShutdown();

        server.start().join();

        logger.info("Server has been started. Serving tomato service at http://127.0.0.1:{}",
                server.activeLocalPort());
    }
}
