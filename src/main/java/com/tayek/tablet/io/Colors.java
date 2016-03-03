package com.tayek.tablet.io;
public class Colors {
    public int color(int index,boolean state) {
        if(index==rows*columns) // reset
            return state?resetOn:resetOff;
        else if(index/columns%2==0) return state?whiteOn:whiteOff;
        else return state?on[index%columns]:off[index%columns];
    }
    public int aColor(int index,boolean state) {
        return color(index,state)|0xff000000; // android seems to want this
    }
    public final int rows=2,columns=5,n=rows*columns+1;
    public final int background=0xd0d0e0;
    public final Integer[] on=new Integer[columns],off=new Integer[columns];
    public final Integer whiteOn=0xffffff,whiteOff=0xe0e0e0;
    public final Integer resetOn=0xffff80,resetOff=0x80ffff;
    {
        on[0]=0xff0000;
        on[1]=0xffff00;
        on[2]=0x00ff00;
        on[3]=0x0000ff;
        on[4]=0xff8000;
        off[0]=0x800000;
        off[1]=0x808000;
        off[2]=0x008000;
        off[3]=0x000080;
        off[4]=0x804000;
    }

}
