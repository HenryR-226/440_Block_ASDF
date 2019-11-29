package com.abc.handoff;

import com.abc.pp.stringhandoff.*;
import com.programix.thread.*;

public class StringHandoffImpl implements StringHandoff {

    public StringHandoffImpl() {
        // FIXME
    }

    @Override
    public synchronized void pass(String msg, long msTimeout)
            throws InterruptedException,
                   TimedOutException,
                   ShutdownException,
                   IllegalStateException {

        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public synchronized void pass(String msg)
            throws InterruptedException,
                   ShutdownException,
                   IllegalStateException {

        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public synchronized String receive(long msTimeout)
            throws InterruptedException,
                   TimedOutException,
                   ShutdownException,
                   IllegalStateException {

        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public synchronized String receive()
            throws InterruptedException,
                   ShutdownException,
                   IllegalStateException {

        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public synchronized void shutdown() {
        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public Object getLockObject() {
        return this;
    }
}