package com.abc.pp.stringhandoff;

import com.programix.thread.*;

/**
 * StringHandoff is used to pass a String from one thread to another.
 * The passer and the receiver meet inside an instance for the handoff.
 * For example, if pass() is called and there is not receiver waiting, the
 * thread calling pass() will block until the receiver arrives. Similarly,
 * if receive() is called and there is no passer waiting, the thread calling
 * receive() will block until the passer arrives.
 * <p>
 * There can only be one thread waiting to pass at any given time. If a second
 * thread tries to call pass() when another thread is already waiting to pass,
 * an IllegalStateException is thrown. Similarly, there can only be one thread
 * waiting to receive at any given time. If a second thread tries to call
 * receive() when another thread is already waiting to receive, an
 * IllegalStateException is thrown. IllegalStateException is a RuntimeException.
 * <p>
 * Methods that take a timeout parameter will throw a TimedOutException if
 * the specified number of milliseconds passes without the handoff occurring.
 * TimedOutException is a RuntimeException.
 * <p>
 * The methods that declare that they may throw a ShutdownException will do
 * so after shutdown() has been called. If a thread is waiting inside a method
 * and another thread calls shutdown(), then the waiting thread will throw
 * a ShutdownException. If a thread calls a method that declares that it may
 * throw a ShutdownException, and any thread has already called shutdown(), then
 * the call will immediately throw the ShutdownException.
 * ShutdownException is a RuntimeException.
 */
public interface StringHandoff {
    void pass(String msg, long msTimeout) throws InterruptedException,
                                                 TimedOutException,
                                                 ShutdownException,
                                                 IllegalStateException;

    void pass(String msg) throws InterruptedException,
                                 ShutdownException,
                                 IllegalStateException;

    String receive(long msTimeout) throws InterruptedException,
                                          TimedOutException,
                                          ShutdownException,
                                          IllegalStateException;

    String receive() throws InterruptedException,
                            ShutdownException,
                            IllegalStateException;

    void shutdown();

    Object getLockObject();
}
