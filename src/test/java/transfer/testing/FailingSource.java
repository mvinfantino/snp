package transfer.testing;

import transfer.domain.Source;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FailingSource<T> implements Source<T> {
    private final String name;
    private final List<T> elements;
    private final int failAt;

    public FailingSource(String name, List<T> elements, int failAt) {
        this.name = Objects.requireNonNull(name, "name");
        this.elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
        if (failAt < 0) {
            throw new IllegalArgumentException("failAt must be >= 0");
        }
        this.failAt = failAt;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Stream<T> read() {
        Iterator<T> iterator = new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                if (index == failAt) {
                    throw new IllegalStateException("read failed at index " + index);
                }
                return index < elements.size();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements.get(index++);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
