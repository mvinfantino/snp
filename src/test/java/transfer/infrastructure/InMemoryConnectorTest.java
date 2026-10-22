package transfer.infrastructure;

import org.junit.jupiter.api.Test;
import transfer.application.TransferService;
import transfer.domain.Sink;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.sample.Customer;
import transfer.sample.Customers;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryConnectorTest {

    @Test
    void readingAdaDoesNotSeeGraceAddedLater() throws Exception {
        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", List.of(Customers.ADA));

        try (Stream<Customer> read = inbox.read();
             Sink<Customer> sink = inbox.openSink()) {
            sink.write(Customers.GRACE);
            assertThat(read.toList()).containsExactly(Customers.ADA);
        }

        assertThat(inbox.contents()).containsExactly(Customers.ADA, Customers.GRACE);
    }

    @Test
    void anInboxCanLaterReceiveWhatItSent() throws TransferException {
        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", Customers.ALL);
        InMemoryConnector<Customer> outbox = new InMemoryConnector<>("outbox", List.of());
        TransferService service = new TransferService();

        service.execute(new Transfer<>(inbox, outbox));
        service.execute(new Transfer<>(outbox, inbox));

        assertThat(outbox.contents()).containsExactly(Customers.ADA, Customers.GRACE, Customers.LINUS);
        assertThat(inbox.contents()).containsExactly(
                Customers.ADA, Customers.GRACE, Customers.LINUS,
                Customers.ADA, Customers.GRACE, Customers.LINUS);
    }
}
