package reactive_progression.reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Slf4j
public class AdvancedReactor {

    public static void main(String[] args) throws InterruptedException {
        // The thread scheduling model in Reactor
        // The publishOn operator, allows the moving part of runtime execution to a specified worker
        // This operator is on runtime execution
        // It keeps a queue to which it supplies new elements so that a dedicated worker can consume messages and process them one by one
        // because of the queue the elemnts are ALWAYS processed in sequence
        Scheduler scheduler = new Scheduler() {
            @Override
            public Disposable schedule(Runnable runnable) {
                return null;
            }

            @Override
            public Worker createWorker() {
                return null;
            }
        };

//        Flux.range(0,100)
//                .map(String::valueOf)
//                .filter(s -> s.length() > 1)
//                .publishOn(scheduler)
//                //.map(doBusinessLogic)
//                .subscribe();

        // The subscribeOn operator
        // Is used to control the worker allocation at subscription time.
        // A Worker in project reactor is just an abstraction over a Thread or a Resouce

        //Using Context as oposed to Thread local which will not work because reactive stream processing is split up in multiple threads:
        ThreadLocal<Map<Object, Object>> threadLocal =
                new ThreadLocal<>();
        threadLocal.set(new HashMap<>());
        // Below exaple will give NPE. Uncomment to test
/*        Flux
                .range(0, 10)
                .doOnNext(k ->
                        threadLocal
                                .get()
                                .put(k, new Random(k).nextGaussian())
                )
                .publishOn(Schedulers.parallel())
                .map(k -> threadLocal.get().get(k))
                .blockLast();*/


        // Using context
        // THe only life-cycle period when each Subscriber may be provided with a Context is subscription time.
        Flux.range(0,10)
                .flatMap(k ->
                        Mono.subscriberContext()
                            .doOnNext(context -> {
                                Map<Object, Object> map = context.get("randoms");
                                map.put(k, new Random(k).nextGaussian());
                                log.info("generated random gausian for: {} / values {}", k, map.get(k));
                            })
                            .thenReturn(k)
                )
                .publishOn(Schedulers.parallel())
                .flatMap(k ->
                        Mono.subscriberContext()
                            .map(context -> {
                                Map<Object, Object> map = context.get("randoms");
                                log.info("Found context variable: {}", map.get(k));
                                return map.get(k);
                            })
                )
                .subscriberContext(context ->
                    context.put("randoms", new HashMap<>())
                )
                .subscribe(e -> log.info("onNext: {}", e));

        // Using different contexts at subscriberTime
        printCurrentContext("top")
                .subscriberContext(Context.of("top", "message"))
                .flatMap(__ -> printCurrentContext("middle"))
                .subscriberContext(Context.of("middle", "context"))
                .flatMap(__ -> printCurrentContext("bottom"))
                .subscriberContext(Context.of("bottom", "context"))
                .flatMap(__ -> printCurrentContext("initial"))
                .block();
    }

    private static Mono<Context> printCurrentContext(String someId){
        return Mono.subscriberContext()
                .doOnNext(context -> log.info("id / Context: {} {}",someId, context));
    }
}
