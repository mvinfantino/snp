package transfer.domain;

import java.util.stream.Stream;

public interface Source<T> extends DataStore {
    Stream<T> read();
}
