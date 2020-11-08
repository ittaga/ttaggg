package com.ittaga.ttaggg.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.function.Function;

@RestController
public class CompletionController {
    private final AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    private final String URL = "http://localhost:8080/async?req={req}";

    @GetMapping("/complete/rest")
    public DeferredResult<String> rest() {
        DeferredResult<String> df = new DeferredResult<>();
        Completion.from(asyncRestTemplate.getForEntity(URL, String.class))
            .andApply(s -> asyncRestTemplate.getForEntity(URL, String.class, s))
            .andError(e -> df.setErrorResult(e))
            .andAccept(s -> df.setResult(s.getBody()));
        return df;
    }

    @GetMapping("/complete")
    public String rest2(@RequestParam("req") String req) {
        System.out.println(req);
        return req + "response";
    }

    public static class ErrorCompletion extends Completion {
        public Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        void error(Throwable e) {
            econ.accept(e);
        }
    }
    public static class Completion {
        Consumer<ResponseEntity<String>> consumer;
        Completion next;
        private Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function;

        public Completion(Consumer<ResponseEntity<String>> consumer) {
            this.consumer = consumer;
        }

        public Completion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
            this.function = function;
        }

        public Completion() {
        }

        public void andAccept(Consumer<ResponseEntity<String>> consumer) {
            Completion c = new Completion(consumer);
            this.next = c;
        }

        public Completion andError(Consumer<ResponseEntity<String>> consumer) {
            Completion c = new Completion(consumer);
            this.next = c;
            return c;
        }

        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
            Completion c = new Completion(function);
            this.next = c;
            return c;
        }

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            Completion c = new Completion();
            lf.addCallback(
                s -> {
                    c.complete(s);
                }, ex -> {
                    c.error(ex);
                }
            );
            return c;
        }

        void error(Throwable ex) {
            if (next != null) next.error(ex);
        }

        void complete(ResponseEntity<String> s) {
            if (next != null) next.run(s);
        }

        void run(ResponseEntity<String> value) {
            if (consumer != null) consumer.accept(value);
            else if (function != null) {
                ListenableFuture<ResponseEntity<String>> lf = function.apply(value);
                lf.addCallback(s -> complete(s), e -> error(e));
            }

        }
    }

}
