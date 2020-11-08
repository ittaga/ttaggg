package com.ittaga.ttaggg;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class OldAsyncProcess {
    /*
    * 서블릿 스펙은 InputStream, OutputStream을 구현해서 blocking 방식으로 구현됨
    * block 되는 순간 thread가 wait 상태가 되고 context swtiching을 위해 CPU 자원이 많이 소모됨
    *
    * 서블릿 3.0 부터는 비동기 방식이 지원됨
    * 비동기 작업시 서블릿 스레드를 반환하여 자원소모를 줄임
    * */

    @Test
    public void callback() throws InterruptedException {
        /*
         * Callback 비동기 작업의 결과에 대한 처리
         * */
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask cb = new CallbackFutureTask(() -> {
            Thread.sleep(1000);
            System.out.println("Async");
            return "Hello";
        }, res -> System.out.println(res));
        System.out.println("Exit");

        es.execute(cb);
        es.shutdown();

        Thread.sleep(2000L);
    }


    @Test
    public void future() throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newCachedThreadPool();

        /*
        * Future 비동기 작업의 결과에 대한 핸들러 오브젝트
        * */
        Future<String> result = es.submit(() -> {
            Thread.sleep(2000l);
            return "Hello";
        });

        System.out.println(result.get());
        System.out.println("Exit");
    }

    interface SucessCallback {
        void onSuccess(String result);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SucessCallback sc;

        public CallbackFutureTask(Callable<String> callable, SucessCallback sc) {
            super(callable);
            this.sc = sc;
        }

        /*
        * 비동기 작업이 완료되면 호출됨
        * */
        @Override
        protected void done() {
            System.out.println("done");
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
