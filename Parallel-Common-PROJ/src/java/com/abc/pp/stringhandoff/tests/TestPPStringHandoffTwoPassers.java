package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;
import com.programix.thread.*;


public class TestPPStringHandoffTwoPassers extends TestPPStringHandoffBase {
    public TestPPStringHandoffTwoPassers(StringHandoffFactory factory,
                                                TestThreadFactory threadFactory) {
        super("two passers, expecting IllegalStateException", new BasicScoringInfo(3, 8), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testTwoPassers();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testTwoPassers() throws InterruptedException {
        StringHandoff handoff = createDS();

        Passer passer = new Passer(handoff, 0L, 0L);
        Thread.sleep(500); // give passer a change to load up a string

        try {
            outln("Attempting to pass 'grape'");
            handoff.pass("grape");
            outln("Just passed 'grape'", false);
        } catch ( IllegalStateException x ) {
            // This is correct, Passer is already trying to pass right now
            outln("Got exception: " + x, true);
        }

        outln("handoff.receive()", handoff.receive(), SAMPLE_DATA[0]);
        outln("handoff.receive()", handoff.receive(), SAMPLE_DATA[1]);
        outln("handoff.receive()", handoff.receive(), SAMPLE_DATA[2]);
        Thread.sleep(500); // give passer a chance to put in a fourth string

        // Shutting down the Passer should cause StringHandoff to clear
        // out the value that Passer was trying to pass and should also
        // clear out the status that someone is trying to pass.
        passer.stopRequest();
        passer.waitUntilDone(5000);

        // Create a new Passer passing different data to be sure that the old
        // data got removed upon the 'stopRequest'.
        String[] otherData = new String[] { "orange", "pear", "tomato" };
        passer = new Passer(handoff, 0L, 0L, otherData);
        Thread.sleep(500); // give passer a change to load up a string

        outln("handoff.receive()", handoff.receive(), otherData[0]);
        outln("handoff.receive()", handoff.receive(), otherData[1]);
        Thread.sleep(500); // give passer a chance to put in a thrid string

        passer.stopRequest();
        passer.waitUntilDone(5000);

        Receiver receiver = new Receiver(handoff, 0L, 200L);

        // This should succeed since the old Passer is done.
        NanoTimer timer = NanoTimer.createStarted();
        handoff.pass("grape", 500L);
        outln("...passed grape", true);
        outln("   seconds until passed", timer.getElapsedSeconds(), 200.0 / 1000.0, 0.080, 7);

        receiver.stopRequest();
        receiver.waitUntilDone(5000);
    }
}
