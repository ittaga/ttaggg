package com.ittaga.ttaggg.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
public class MonoFluxController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/mono/rest")
    public Mono<String> mono() {
        log.info("pos1");
        Mono<String> mono = Mono.just("hello")
            .doOnNext(c -> log.info(c))
            .log();
        String monoString = mono.block();
        log.info("mono: {}", monoString);
        log.info("pos2");
        return mono;
    }

    @GetMapping("/flux/items")
    public Mono<List<String>> monoCollection() {
        Mono<List<String>> mono = Mono.just(Arrays.asList("hello", "abc"))
            .log();
        return mono;
    }

    /*
     * stream 형태로 응답 전달이 가능,
     * flux에서 제공하는 다양한 operator 가공하다.
     * */
    @GetMapping(value = "/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> flux() {
        Flux<Long> longFlux = Flux
            .<Long, Long>generate(() -> 1L, (id, sink) -> {
                sink.next(id + 2L);
                return id + 2L;
            })
            .take(100);
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(longFlux, interval)
            .map(tu -> Event.of(tu.getT1(), tu.getT2()));
    }

    public static class Event {
        private Long first;
        private Long second;

        public Event(Long first, Long second) {
            this.first = first;
            this.second = second;
        }

        public static Event of(Long first, Long second) {
            return new Event(first, second);
        }

        public Long getFirst() {
            return first;
        }

        public Long getSecond() {
            return second;
        }
    }
}
