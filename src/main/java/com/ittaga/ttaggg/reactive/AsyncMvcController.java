package com.ittaga.ttaggg.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

@RestController("/async")
public class AsyncMvcController {
    private final AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

    @GetMapping("/rest")
    public ListenableFuture<ResponseEntity<String>> rest() {
        ListenableFuture<ResponseEntity<String>> f1 = asyncRestTemplate.getForEntity("http://localhost:8080/async?req=hello", String.class);
        f1.addCallback(s -> {
                ListenableFuture<ResponseEntity<String>> f2 = asyncRestTemplate.getForEntity("http://localhost:8080/async?req=hello", String.class);
                f2.addCallback(s2 -> {
                        ListenableFuture<ResponseEntity<String>> f3 = asyncRestTemplate.getForEntity("http://localhost:8080/async?req=hello", String.class);
                    },
                    e -> System.out.println("ERROR"));
            },
            e -> System.out.println("ERROR"));
        return f1;
    }

    @GetMapping("")
    public String rest2(@RequestParam("req") String req) {
        System.out.println(req);
        return req + "response";
    }

}
