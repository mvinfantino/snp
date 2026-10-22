package transfer.application;

import org.junit.jupiter.api.Test;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;
import transfer.infrastructure.InMemoryConnector;
import transfer.testing.FailingSource;
import transfer.testing.FailingTarget;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransferServiceTest {
    private final TransferService service = new TransferService();

    @Test
    void copiesEveryElement() throws TransferException {
        InMemoryConnector<String> source = new InMemoryConnector<>("source", List.of("a", "b", "c"));
        InMemoryConnector<String> target = new InMemoryConnector<>("target", List.of());

        TransferResult result = service.execute(new Transfer<>(source, target));

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(source.contents()).containsExactly("a", "b", "c");
        assertThat(target.contents()).containsExactly("a", "b", "c");
    }

    @Test
    void emptySourceWritesNothing() throws TransferException {
        InMemoryConnector<String> source = new InMemoryConnector<>("source", List.of());
        InMemoryConnector<String> target = new InMemoryConnector<>("target", List.of("kept"));

        TransferResult result = service.execute(new Transfer<>(source, target));

        assertThat(result.elementCount()).isZero();
        assertThat(target.contents()).containsExactly("kept");
    }

    @Test
    void targetFailureKeepsSuccessfulWrites() {
        InMemoryConnector<String> source = new InMemoryConnector<>("source", List.of("a", "b", "c"));
        FailingTarget<String> target = new FailingTarget<>("target", 1);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(source, target)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isEqualTo(1);
        assertThat(failure).hasMessageContaining("source");
        assertThat(failure).hasMessageContaining("target");
        assertThat(failure).hasMessageContaining("1");
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(target.written()).containsExactly("a");
        assertThat(target.sinkClosed()).isTrue();
    }

    @Test
    void sourceFailureKeepsSuccessfulWrites() {
        FailingSource<String> source = new FailingSource<>("source", List.of("a", "b", "c"), 1);
        FailingTarget<String> target = new FailingTarget<>("target", -1);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(source, target)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isEqualTo(1);
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(target.written()).containsExactly("a");
        assertThat(target.sinkClosed()).isTrue();
    }
}
