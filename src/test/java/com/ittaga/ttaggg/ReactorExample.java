package com.ittaga.ttaggg;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactorExample {
    /*
    * 1.Publisher -> onSubscribe, onNext*, (onError|onComplete)?
    * 2.Subscriber
    * 3.Subscription
    * 4.Processor
    * */
    @Test
    void reactorBasic() throws InterruptedException {
        Publisher<Integer> pub = getPub(Stream.iterate(1, a -> a + 1)
            .limit(100)
            .collect(Collectors.toList()));
        Publisher<String> mapPub = mapPub(pub, s-> s + "10");
        Publisher<String> reducePub = reducePub(mapPub, "", (a, b) -> a+b);

        reducePub.subscribe(logSubscriber());
    }

    private static <T,R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> integerIntegerIntegerBiFunction) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> s) {
                pub.subscribe(new DelegateSub<T, R>(s) {
                    R result = init;

                    @Override
                    public void onNext(T integer) {
                       result = integerIntegerIntegerBiFunction.apply(result, integer);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                    }
                });
            }
        };
    }

    private static <T, R>Publisher<R> mapPub(Publisher<T> pub, Function<T, R> function) {
        return sub -> pub.subscribe(new DelegateSub<T, R>(sub) {
            @Override
            public void onNext(T integer) {
                sub.onNext(function.apply(integer));
            }
        });
    }

    private static <T> Subscriber<T> logSubscriber() {
        return new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("on subscription: " + subscription);
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T integer) {
                System.out.println("on Next: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("on ERROR: " + t);
            }

            @Override
            public void onComplete() {
                System.out.println("on Complete");
            }
        };
    }

    private static Publisher<Integer> getPub(List<Integer> iter) {
        return sub -> sub.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                try {
                    iter.forEach(s -> sub.onNext(s));
                    sub.onComplete();
                } catch (Throwable t) {
                    sub.onError(t);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    private static class DelegateSub<T, R> implements Subscriber<T> {
        Subscriber subscriber;

        public DelegateSub(Subscriber<? super R> logSub) {
            subscriber = logSub;
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscriber.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            subscriber.onNext(t);

        }

        @Override
        public void onError(Throwable t) {
            subscriber.onError(t);
        }

        @Override
        public void onComplete() {

        }
    }
}
