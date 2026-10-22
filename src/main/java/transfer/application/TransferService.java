package transfer.application;

import transfer.domain.Sink;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public final class TransferService {

    public <T> TransferResult execute(Transfer<T> transfer) throws TransferException {
        Objects.requireNonNull(transfer, "transfer");
        long written = 0L;
        try (Stream<T> elements = transfer.source().read();
             Sink<T> sink = transfer.target().openSink()) {
            Iterator<T> iterator = elements.iterator();
            while (iterator.hasNext()) {
                sink.write(iterator.next());
                written++;
            }
            return new TransferResult(written);
        } catch (Exception failure) {
            if (failure instanceof TransferException transferException) {
                throw transferException;
            }
            throw new TransferException(
                    transfer.source().name(),
                    transfer.target().name(),
                    written,
                    failure);
        }
    }
}
