package example.armeria.reactor;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;


public class Main {
    public static void main(String[] args) {
        final Flux<Integer> flux = Flux.just(1, 2, 3);

    }
}
