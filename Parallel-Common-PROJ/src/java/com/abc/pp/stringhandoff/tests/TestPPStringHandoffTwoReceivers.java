package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;
import com.programix.thread.*;


public class TestPPStringHandoffTwoReceivers extends TestPPStringHandoffBase {
    public TestPPStringHandoffTwoReceivers(StringHandoffFactory factory,
                                                TestThreadFactory threadFactory) {
        super("two receivers, expecting IllegalStateException", new BasicScoringInfo(3, 9), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testTwoReceivers();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testTwoReceivers() throws InterruptedException {
        StringHandoff handoff = createDS();

        Receiver receiver = new Receiver(handoff, 200L, 0L);
        Thread.sleep(500);

        try {
            outln("Attempting to call receive()");
            handoff.receive();
            outln("Just received something--trouble", false);
        } catch ( IllegalStateException x ) {
            outln("Got exception: " + x, true);
        }

        NanoTimer timer = NanoTimer.createStopped();
        for ( int i = 0; i < 3; i++ ) {
            timer.resetAndStart();
            String item = SAMPLE_DATA[i];
            handoff.pass(item, 500L);
            timer.stop();
            outln("...passed '" + item + "'", true);
            outln("   seconds until passed", timer.getElapsedSeconds(), (i == 0 ? 0L : 200L) / 1000.0, 0.080, 7);
        }

        Thread.sleep(500); // give receiver a chance to try to receive again

        // Shutting down the Receiver should cause StringHandoff to clear
        // out the status that someone is trying to receive.
        receiver.stopRequest();
        receiver.waitUntilDone(5000);

        // Create a Passer passing different data to be sure that the old
        // data is irrelevant.
        String[] otherData = new String[] { "orange", "pear", "tomato" };
        Passer passer = new Passer(handoff, 0L, 0L, otherData);
        Thread.sleep(500); // give passer a change to load up a string

        outln("handoff.receive()", handoff.receive(), otherData[0]);
        outln("handoff.receive()", handoff.receive(), otherData[1]);

        passer.stopRequest();
        passer.waitUntilDone(5000);
    }
}
