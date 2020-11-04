package project.reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.time.Duration;

@Slf4j
public class CreatingStreams {
    public static void main(String[] args) throws InterruptedException {

        // The PUSH method covers cancellation and backpressure itself.
/*        Flux.push(emitter -> IntStream
                                .range(2000,3000)
                                .forEach(emitter::next))
                .delayElements(Duration.ofMillis(1))
                .subscribe(e -> log.info("onNext: {}", e));

        Thread.sleep(250);*/

        // The CREATE factory method
        Flux.push(emitter -> {
            emitter.onDispose(() -> log.info("Disposed"));
        }).subscribe(e -> log.info("onNext: {}", e));

        // The GENERATE factory method
        // Generate Fibonacci seq
        Flux.generate(()-> Tuples.of(0L, 1L),
                (state, sink) -> {
                    log.info("generated value: {}", state.getT2());
                    sink.next(state.getT2());
                    long newValue = state.getT1() + state.getT2();
                    return Tuples.of(state.getT2(), newValue);
                })
                .delayElements(Duration.ofMillis(1))
                .take(7)
                .subscribe(r -> log.info("onNext: {}", r));
        Thread.sleep(300);


        // Wrapping disposable resources into Reactive Streams
        // UsingWhen operator
//        Flux.usingWhen(
//                SimplifiedTransaction.beginTransaction(),
//                transaction -> transaction.insertSomeRows(Flux.just("A", "B", "C")),
//                SimplifiedTransaction::commit,
//                SimplifiedTransaction::rollback
//                );
    }
}
