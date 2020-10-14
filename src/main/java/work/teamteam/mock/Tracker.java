package work.teamteam.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Tracker {
    private static volatile Visitor lastCall = null;
    private final List<Visitor.Description> callHistory = new ArrayList<>();
    private final Map<String, CallHistory> callHistories = new HashMap<>();
    private int size = 0;

    // key should be name + signature
    public boolean visit(final Visitor visitor, final String key, final Object... args) {
        lastCall = visitor;
        synchronized (this) {
            callHistory.add(new Visitor.Description(key, args));
        }
        return true;
    }

    public Map<String, CallHistory> collect() {
        synchronized (this) {
            if (!callHistory.isEmpty() && size != callHistory.size()) {
                size = callHistory.size();
                callHistories.clear();
                for (final Visitor.Description description : callHistory) {
                    CallHistory hist = callHistories.get(description.getKey());
                    if (hist == null) {
                        callHistories.put(description.getKey(), new CallHistory(description.getArgs()));
                    } else {
                        hist.update(description.getArgs());
                    }
                }
            }
        }
        return callHistories;
    }

    public static Mock rollbackLast() {
        final List<Visitor.Description> hist = lastCall.getTracker().callHistory;
        return new Mock(lastCall, hist.remove(hist.size() - 1));
    }

    public long get(final String key, final Object[] args) {
        final CallHistory hist = collect().get(key);
        return hist == null ? 0L : hist.get(args);
    }

    public void reset() {
        synchronized (this) {
            callHistory.clear();
            callHistories.clear();
        }
    }

    // @todo: thread safety?
    private static final class CallHistory {
        private final Map<List<Object>, Long> perArgset;

        public CallHistory(final Object... args) {
            perArgset = new HashMap<>();
            perArgset.put(Arrays.asList(args), 1L);
        }

        public void update(final Object... args) {
            final List<Object> wrapper = Arrays.asList(args);
            final Long l = perArgset.get(wrapper);
            perArgset.put(wrapper, l == null ? 1L : l + 1);
        }

        public long get(final Object[] args) {
            return perArgset.getOrDefault(Arrays.asList(args), 0L);
        }
    }
}
