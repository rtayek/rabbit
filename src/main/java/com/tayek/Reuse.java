package com.tayek;
class Reuse {
    Reuse Reuse(Reuse Reuse) {
        this.Reuse=Reuse;
        Reuse:for(;;)
            if(Reuse.Reuse(Reuse)==Reuse) break Reuse;
        return Reuse;
    }
    public static void main(String[] arg) {
        Reuse Reuse=new Reuse();
        Reuse.Reuse(Reuse);
    }
    Reuse Reuse;
}
