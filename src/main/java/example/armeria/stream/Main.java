package example.armeria.stream;

import com.linecorp.armeria.common.stream.ByteStreamMessage;
import com.linecorp.armeria.common.stream.StreamWriter;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.file.HttpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server newServer(int port) throws Exception {
        URL resource = Main.class.getResource("/cantrbry/alice29.txt");
        // 자바의 File 오브젝트도 모든 데이터를 다 읽어서 메모리에 적재하는 것이 아닌 약간 C언어의 file descriptor 같은...?
        HttpFile bigFile = HttpFile.of(new File(resource.toURI()));

        return Server.builder()
                .http(port)
                .service("/bigfile", bigFile.asService())
                .build();
    }

    public static void main(String[] args) throws Exception {
        Server server = newServer(8080);
        server.closeOnJvmShutdown();
        server.start().join();
        logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}", server.activeLocalPort());
    }
}
