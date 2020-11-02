package project.reactor;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Log4j2
public class OperatorExamples {
    public static void main(String[] args) {
        // Index and timestamp operators:
        Flux.range(2000, 10)
                .timestamp()
                .index()
                .subscribe(e -> log.info("index: {}, ts: {}, value: {}",
                        e.getT1(),
                        Instant.ofEpochMilli(e.getT2().getT1()),
                        e.getT2().getT2()));

        // Filtering reactive sequences:

    }
}
