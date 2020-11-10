package com.example.reactive.repository;

import com.example.reactive.domain.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryOrderRepository implements OrderRepository {
    final Map<String, Order> ordersMap;

    public InMemoryOrderRepository() {
        this.ordersMap = new HashMap<>();
    }

    @Override
    public Mono<Order> findById(String id) {
        return Mono.justOrEmpty(ordersMap.get(id));
    }

    @Override
    public Mono<Order> save(Order order) {
        ordersMap.put(order.getId(), order);
        return Mono.just(order);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        ordersMap.remove(id);
        return null;
    }
}
