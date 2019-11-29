package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;
import com.programix.thread.*;


public class TestPPStringHandoffShutdownWhileReceiving extends TestPPStringHandoffBase {
    public TestPPStringHandoffShutdownWhileReceiving(StringHandoffFactory factory,
                                                TestThreadFactory threadFactory) {
        super("shtudown() called while receiving", new BasicScoringInfo(1, 10), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testShutdownCalledWhileReceiving();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testShutdownCalledWhileReceiving() throws InterruptedException {
        StringHandoff handoff = createDS();

        long msDelayBeforeShutdown = 1000L;
        ShutdownHelper shutdownHelper = new ShutdownHelper(handoff, 1000L);
        NanoTimer timer = NanoTimer.createStopped();
        try {
            outln("Calling receive(5000)");
            timer.resetAndStart();
            String item = handoff.receive(5000);
            timer.stop();
            outln("Just received '" + item + "', but should have thrown ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("Got exception: " + x, true);
            outln("   seconds until exception", timer.getElapsedSeconds(), msDelayBeforeShutdown / 1000.0, 0.080, 7);
        }

        checkAfterShutdown(handoff);

        shutdownHelper.stopRequest();
        shutdownHelper.waitUntilDone(5000);
    }
}
