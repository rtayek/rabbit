package com;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
public class ProcessBuilderExample {
    public static void main(String[] args) throws InterruptedException,IOException {
        String command="testpb.bat";
        String strings[]=new String[]{"ping","192.168.1.2"};
        List<String> c=Arrays.asList(strings);
        ProcessBuilder pb=new ProcessBuilder(c);
        System.out.println("run command: "+command);
        Process process=pb.start();
        int errCode=process.waitFor();
        System.out.println("done, any errors? "+(errCode==0?"No":"Yes"));
        System.out.println("output:\n"+output(process.getInputStream()));
        System.out.println("err:\n'"+output(process.getErrorStream())+"'");
    }
    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb=new StringBuilder();
        BufferedReader br=null;
        try {
            br=new BufferedReader(new InputStreamReader(inputStream));
            String line=null;
            while((line=br.readLine())!=null) {
                sb.append(line+System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }
}
