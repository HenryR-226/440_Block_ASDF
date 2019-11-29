package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;


public class TestPPStringHandoffShutdownWhileIdle extends TestPPStringHandoffBase {
    public TestPPStringHandoffShutdownWhileIdle(StringHandoffFactory factory,
                                                TestThreadFactory threadFactory) {
        super("shtudown() called while idle", new BasicScoringInfo(2, 8), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testShutdownCalledWhileIdle();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testShutdownCalledWhileIdle() throws InterruptedException {
        StringHandoff handoff = createDS();

        ShutdownHelper shutdownHelper = new ShutdownHelper(handoff, 0L);
        Thread.sleep(200); // give helper a chance to call shutdown()

        checkAfterShutdown(handoff);

        shutdownHelper.stopRequest();
        shutdownHelper.waitUntilDone(5000);
    }
}
