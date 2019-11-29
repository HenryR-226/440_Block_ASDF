package com.abc.ds.tests;

import com.abc.ds.filter.*;
import com.abc.ds.tests.TestDSHelper.TestAccess;
import com.programix.testing.*;
import com.programix.thread.ix.*;
import com.programix.util.*;

public abstract class TestDSBase extends BaseTest {
    protected final TestAccess testAccess;

    protected TestDSBase(String title, ScoringInfo scoringInfo) {
        super(title, scoringInfo);

        testAccess = new TestDSHelper.TestAccess() {
            @Override
            public void outln(String msg, boolean success) {
                TestDSBase.this.outln(msg, success);
            }

            @Override
            public void outln(String msg) {
                TestDSBase.this.outln(msg);
            }

            @Override
            public void outlnErrorText(String msg) {
                TestDSBase.this.outlnErrorText(msg);
            }
        };
    }

    protected TestDSBase(String title) {
        this(title, ScoringInfo.ZERO_POINT_INSTANCE);
    }

    /*
    protected void checkZeroLenArray(String methodName, String[] results) {
        if ( results == null ) {
            outln(methodName + " returned null, expected a zero-length array", false);
        } else if ( results.length > 0 ) {
            outln(methodName + " returned an array with length=" + results.length +
                ", expected a zero-length array", false);
        } else {
            outln(methodName + " returned a zero-length array", true);
        }
    }

    protected void checkArrayMatch(String[] results,
                                   String[] expecteds) {

        if ( ObjectTools.isSameArray(results, expecteds) ) {
            String suffix = results.length > 100
                ? "[too many to list here]"
                : "[" + StringTools.formatCommaDelimited(results) + "]";
            outln("all " + results.length +
                " items match what is expected " + suffix, true);
        } else if ( results == null && expecteds != null ) {
            outln("result String[] is null but " +
                "expected String[] is not null and has length of " +
                expecteds.length, false);
        } else if ( results != null && expecteds == null ) {
            outln("result String[] is not null and has a length of " +
                results.length + ", but expected String[] is null", false);
        } else {
            outln("mismatch on Result items and Expected items in array:", false);
            outln("    number of items", results.length, expecteds.length);

            int lenOfLonger = Math.max(results.length, expecteds.length);
            boolean printMatchesToo = expecteds.length <= 100;
            outln(String.format(
                "    %5.5s %-20.20s %-20.20s", "Index", "Result", "Expected"));
            outln(String.format(
                "    %5.5s %-20.20s %-20.20s", "-----", "-----", "---------"));
            for ( int i = 0; i < lenOfLonger; i++ ) {
                boolean hasResult = i < results.length;
                boolean hasExpected = i < expecteds.length;
                boolean matches =
                    hasResult &&
                    hasExpected &&
                    ObjectTools.isSame(results[i], expecteds[i]);

                boolean printLine = printMatchesToo || !matches;
                if ( printLine ) {
                    String text = String.format("    %5d %-20.20s %-20.20s",
                        i,
                        hasResult ? StringTools.quoteWrap(results[i]) : "",
                        hasExpected ? StringTools.quoteWrap(expecteds[i]) : ""
                        );

                    if ( matches ) {
                        outln(text);
                    } else {
                        outlnErrorText(text);
                    }
                }
            }
        }
    }
     */

    protected String[] getFruits(int count) {
        return TestFruitGenerator.getFruits(count);
    }

    protected String[] getFruits(int offset, int count) {
        return TestFruitGenerator.getFruits(offset, count);
    }

    protected String formatCommaDelimited(String... items) {
        return StringTools.formatCommaDelimited(items);
    }

    protected String formatCommaDelimited(int... items) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( int item : items ) {
            if ( first ) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    protected DSFilter<String> createStringLengthFilter(final int lengthToMatch) {
        return new DSFilter<String>() {
            @Override
            public boolean matches(String item) {
                return item != null && item.length() == lengthToMatch;
            }

            @Override
            public String toString() {
                return "DSFilter[string length = " + lengthToMatch + "]";
            }
        };
    }

    public static class RunState {
        private boolean keepRunning;
        private Thread internalThread;
        private boolean stillRunning;
        private final WaiterIx waiter;
        private final WaiterIx.Condition stillRunningCondition;

        public RunState() {
            keepRunning = true;
            stillRunning = true;

            waiter = new WaiterIx(this);
            stillRunningCondition = waiter.createCondition(new WaiterIx.Expression() {
                @Override
                public boolean isTrue() {
                    return stillRunning;
                }
            });
        }

        public synchronized boolean isKeepRunning() {
            return keepRunning;
        }

        public synchronized void registerCallerAsInternalThread() {
            internalThread = Thread.currentThread();
        }

        public synchronized void stopRequest() {
            keepRunning = false;
            waiter.signalChange();
            if (internalThread != null) {
                internalThread.interrupt();
            }
        }

        public synchronized boolean isStillRunning() {
            return stillRunning;
        }

        public synchronized void setNoLongerRunning() {
            stillRunning = false;
            waiter.signalChange();
        }

        public synchronized boolean waitWhileStillRunning(long msTimeout)
                throws InterruptedException {

            return stillRunningCondition.waitWhileTrue(msTimeout);
        }
    } // type RunState
}
