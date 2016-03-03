package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.*;
public class StandAloneCallableTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        threads=Thread.activeCount();
        executorService=Executors.newSingleThreadExecutor();
    }
    @After public void tearDown() throws Exception {
        executorService.shutdown();
        Thread.sleep(100);
        checkThreadCount();
    }
    private void checkThreadCount() {
        int threads=Thread.activeCount();
        if(threads>this.threads) p((threads-this.threads)+" extra threads!");
    }
    public static void p(String string) {
        System.out.println(string);
    }
    public static void printThreads() {
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null) p(thread.toString()+" is alive: "+thread.isAlive()+", is interrupted:  "+thread.isInterrupted());
    }
    public <T> Future<T> run(Callable<T> callable) throws InterruptedException,ExecutionException {
        return executorService.submit(callable);
    }
    public <T> T runAndWait(Callable<T> callable) throws InterruptedException,ExecutionException {
        Future<T> future=run(callable);
        while(!future.isDone())
            Thread.yield();
        return future.get();
    }
    Integer run() throws InterruptedException,ExecutionException {
        return runAndWait(new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                Thread.currentThread().setName(name);
                return expected;
            }
        });
    }
    @Test public void test1() throws InterruptedException,ExecutionException {
        assertEquals(expected,run());
        //printThreads();
    }
    @Test public void test2() throws InterruptedException,ExecutionException {
        assertEquals(expected,run());
        //printThreads();
    }
    @Test public void test3() throws InterruptedException,ExecutionException {
        assertEquals(expected,run());
        //printThreads();
    }
    private ExecutorService executorService;
    int threads;
    final Integer expected=1;
    final String name="callable";
    final int sleep=0;
}
