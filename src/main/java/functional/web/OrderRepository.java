package functional.web;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface OrderRepository {

    Mono<Order> findById(String id);

    Mono<Order> save(Order order);

    Mono<Void> deleteById(String id);

    Flux<Order> findAll();
}
