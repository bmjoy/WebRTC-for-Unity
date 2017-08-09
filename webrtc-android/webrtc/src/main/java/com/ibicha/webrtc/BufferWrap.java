package com.ibicha.webrtc;

/**
 * Created by bhadriche on 8/8/2017.
 */

public class BufferWrap {
    private byte[] buffer;

    public BufferWrap(byte[] buffer){
        this.buffer = buffer;
    }
    public byte[] getBuffer(){
        return buffer;
    }
}
