package transfer.testing;

import transfer.domain.Sink;
import transfer.domain.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FailingTarget<T> implements Target<T> {
    private final String name;
    private final int failAt;
    private final List<T> written = new ArrayList<>();
    private boolean sinkClosed;

    public FailingTarget(String name, int failAt) {
        this.name = Objects.requireNonNull(name, "name");
        if (failAt < -1) {
            throw new IllegalArgumentException("failAt must be >= -1");
        }
        this.failAt = failAt;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Sink<T> openSink() {
        return new FailingSink();
    }

    public List<T> written() {
        return List.copyOf(written);
    }

    public boolean sinkClosed() {
        return sinkClosed;
    }

    private final class FailingSink implements Sink<T> {
        private int attempts;
        private boolean closed;

        @Override
        public void write(T element) {
            if (closed) {
                throw new IllegalStateException("Sink for '" + name + "' is closed");
            }
            if (failAt >= 0 && attempts == failAt) {
                throw new IllegalStateException("write failed at index " + attempts);
            }
            written.add(Objects.requireNonNull(element, "element"));
            attempts++;
        }

        @Override
        public void close() {
            closed = true;
            sinkClosed = true;
        }
    }
}
