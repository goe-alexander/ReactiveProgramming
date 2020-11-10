package reactive_progression;

import io.reactivex.*;
import io.reactivex.observers.DisposableObserver;
import org.jetbrains.annotations.NotNull;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@org.jetbrains.annotations.NotNull ObservableEmitter<String> observableEmitter) throws Exception {
                observableEmitter.onNext("Hellow Reactive World");
                observableEmitter.onComplete();
            }
        });

        DisposableObserver<String> observer = new DisposableObserver<String>() {
            @Override
            public void onNext(@NotNull String s) {
                System.out.println(s);
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("DONE!");
            }
        };

        observable.subscribe(observer);



        Observable.create(sub -> {
            sub.onNext("Hellow from Lambda!");
            sub.onComplete();
        }).subscribe(
                System.out::println,
                System.out::println,
                () -> System.out.println("DONE FOR this lambda!")
        );

        Observable.just("1", "2", "3", "4").subscribe(System.out::println,
                System.out::println,
                () -> System.out.println("DONE FOR this simple just reactive_progression.observer!")
        );
        Observable.fromArray(new String[] {"A", "B", "C"}).subscribe(System.out::println,
                System.out::println,
                () -> System.out.println("DONE FOR this Array just reactive_progression.observer!")
        );
        //Observable from Callable
        Observable<String> hello =  Observable.fromCallable(() -> "HEllo from callable");
        hello.subscribe(System.out::println,
                System.out::println,
                () -> System.out.println("DONE FOR Callable observable"));
        //"Observable from Future"
/*        Future<String> future = Executors.newCachedThreadPool().submit(() -> " World in the future");
        Observable<String> world = Observable.fromFuture(future);
        world.subscribe(System.out::println,
                System.out::println,
                () -> System.out.println("DONE FOR Future observable!"));*/

        // Generating async sequence in the future
        //Observable.interval(1, TimeUnit.SECONDS).subscribe(e -> System.out.println("Received: " + e));
        //Thread.sleep(5000);

        // Zipping multiple streams of data
        Observable.zip(Observable.just("A", "B", "C"),
                Observable.just("1", "2", "3"),
                (x,y) -> x + y)
        .forEach(System.out::println);


        // Flowable example -> reaplces the Observable but extends Publisher right of the bat
        Flowable.just(1,2,3)
                .map(String::valueOf)
                .toObservable()  // Conversion from Observable to flowable is straight forward. Observable in RX2 was designed as push only
                .toFlowable(BackpressureStrategy.ERROR) // Reverse needs an additional BackPressure strategy
                .subscribe(System.out::println, System.out::println, () -> System.out.println("Done for flowable"));
    }
}
