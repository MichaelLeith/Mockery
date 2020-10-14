package work.teamteam.mock;

public class Verifier {
    private final long numCalls;

    public Verifier(final long numCalls) {
        this.numCalls = numCalls;
    }

    public void verify(final Tracker tracker, final String key, final Object... args) {
        final long calls = tracker.get(key, args);
        if (numCalls != calls) {
            throw new RuntimeException("expected " + numCalls + ", but was called " + calls + " times");
        }
    }
}
