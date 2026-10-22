package transfer.infrastructure;

import org.junit.jupiter.api.Test;
import transfer.application.TransferService;
import transfer.domain.Sink;
import transfer.domain.Transfer;
import transfer.domain.TransferException;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryConnectorTest {

    @Test
    void readIsASnapshot() throws Exception {
        InMemoryConnector<String> store = new InMemoryConnector<>("store", List.of("a"));

        try (Stream<String> read = store.read();
             Sink<String> sink = store.openSink()) {
            sink.write("b");
            assertThat(read.toList()).containsExactly("a");
        }

        assertThat(store.contents()).containsExactly("a", "b");
    }

    @Test
    void sameConnectorCanBeSourceAndThenTarget() throws TransferException {
        InMemoryConnector<String> left = new InMemoryConnector<>("left", List.of("a", "b"));
        InMemoryConnector<String> right = new InMemoryConnector<>("right", List.of());
        TransferService service = new TransferService();

        service.execute(new Transfer<>(left, right));
        service.execute(new Transfer<>(right, left));

        assertThat(right.contents()).containsExactly("a", "b");
        assertThat(left.contents()).containsExactly("a", "b", "a", "b");
    }
}
