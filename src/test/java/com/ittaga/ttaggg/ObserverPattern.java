package com.ittaga.ttaggg;

import org.junit.jupiter.api.Test;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObserverPattern {
    /*
    *
    * Observer 패턴은 iterator 패턴과 기능은 똑같지만 (쌍대성) 단순한 Observer 패턴은 단점이 있다.
    * 1. iterator와 다르게 Complete 시점을 알지 못한다.
    * 2. 예외에 대한 처리가 고려되어 있지 않다.
    *
    * 하지만 Observer 패턴은 동시성을 가지고 복잡한 작업을 하는 코드를 간결하게 처리할 수 있는 방식이다.
    * */

    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                setChanged();
                notifyObservers(i);
            }
        }
    }

    @Test
    public void observe() {
        Observer observer = new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        IntObservable observable = new IntObservable();
        observable.addObserver(observer);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(observable);

        System.out.println(Thread.currentThread().getName() + "EXIT");
    }
}
