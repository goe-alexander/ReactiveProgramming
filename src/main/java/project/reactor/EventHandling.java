package project.reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class EventHandling {
    static Random random = new Random();
    public static void main(String[] args) throws InterruptedException {
        // IN reactive streams the onError method is a terminal operation. After which the reactive sequenece stops executing
        // Implementing a client that handles mocker unreliable books service
        Flux.just("user-1")
                .flatMap(user ->
                        recommendedBooks(user)
                            .retry(5)
                            .timeout(Duration.ofSeconds(3))
                            .onErrorResume(e -> Flux.just("The Martian")))
                .subscribe(
                        b -> log.info("onNext: {}",b),
                        e -> log.info("onError: {}",e.getMessage()),
                                () -> log.info("onComplete")
                );
        Thread.sleep(2000);

        // Dealing with Backpressure
        // Hot and Cold Streams:
        //Cold publishers behave in such a way that, whenever a subscriber appeaars, all of the seq data is generated for that subsciber
        System.out.println("######## Cold Publishers Flux Example");
        Flux<String> coldPublisher = Flux.defer(() -> {
            log.info("Generating new items");
            return Flux.just(UUID.randomUUID().toString());
        });

        log.info("No data was generated so far");
        coldPublisher.subscribe(e -> log.info("onNext: {}",e));
        coldPublisher.subscribe(e -> log.info("onNext: {}",e));
        log.info("Data was generated twice for 2 subscribers");

        //Multicasting elements of a stream
        //ConnectableFlux is generated to fulfill the most hungry demand. It's cached so that all other subscribers can process the data at their pace.
        System.out.println("######## Connectable Flux Example");
        Flux<Integer> source = Flux.range(0, 3)
                .doOnSubscribe(s ->
                        log.info("new subscription for the cold publisher"));
        ConnectableFlux<Integer> conn = source.publish();
        conn.subscribe(e -> log.info("[Subscriber 1 ] onNext: {}", e));
        conn.subscribe(e -> log.info("[Subscriber 2 ] onNext: {}", e));
        log.info("all subscribers are ready, connecting");
        conn.connect();

        //Caching elements of a stream
        //With ConnectableFlux is easy to implement different data caching strategies
        System.out.println("######## Cacheable Flux Example");
        Flux<Integer> cacheableSource = Flux.range(0, 2 )
                .doOnSubscribe(s -> log.info("new subscription for the cold publisher"));
        Flux<Integer> cachedSource = cacheableSource.cache(Duration.ofSeconds(1));
        cachedSource.subscribe(e -> log.info("[S1: ] onNext: {}", e));
        cachedSource.subscribe(e -> log.info("[S2: ] onNext: {}", e));

        Thread.sleep(1200);
        cachedSource.subscribe(e -> log.info("[S3: ] onNext: {}", e));


        //Sharing elements of a stream
        // A ConnectableFlux can be shared transforming the cold publisher into a hot one. Meaning, we no lnger have to wait for a
        // subscriber to appear to start processing
        System.out.println("######## Shareable Flux Example");
        Flux<Integer> shareableSource = Flux.range(0,5)
                .delayElements(Duration.ofMillis(100))
                .doOnSubscribe(s -> log.info("New Subscription for cold publisher"));
        Flux<Integer> cachedSource2 = shareableSource.share();

        cachedSource2.subscribe(e -> log.info("[S1] onNext: {}", e));
        Thread.sleep(400);
        cachedSource2.subscribe(e -> log.info("[S2] onNext: {}", e));
        Thread.sleep(500);

        //Dealing with Time
        Flux.range(0,5)
                .delayElements(Duration.ofMillis(100))
                .elapsed()
                .subscribe(e -> log.info("Elapsed {} ms: {}", e.getT1(), e.getT2()));
        Thread.sleep(500);

        // Composing and transforming reactive Streams

    }

    public static Flux<String> recommendedBooks(String userId) {
        return Flux.defer(() -> {
            if(random.nextInt(10) < 7){
                return Flux.<String>error(new RuntimeException("Err"))
                        .delaySequence(Duration.ofMillis(100));
            }else{
                return Flux.just("Blue Mars", "The Expanse")
                        .delayElements(Duration.ofMillis(50));
            }
        }).doOnSubscribe(s -> log.info("Request for {}", userId));
    }
}
