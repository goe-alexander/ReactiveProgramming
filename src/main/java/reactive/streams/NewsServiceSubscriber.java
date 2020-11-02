package reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NewsServiceSubscriber implements Subscriber<Newsletter> {

    final Queue<Newsletter> mailBox = new ConcurrentLinkedQueue<>();
    final int take;
    final AtomicInteger remaining = new AtomicInteger();
    Subscription subscription;

    public NewsServiceSubscriber(int take) {
        this.take = take;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(take);
    }

    @Override
    public void onNext(Newsletter newsletter) {
        mailBox.offer(newsletter);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("There was an error ! " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("Take was completed! ");
    }

    public Optional<Newsletter>  eventuallyReadDigest(){
        Newsletter newsletter = mailBox.poll();
        if (newsletter != null) {
            if (remaining.decrementAndGet() == 0) {
                subscription.request(take);
                remaining.set(take);
            }
            return Optional.of(newsletter);
        }
        return Optional.empty();
    }



}
