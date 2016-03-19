package com.example.hp.lisa;

import java.io.Serializable;

/**
 * Created by hp on 03-07-2015.

 import java.io.Serializable;

 /**
 * Created by hp on 27-06-2015.
 */
public class Data implements Serializable {

    private float x;
    private float y;
    private float z;
    private long time;


    public Data(float x,float y,float z, long time){
        this.x=x;
        this.y=y;
        this.z=z;
        this.time=time;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getTime() {
        return time;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
