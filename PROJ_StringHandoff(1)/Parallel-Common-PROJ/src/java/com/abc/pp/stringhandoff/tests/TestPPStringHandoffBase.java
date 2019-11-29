package com.abc.pp.stringhandoff.tests;

import java.util.*;

import com.abc.ds.tests.*;
import com.abc.pp.stringhandoff.*;
import com.programix.testing.*;
import com.programix.thread.*;
import com.programix.util.*;

/* deliberate package access */
abstract class TestPPStringHandoffBase extends TestDSBase {

    protected static final long RECEIVE_INTERVAL = 600L;
    protected static final long PASS_INTERVAL = RECEIVE_INTERVAL;
    protected static final long STANDARD_TOLERANCE = 100L;

    protected static final String[] SAMPLE_DATA = {
        "apple", "banana", "cranberry", "date", "eggplant", "fig"
    };

    protected final StringHandoffFactory factory;
    protected final TestDSHelper<String> testHelper;
    protected final TestThreadFactory threadFactory;

    protected TestPPStringHandoffBase(String titleSuffix,
                                      ScoringInfo scoringInfo,
                                      StringHandoffFactory factory,
                                      TestThreadFactory threadFactory) {

        //super("StringHandoff - " + titleSuffix, scoringInfo);
        super(titleSuffix, scoringInfo);
        this.factory = factory;
        this.threadFactory = threadFactory;
        testHelper = new TestDSHelper.Builder<String>()
            .setItemType(String.class)
            .setTestAccess(testAccess)
            .setAllowDuplicates(true)
            .setOrderMatters(true)
            .setWrapItemsInQuotes(true)
            .create();
    }

    protected TestPPStringHandoffBase(String titleSuffix,
                                      ScoringInfo scoringInfo,
                                      StringHandoffFactory factory) {
        this(titleSuffix, scoringInfo, factory, null);
    }

    protected TestPPStringHandoffBase(String titleSuffix,
                                      StringHandoffFactory factory,
                                      TestThreadFactory threadFactory) {
        this(titleSuffix, ScoringInfo.ZERO_POINT_INSTANCE, factory, threadFactory);
    }

    protected TestPPStringHandoffBase(String titleSuffix,
                                      StringHandoffFactory factory) {
        this(titleSuffix, ScoringInfo.ZERO_POINT_INSTANCE, factory, null);
    }


    protected StringHandoff createDS() {
        outln("Creating a new StringHandoff instance ...");
        StringHandoff sh = factory.create();
        outln("   ...created: " + sh.getClass().getCanonicalName());
        return sh;
    }

    protected TestWackyWaiter kickoffWackyWaiter(Object lockObject) throws IllegalStateException {
        if (threadFactory == null) {
            throw new IllegalStateException("can't use WackyWaiter, no TestThreadFactory supplied at construction");
        }
        return new TestWackyWaiter(lockObject, threadFactory, testAccess);
    }

    protected TestNastyNotifier kickoffNastyNotifier(Object lockObject) throws IllegalStateException {
        if (threadFactory == null) {
            throw new IllegalStateException("can't use NastyNotifier, no TestThreadFactory supplied at construction");
        }
        return new TestNastyNotifier(lockObject, threadFactory, testAccess);
    }

    protected void checkAfterShutdown(StringHandoff handoff)
            throws InterruptedException {

        outln("Checking that after shutdown, all methods throw ShutdownException immediately");

        NanoTimer timer = NanoTimer.createStopped();
        try {
            outln("Trying handoff.receive() ...");
            timer.resetAndStart();
            handoff.receive();
            outln("No ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("ShutdownException thrown", true);
            outln("   seconds until exception", timer.getElapsedSeconds(), 0.0, 0.080, 7);
        }

        try {
            outln("Trying handoff.receive(500) ...");
            timer.resetAndStart();
            handoff.receive(500L);
            outln("No ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("ShutdownException thrown", true);
            outln("   seconds until exception", timer.getElapsedSeconds(), 0.0, 0.080, 7);
        }

        try {
            outln("Trying handoff.pass(\"strawberry\") ...");
            timer.resetAndStart();
            handoff.pass("strawberry");
            outln("No ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("ShutdownException thrown", true);
            outln("   seconds until exception", timer.getElapsedSeconds(), 0.0, 0.080, 7);
        }

        try {
            outln("Trying handoff.pass(\"strawberry\", 500) ...");
            timer.resetAndStart();
            handoff.pass("strawberry", 500L);
            outln("No ShutdownException", false);
        } catch ( ShutdownException x ) {
            outln("ShutdownException thrown", true);
            outln("   seconds until exception", timer.getElapsedSeconds(), 0.0, 0.080, 7);
        }
    }

    protected class ShutdownHelper {
        private final StringHandoff ds;
        private final long msDelayBeforeShutdown;
        private final TestDSBase.RunState runState;

        public ShutdownHelper(StringHandoff ds,
                              long msDelayBeforeShutdown) {

            this.ds = ds;
            this.msDelayBeforeShutdown = msDelayBeforeShutdown;

            runState = new TestDSBase.RunState();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    runWork();
                }
            };

            threadFactory.createThreadFor(r, "ShutdownHelper");
        }

        private void runWork() {
            runState.registerCallerAsInternalThread();
            try {
                testAccess.outln(
                    String.format(
                        "waiting %.5f seconds before calling shutdown()", msDelayBeforeShutdown / 1000.0));

                if (msDelayBeforeShutdown > 0L) {
                    Thread.sleep(msDelayBeforeShutdown);
                }

                if (runState.isKeepRunning()) {
                    testAccess.outln("Attempting to call shutdown()...");
                    ds.shutdown();
                }
            } catch ( InterruptedException x ) {
                // ignore and die
            } finally {
                testAccess.outln("ShutdownHelper finished");
                runState.setNoLongerRunning();
            }
        }

        public void stopRequest() {
            runState.stopRequest();
        }

        public boolean waitUntilDone(long msTimeout) throws InterruptedException {
            return runState.waitWhileStillRunning(msTimeout);
        }
    } // type ShutdownHelper

    protected class Passer {
        private final StringHandoff ds;
        private final long msBetweenPassAttempts;
        private final long delayBeforeFirstPass;
        private final TestDSBase.RunState runState;
        private final String[] itemsToPass;

        public Passer(StringHandoff ds,
                      long msBetweenPassAttempts,
                      long delayBeforeFirstPass,
                      String... itemsToPass) {

            this.ds = ds;
            this.delayBeforeFirstPass = delayBeforeFirstPass;
            this.msBetweenPassAttempts = msBetweenPassAttempts;
            this.itemsToPass = itemsToPass;

            runState = new TestDSBase.RunState();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    runWork();
                }
            };

            threadFactory.createThreadFor(r, "Passer");
        }

