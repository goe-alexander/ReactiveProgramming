package com.example.reactive.domain;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TemperatureSensor {
    private final ApplicationEventPublisher publisher;
    private final Random rand = new Random();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public TemperatureSensor(ApplicationEventPublisher publisher){
        this.publisher = publisher;
    }

    @PostConstruct
    public void startProcessing() {
        this.executorService.schedule(this::probe, 1, TimeUnit.SECONDS);
    }

    private void probe(){
        double temperature = 16 + rand.nextGaussian()*10;
        publisher.publishEvent(new Temperature(temperature));
        // schedule next read after some delay
        executorService.schedule(this::probe, rand.nextInt(5000), TimeUnit.MILLISECONDS);
    }
}
