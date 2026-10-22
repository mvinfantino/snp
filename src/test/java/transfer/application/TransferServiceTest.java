package transfer.application;

import org.junit.jupiter.api.Test;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;
import transfer.infrastructure.InMemoryConnector;
import transfer.sample.Customer;
import transfer.sample.Customers;
import transfer.testing.FailingSource;
import transfer.testing.FailingTarget;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransferServiceTest {
    private final TransferService service = new TransferService();

    @Test
    void copiesAdaGraceAndLinus() throws TransferException {
        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", Customers.ALL);
        InMemoryConnector<Customer> outbox = new InMemoryConnector<>("outbox", List.of());

        TransferResult result = service.execute(new Transfer<>(inbox, outbox));

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(inbox.contents()).containsExactlyElementsOf(Customers.ALL);
        assertThat(outbox.contents()).containsExactly(Customers.ADA, Customers.GRACE, Customers.LINUS);
    }

    @Test
    void emptyInboxLeavesTheOutboxUntouched() throws TransferException {
        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", List.of());
        InMemoryConnector<Customer> outbox = new InMemoryConnector<>("outbox", List.of(Customers.ADA));

        TransferResult result = service.execute(new Transfer<>(inbox, outbox));

        assertThat(result.elementCount()).isZero();
        assertThat(outbox.contents()).containsExactly(Customers.ADA);
    }

    @Test
    void stopsAfterAdaWhenTheOutboxFails() {
        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", Customers.ALL);
        FailingTarget<Customer> outbox = new FailingTarget<>("outbox", 1);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(inbox, outbox)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isEqualTo(1);
        assertThat(failure).hasMessageContaining("inbox");
        assertThat(failure).hasMessageContaining("outbox");
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(outbox.written()).containsExactly(Customers.ADA);
        assertThat(outbox.written()).doesNotContain(Customers.GRACE, Customers.LINUS);
        assertThat(outbox.sinkClosed()).isTrue();
    }

    @Test
    void stopsAfterAdaWhenTheInboxFails() {
        FailingSource<Customer> inbox = new FailingSource<>("inbox", Customers.ALL, 1);
        FailingTarget<Customer> outbox = new FailingTarget<>("outbox", -1);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(inbox, outbox)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isEqualTo(1);
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(outbox.written()).containsExactly(Customers.ADA);
        assertThat(outbox.written()).doesNotContain(Customers.GRACE, Customers.LINUS);
        assertThat(outbox.sinkClosed()).isTrue();
    }
}
