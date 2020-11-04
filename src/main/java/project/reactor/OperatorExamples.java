package project.reactor;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

@Log4j2
public class OperatorExamples {
    public static void main(String[] args) throws InterruptedException {
        // Index and timestamp operators:
        Flux.range(2000, 10)
                .timestamp()
                .index()
                .subscribe(e -> log.info("index: {}, ts: {}, value: {}",
                        e.getT1(),
                        Instant.ofEpochMilli(e.getT2().getT1()),
                        e.getT2().getT2()));

        // Collecting all elements as a Collection and processing them as a Mono
        // avoid this for large or infinite streams
        Flux.just(1, 6, 4, 5, 7, 99, 32, 2, 1)
                .collectSortedList(Comparator.reverseOrder())
                .subscribe(System.out::println);

        // Reducing stream elements
        Flux.just(3, 5, 7, 9, 11, 15, 16, 17)
                .any(e -> e % 2 == 0)
                .subscribe(t -> log.info("Has evens: {}", t));

        Flux.range(1, 5)
                .reduce(0, (acc, elem) -> acc + elem)
                .subscribe(result -> log.info("Result : {}", result));

        Flux.range(1, 6)
                .scan(0, (acc, elem) -> acc + elem)
                .subscribe(result -> log.info("Result: {}", result));

        // Calculate a moving average on a stream
        int bucketSize = 5;
        Flux.range(1, 500)
                .index()
                .scan(new int[bucketSize],
                        (acc, elem) -> {
                            acc[(int) (elem.getT1() % bucketSize)] = elem.getT2();
                            return acc;
                        })
                .skip(bucketSize)
                .map(array -> Arrays.stream(array).sum() * 1.0 / bucketSize)
                .subscribe(av -> log.info("Running average: {}", av));

        System.out.println("####### Then many operator:");
        Flux.just(1, 2, 3, 4, 5)
                .thenMany(Flux.just(6, 7))
                .subscribe(e -> log.info("onNext: {}", e));

        // Concatenating streams
        System.out.println("####### Concatenating streams:");
        Flux.concat(
                Flux.range(1, 3),
                Flux.range(4, 2),
                Flux.range(6, 5)

        ).subscribe(e -> log.info("onNext: {}", e));

        System.out.println("####### Batching elements with buffer:");
        Flux.range(1, 13)
                .buffer(4)
                .subscribe(e -> log.info("onNext: {}", e));

        System.out.println("####### Batching elements with window:");
        Flux.range(101, 20)
                .windowUntil(elem -> elem % 2 == 0, true)
                .subscribe(window -> window.collectList().subscribe(e -> log.info("window: {}", e)));

        System.out.println("####### Batching elements with groupBy:");
        Flux.range(1, 7)
                .groupBy(e -> e % 2 == 0 ? "Even" : "Odd")
                .subscribe(groupFlux -> groupFlux
                        .scan(new LinkedList<>(),
                                (list, elem) -> {
                                    list.add(elem);
                                    if (list.size() > 2) {
                                        list.remove(0);
                                    }
                                    return list;
                                }).filter(arr -> !arr.isEmpty())
                        .subscribe(data -> log.info("{} : {} ", groupFlux.key(), data)));

        System.out.println("####### Flat mapping streams:");
        Flux.just("user-1", "user-2", "user-3")
                .flatMap(u -> requestBooks(u)
                .map(b -> u + "/" + b))
                .subscribe(r -> log.info("onNext: {}", r));

        Thread.sleep(500);

        // Sampling elements
        System.out.println("####### Flat mapping streams:");
        Flux.range(1, 100)
                .delayElements(Duration.ofMillis(1))
                .sample(Duration.ofMillis(20))
                .subscribe(e -> log.info("sampled: {}", e));
        Thread.sleep(1000);

        // Blocking structures:
        // * The toIterable method transforms reactive Flux into a blocking Iterable .
        // * The toStream method transforms reactive Flux into a blocking Stream API. As
        //of Reactor 3.2, it uses the toIterable method under the hood.
        // * The blockFirst method blocks the current thread until the upstream signals its
        //first value or completes.
        // * The blockLast method blocks the current thread until the upstream signals its
        //last value or completes. In the case of the onError signal, it throws the exception
        //in the blocked thread.


        // Peeking while sequence processing
        // We can peek at various stages of the processing pipeline.
        Flux.just(1,2,3,4,5)
                .concatWith(Flux.error(new RuntimeException("Connection error")))
                .doOnEach(s -> log.info("signal: {}", s))
                .subscribe();


        //Materializing and dematerializing signals
        // When you need to process a stream in terms of signals not in terms of data
        System.out.println("####### Materializing Strings ");
        Flux.range(1,3)
                .doOnNext(e -> log.info("data : {}", e))
                .materialize()
                .doOnNext(e -> log.info("signal : {}", e))
                .dematerialize()
                .collectList()
                .subscribe(r -> log.info("result: {}", r));

        // Logging signals
        Flux.range(1, 5)
                .log()
                .subscribe(r -> log.info("log: {}", r));
    }


    // mock service
    static Random random = new Random();
    public static Flux<String> requestBooks(String user) {
        return Flux.range(1, random.nextInt(3) + 1)
                .map( i -> "book-" + i)
                .delayElements(Duration.ofMillis(3));

    }
}
