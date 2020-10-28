package com.example.reactive.rx_controller;

import com.example.reactive.domain.Temperature;
import io.reactivex.Observer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RxTemperatureController {
    private final RxTemperatureSensor rxTemperatureSensor;

    public RxTemperatureController(RxTemperatureSensor rxTemperatureSensor) {
        this.rxTemperatureSensor = rxTemperatureSensor;
    }


    @RequestMapping(value = "/rx-temp-stream", method = RequestMethod.GET)
    public SseEmitter events(HttpServletRequest request){
        RxSSeEmitter emitter = new RxSSeEmitter();

        rxTemperatureSensor.temperatureStream().subscribe((Observer<? super Temperature>) emitter.getSubscriber());
        return emitter;
    }
}
