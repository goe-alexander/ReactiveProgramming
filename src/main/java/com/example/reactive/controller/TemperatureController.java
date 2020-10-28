package com.example.reactive.controller;

import com.example.reactive.domain.Temperature;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class TemperatureController {
    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @RequestMapping(value = "/temp-stream", method = RequestMethod.GET)
    public SseEmitter events(HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter();
        clients.add(emitter);

        // Remove emitter from clients on error or disconnect
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onCompletion(() -> clients.remove(emitter));
        return emitter;
    }


    @Async
    @EventListener
    public void handleMessage(Temperature temperature) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        clients.forEach(emitter -> {
            try {
                emitter.send(temperature);
            }catch (Exception ex){
                deadEmitters.add(emitter);
            }
        });
        clients.removeAll(deadEmitters);
    }
}
