package example.armeria.stream;

import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpResponseWriter;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.stream.StreamMessage;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.file.HttpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Server server = newServer(8080);
        server.closeOnJvmShutdown();
        server.start().join();
        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}", server.activeLocalPort());
    }

    static Server newServer(int port) throws Exception {
        URL resource = Main.class.getResource("/cantrbry/alice29.txt");
        // 자바의 File 오브젝트도 모든 데이터를 다 읽어서 메모리에 적재하는 것이 아닌 약간 C언어의 file descriptor 같은...?
        HttpFile bigFile = HttpFile.of(new File(resource.toURI()));

        return Server.builder()
                .http(port)
                .service("/bigfile", bigFile.asService())
                .service("/bigfile2", (ctx, req) -> {
                    HttpResponseWriter response = HttpResponse.streaming();
                    response.write(ResponseHeaders.of(200));
                    streamingFileResponse(response, new FileInputStream(new File(resource.toURI())));
                    return response;
                })
                .service("/from-input-stream", (ctx, req) -> {
                    HttpResponseWriter response = HttpResponse.streaming();
                    return response;
                })
                .build();
    }

    private static void streamingFileResponse(HttpResponseWriter response, InputStream inputStream) {
        byte[] chunk = new byte[8192];
        try {
            int length = inputStream.read(chunk);
            if (length == -1) {
                response.close();
            } else {
                response.whenConsumed().thenRun(() -> {
                    if (response.tryWrite(HttpData.copyOf(chunk, 0, length))) {
                        streamingFileResponse(response, inputStream);
                    } else {
                        // The response is completed unexpectedly.
                        response.close();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.close();
        }
    }
}
