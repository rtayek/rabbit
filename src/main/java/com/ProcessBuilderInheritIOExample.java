package com;
import java.io.IOException;
import java.util.*;
public class ProcessBuilderInheritIOExample {
    public static void main(String[] args) throws InterruptedException,IOException {
        String strings[]=new String[]{"ping","192.168.1.2"};
        List<String> c=Arrays.asList(strings);
        ProcessBuilder pb=new ProcessBuilder(c);
        pb.inheritIO();
        System.out.println("Run command with inheritIO set");
        Process process=pb.start();
        process.waitFor();
    }
}
