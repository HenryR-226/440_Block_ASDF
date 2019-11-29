package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;
import com.programix.thread.*;


public class TestPPStringHandoffShutdownWhilePassing extends TestPPStringHandoffBase {
    public TestPPStringHandoffShutdownWhilePassing(StringHandoffFactory factory,
                                                TestThreadFactory threadFactory) {
        super("shtudown() called while passing", new BasicScoringInfo(1, 10), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testShutdownCalledWhilePassing();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testShutdownCalledWhilePassing() throws InterruptedException {
        StringHandoff handoff = createDS();

        long msDelayBeforeShutdown = 1000L;
        ShutdownHelper shutdownHelper = new ShutdownHelper(handoff, 1000L);
        NanoTimer timer = NanoTimer.createStopped();
        try {
            outln("Calling pass(\"watermelon\")");
            timer.resetAndStart();
            handoff.pass("watermelon");
            timer.stop();
            outln("Just passed, but should have thrown ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("Got exception: " + x, true);
            outln("   seconds until exception", timer.getElapsedSeconds(), msDelayBeforeShutdown / 1000.0, 0.080, 7);
        }

        checkAfterShutdown(handoff);

        shutdownHelper.stopRequest();
        shutdownHelper.waitUntilDone(5000);
    }
}
