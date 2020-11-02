package project.reactor;

import org.hibernate.dialect.identity.MySQLIdentityColumnSupport;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.invoke.VolatileCallSite;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class Examples {

    public static void main(String[] args) throws InterruptedException {
        Flux<String> stream1 = Flux.just("Hello", "World");
        Flux<Integer> str2 = Flux.fromArray(new Integer[]{1, 2, 3, 4, 5});
        Flux<Integer> str3 = Flux.fromIterable(Arrays.asList(9, 8, 7));
        Flux<Integer> rangeStream = Flux.range(1, 100);


        Mono<String> stream5 = Mono.just("ONe");
        Mono<String> stream6 = Mono.justOrEmpty(null);
        Mono<String> stream7 = Mono.justOrEmpty(Optional.empty());


        // This type of subscription demands Long.MAX_VALUE from the producer. It is to be avoided
        stream1.subscribe(data -> System.out.println("on Next: " + data),
                err -> {/*Ignored, we have no errors*/},
                () -> System.out.println("COMPLETED!")
        );
        // it is often recommended to control the producer by providing the take size
        // Notice here the stream will never COMPLETE
        rangeStream.subscribe(data -> System.out.println("on Next: " + data),
                err -> {/*Ignored, we have no errors*/},
                () -> System.out.println("COMPLETED!"),
                subscription -> {
                    subscription.request(5);
                    subscription.cancel();
                }
        );

        //Canceling the subscription can also be done by the Disposable object (one level above Subscription)
        Disposable disposableStream = Flux.interval(Duration.ofMillis(50))
                .subscribe(data -> System.out.println("onNext: {}" + data));
        Thread.sleep(200);
        disposableStream.dispose();

        // Implementing custom subscribers:

        Subscriber<String> customSubscriber = new Subscriber<String>() {
            volatile Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                System.out.println("initial request for 1 element");
                subscription.request(1);
            }

            @Override
            public void onNext(String s) {
                System.out.println("on Next: " + s);
                System.out.println("Requesting one more element");
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("There was an error: " + throwable);
            }

            @Override
            public void onComplete() {
                System.out.println("Completed our custom subscriber");
            }
        };

        Flux<String> streamFroCustom = Flux.just("Hello", "World" , "!");
        streamFroCustom.subscribe(customSubscriber);
        streamFroCustom.subscribe(new MyCustomSubscriber<String>());

        // The previous custom subscriber breaks TCK requirements (does not account for backpressure)
        // It is recommended to extend BaseSubscriber class from project Reactor
    }

    static class MyCustomSubscriber<T> extends BaseSubscriber<T>{
        public void hookOnSubscribe(Subscription subscription) {
            System.out.println("Initial request for one element from BASE###");
            request(1);
        }

        public void hookOnNext(T value) {
            System.out.println("on Next: " + value);
            System.out.println("requesting 1 more element ");
            request(1);
        }
    }
}
