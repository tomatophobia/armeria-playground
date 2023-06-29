package example.armeria.websocket;

import com.linecorp.armeria.common.ByteBufAccessMode;
import com.linecorp.armeria.common.websocket.*;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.websocket.WebSocketServiceHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Duration;

public class AbstractWebSocketHandler implements WebSocketServiceHandler {

    @Override
    public WebSocket handle(ServiceRequestContext ctx, WebSocket in) {
//        ctx.setRequestTimeout(Duration.ofSeconds(1));
        final WebSocketWriter writer = WebSocket.streaming();
        in.subscribe(new Subscriber<WebSocketFrame>() {
            @Override
            public void onSubscribe(Subscription s) {
                onOpen(writer);
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(WebSocketFrame webSocketFrame) {
                try (WebSocketFrame frame = webSocketFrame) {
                    switch (frame.type()) {
                        case TEXT:
                            onText(writer, frame.text());
                            break;
                        case BINARY:
                            onBinary(writer, frame.byteBuf(ByteBufAccessMode.RETAINED_DUPLICATE));
                            break;
                        case CLOSE:
                            final CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
                            onClose(writer, closeFrame.status(), closeFrame.reasonPhrase());
                            break;
                        default:
                            // no-op
                    }
                } catch (Throwable t) {
                    writer.close(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                writer.close(t);
            }

            @Override
            public void onComplete() {
                writer.close();
            }
        });
        return writer;
    }

    void onOpen(WebSocketWriter writer) {}

    void onText(WebSocketWriter writer, String message) {}

    void onBinary(WebSocketWriter writer, ByteBuf message) {
        try {
            if (message.hasArray()) {
                onBinary(writer, message.array());
            } else {
                onBinary(writer, ByteBufUtil.getBytes(message));
            }
        } finally {
            message.release();
        }
    }

    void onBinary(WebSocketWriter writer, byte[] message) {}

    void onClose(WebSocketWriter writer, WebSocketCloseStatus status, String reason) {
        writer.close(status, reason);
    }
}