        public Passer(StringHandoff ds,
                      long msBetweenPassAttempts,
                      long delayBeforeFirstPass) {

            this(ds, msBetweenPassAttempts, delayBeforeFirstPass, SAMPLE_DATA);
        }

        public Passer(StringHandoff ds,
                      long msBetweenPassAttempts) {

            this(ds, msBetweenPassAttempts, 0L);
        }

        private void runWork() {
            runState.registerCallerAsInternalThread();
            try {
                testAccess.outln("Passer starting");
                if (delayBeforeFirstPass > 0) {
                    testAccess.outln("waiting " + delayBeforeFirstPass +
                        " ms before first attempt...");
                    Thread.sleep(delayBeforeFirstPass);
                }
                NanoTimer timer = NanoTimer.createStopped();
                for ( int count = 0; runState.isKeepRunning(); count++ ) {
                    String item = itemsToPass[count % itemsToPass.length];
                    outln("Passer attempting to pass '" + item + "' ...");
                    timer.resetAndStart();
                    ds.pass(item);
                    timer.stop();
                    testAccess.outln(String.format(
                        "passed '%s' after blocking for %.5f seconds",
                        item, timer.getElapsedSeconds()));
                    Thread.sleep(msBetweenPassAttempts);
                }
            } catch ( InterruptedException x ) {
                // ignore and die
            } finally {
                testAccess.outln("Passer finished");
                runState.setNoLongerRunning();
            }
        }

        public void stopRequest() {
            runState.stopRequest();
        }

        public boolean waitUntilDone(long msTimeout) throws InterruptedException {
            return runState.waitWhileStillRunning(msTimeout);
        }
    } // type Passer


    protected class Receiver {
        private final StringHandoff ds;
        private final long delayBeforeFirstRemove;
        private final long msBetweenRemoveAttempts;
        private final ExpectedRemoveList expectedRemoveList;
        private final TestDSBase.RunState runState;

        public Receiver(StringHandoff ds,
                        long msBetweenRemoveAttempts,
                        long delayBeforeFirstRemove) {

            this.ds = ds;
            this.msBetweenRemoveAttempts = msBetweenRemoveAttempts;
            this.delayBeforeFirstRemove = delayBeforeFirstRemove;

            expectedRemoveList = new ExpectedRemoveList();

            runState = new TestDSBase.RunState();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    runWork();
                }
            };

            threadFactory.createThreadFor(r, "Receiver");
        }

        private void runWork() {
            runState.registerCallerAsInternalThread();
            try {
                testAccess.outln("Receiver starting");
                if (delayBeforeFirstRemove > 0) {
                    testAccess.outln("waiting " + delayBeforeFirstRemove +
                        " ms before first attempt...");
                    Thread.sleep(delayBeforeFirstRemove);
                }
                NanoTimer timer = NanoTimer.createStopped();
                while (runState.isKeepRunning()) {
                    timer.resetAndStart();
                    String item = ds.receive();
                    timer.stop();

                    String expectedItem = expectedRemoveList.removeOrNull();
                    if (expectedItem == null) {
                        testAccess.outln(String.format(
                            "received '%s' after blocking for %.5f seconds",
                            item, timer.getElapsedSeconds()));
                    } else {
                        testAccess.outln(String.format(
                            "received '%s' [expected '%s'] after blocking for %.5f seconds",
                            item, expectedItem, timer.getElapsedSeconds()),
                            StringTools.isSame(item, expectedItem));
                    }

                    Thread.sleep(msBetweenRemoveAttempts);
                }
            } catch ( InterruptedException x ) {
                // ignore and die
            } finally {
                testAccess.outln("Receiver finished");
                runState.setNoLongerRunning();
            }
        }

        public void stopRequest() {
            runState.stopRequest();
        }

        public boolean waitUntilDone(long msTimeout) throws InterruptedException {
            return runState.waitWhileStillRunning(msTimeout);
        }

        public void addExpectedItemToBeRemoved(String item) {
            expectedRemoveList.add(item);
        }

        public void addExpectedItemsToBeRemoved(String... items) {
            if (items == null || items.length == 0) return;
            for ( String item : items ) {
                addExpectedItemToBeRemoved(item);
            }
        }

        private class ExpectedRemoveList {
            private List<String> list;

            public ExpectedRemoveList() {
                list = new LinkedList<>();
            }

            public synchronized void add(String item) {
                list.add(item);
            }

            public synchronized String removeOrNull() {
                return list.isEmpty() ? null : list.remove(0);
            }
        } // type ExpectedRemoveList
    } // type Receiver
}
