package com.tayek.utilities;

import java.io.*;

public class Utility {
    public static String method() {
        return Thread.currentThread().getStackTrace()[2].getMethodName()+"()";
    }
    public static Integer toInteger(String argument) {
        Integer n=null;
        try {
            n=Integer.valueOf(argument);
        } catch(NumberFormatException e) {
            System.out.println(argument+" is not a valid integer!");
        }
        return n;
    }
    public static void fromFile(final StringBuffer stringBuffer,final File file) {
        try {
            Reader r=new FileReader(file);
            int c=0;
            while((c=r.read())!=-1)
                stringBuffer.append((char)c);
            r.close();
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
