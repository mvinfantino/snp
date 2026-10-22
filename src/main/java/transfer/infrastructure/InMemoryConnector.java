package transfer.infrastructure;

import transfer.domain.Sink;
import transfer.domain.Source;
import transfer.domain.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class InMemoryConnector<T> implements Source<T>, Target<T> {
    private final String name;
    private final List<T> data = new ArrayList<>();

    public InMemoryConnector(String name, Collection<T> initial) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(initial, "initial");
        for (T element : initial) {
            data.add(Objects.requireNonNull(element, "element"));
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Stream<T> read() {
        return new ArrayList<>(data).stream();
    }

    @Override
    public Sink<T> openSink() {
        return new MemorySink();
    }

    public List<T> contents() {
        return List.copyOf(data);
    }

    private final class MemorySink implements Sink<T> {
        private boolean closed;

        @Override
        public void write(T element) {
            ensureOpen();
            data.add(Objects.requireNonNull(element, "element"));
        }

        @Override
        public void close() {
            closed = true;
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("Sink for '" + name + "' is closed");
            }
        }
    }
}
