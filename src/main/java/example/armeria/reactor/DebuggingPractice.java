package example.armeria.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;


public class DebuggingPractice {
    private static final Logger log = LoggerFactory.getLogger(DebuggingPractice.class);

    public static void main(String[] args) {
//        Hooks.onOperatorDebug();
        debuggingUsingHook();
    }

    private static void debuggingUsingHook() {
        List<String> nameList = Arrays.asList("Rochel", "Rock", "April", "Hong");

        Flux<String> assembly = Flux.fromIterable(nameList)
                .map(name -> name.substring(0, 3))
                .map(String::toUpperCase)
                .distinct()
                .map(name -> {
                    if (name.equals("HON")) {
                        throw new RuntimeException("Boom!");
                    } else {
                        return name;
                    }
                });
//                .checkpoint()
//                .log();

        assembly.subscribe(log::info);
    }

}
