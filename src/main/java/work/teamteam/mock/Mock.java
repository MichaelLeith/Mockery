package work.teamteam.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Mock {
    private final List<Visitor.Fn> state;

    public Mock(final Visitor last, final Visitor.Description description) {
        this.state = new ArrayList<>();
        last.registerCallback(a -> state.isEmpty() ? null : (state.size() == 1 ? state.get(0) : state.remove(0))
                        .apply(a), Objects.requireNonNull(description));
    }

    private Mock add(final Visitor.Fn fn) {
        state.add(fn);
        return this;
    }

    public Mock thenReturn(final Object o) {
        return add(a -> o);
    }

    public Mock thenAnswer(final Function<Object[], Object> fn) {
        return add(fn::apply);
    }

    public Mock thenThrow(final Class<? extends Throwable> e) {
        return thenThrow(Mockery.OBJENESIS.newInstance(e));
    }

    public <T extends Throwable> Mock thenThrow(final T e) {
        return add(a -> { throw e; });
    }
}
