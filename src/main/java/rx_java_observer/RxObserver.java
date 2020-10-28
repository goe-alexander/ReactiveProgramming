package rx_java_observer;

public interface RxObserver<T> {
    void onNext(T next);
    void onComplete();
    void onError();
}
