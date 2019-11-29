package com.abc.pp.stringhandoff.tests;

import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;


public class TestPPStringHandoffMultiPassReceive extends TestPPStringHandoffBase {
    public TestPPStringHandoffMultiPassReceive(StringHandoffFactory factory,
                                               TestThreadFactory threadFactory) {
        super("multiple items pass and receive", new BasicScoringInfo(6, 12), factory, threadFactory);
    }

    @Override
    protected void performTests() {
        try {
            outln("====================");
            testReceiverFirst();
            outln("====================");
            testPasserFirst();
            outln("====================");
        } catch ( InterruptedException x ) {
            failureExceptionWithStackTrace(x);
        }
    }

    private void testReceiverFirst() throws InterruptedException {
        outln("-- receiver shows up first --");
        StringHandoff sh = createDS();
        Receiver receiver = new Receiver(sh, 0L, 0L);
        try {
            receiver.addExpectedItemsToBeRemoved(SAMPLE_DATA);
            Thread.sleep(200);

            for ( String item : SAMPLE_DATA ) {
                outln("Passing \"" + item + "\"...");
                sh.pass(item);
            }

            Thread.sleep(200);
        } catch (InterruptedException x) {
            failureExceptionWithStackTrace(x);
        } finally {
            receiver.stopRequest();
            receiver.waitUntilDone(2000L);
        }
    }

    private void testPasserFirst() throws InterruptedException {
        outln("-- passer shows up first --");
        StringHandoff sh = createDS();
        Passer passer = new Passer(sh, 0L, 0L, SAMPLE_DATA);
        try {
            Thread.sleep(200);
            for ( String expectedItem : SAMPLE_DATA ) {
                outln("received item", sh.receive(), expectedItem);
            }
            Thread.sleep(200);
        } catch (InterruptedException x) {
            failureExceptionWithStackTrace(x);
        } finally {
            passer.stopRequest();
            passer.waitUntilDone(2000L);
        }
    }
}
