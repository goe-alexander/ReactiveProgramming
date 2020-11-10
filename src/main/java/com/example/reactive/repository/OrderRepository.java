package com.example.reactive.repository;

import com.example.reactive.domain.Order;
import reactor.core.publisher.Mono;

public interface OrderRepository {
    Mono<Order> findById(String id);
    Mono<Order> save(Order order);
    Mono<Void> deleteById(String id);

}
