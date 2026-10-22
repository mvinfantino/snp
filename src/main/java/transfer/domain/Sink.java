package transfer.domain;

import java.io.IOException;

public interface Sink<T> extends AutoCloseable {
    void write(T element) throws IOException;

    @Override
    void close() throws IOException;
}
