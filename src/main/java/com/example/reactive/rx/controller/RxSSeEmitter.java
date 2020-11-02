package com.example.reactive.rx.controller;

import com.example.reactive.domain.Temperature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public class RxSSeEmitter extends SseEmitter {
    public static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000;
    private final Observer<Temperature> observer;

    RxSSeEmitter() {
        super(SSE_SESSION_TIMEOUT);

        this.observer = new Observer<Temperature>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {

            }

            @Override
            public void onNext(@NotNull Temperature temperature) {
                try {
                    RxSSeEmitter.this.send(temperature);
                } catch (IOException e) {
                    System.out.println(e);;
                }
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                System.out.println(throwable);
            }

            @Override
            public void onComplete() {
                System.out.println("Done !");
            }
        };
    }
    Observer<Temperature> getSubscriber() {
        return observer;
    }
}