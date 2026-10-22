package transfer.domain;

public interface Target<T> extends DataStore {
    Sink<T> openSink();
}
