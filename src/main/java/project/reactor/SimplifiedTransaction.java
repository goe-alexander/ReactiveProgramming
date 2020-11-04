package project.reactor;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Slf4j
public class SimplifiedTransaction {
    private static final Random random = new Random();
    private final int id;

    public SimplifiedTransaction(int id) {
        this.id = id;
        log.info("[T: {}] created", id);
    }

    public static Mono<SimplifiedTransaction> beginTransaction() {
        return Mono.defer(() -> Mono.just(new SimplifiedTransaction(random.nextInt(1000))));
    }

    public Flux<String> insertSomeRows(Publisher<String> rows) {
        return Flux.from(rows)
                .delayElements(Duration.ofMillis(100))
                .flatMap(r -> {
                    if(random.nextInt() < 2) {
                        return Mono.error(new RuntimeException("Error: " + r));
                    } else {
                        return Mono.just(r);
                    }
                });
    }

    public Mono<Void> commit() {
        return Mono.defer(() -> {
            log.info("[T: {}] commited", id);
            if(random.nextBoolean()){
                return Mono.empty();
            } else {
                return Mono.error(new RuntimeException("Conflict"));
            }
        });
    }

    public Mono<Void> rollback() {
        return Mono.defer(() -> {
            log.info("[T: {}] rollbacked", id);
            if(random.nextBoolean()){
                return Mono.empty();
            } else {
                return Mono.error(new RuntimeException("Connection error"));
            }
        });
    }


}
