package com.tayek;

import java.io.IOException;

interface I {
    class A {}
    static class B {}
}
public class C {
    private class P {
        int x;
    }
    void foo() {
        P p=new P();
        int x=p.x;
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        I.A a=new I.A();
        I.B b=new I.B();
    }

}
