package com.gb.restaurant.CATPrintSDK;

/**
 * Created by apple on 22/11/17.
 */


import java.util.concurrent.locks.ReentrantLock;

public class IO {
    private final ReentrantLock locker = new ReentrantLock();

    public IO() {
    }

    public int Write(byte[] buffer, int offset, int count) {
        return -1;
    }

    public int Read(byte[] buffer, int offset, int count, int timeout) {
        return -1;
    }

    public void SkipAvailable() {
    }

    public boolean IsOpened() {
        return false;
    }

    protected void Lock() {
        this.locker.lock();
    }

    protected void Unlock() {
        this.locker.unlock();
    }
}
