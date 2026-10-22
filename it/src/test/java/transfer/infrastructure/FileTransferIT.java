package transfer.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import transfer.application.TransferService;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;
import transfer.sample.Customer;
import transfer.sample.Customers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class FileTransferIT {
    @TempDir
    Path tempDir;

    private final TransferService service = new TransferService();

    @Test
    void writesAdaGraceAndLinusToAFile() throws TransferException, IOException {
        InMemoryConnector<String> inbox = new InMemoryConnector<>("inbox", Customers.lines());
        Path notebook = tempDir.resolve("customers.txt");

        TransferResult result = service.execute(new Transfer<>(inbox, new FileLineConnector("notebook", notebook)));

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(Files.readAllLines(notebook)).containsExactly(
                "C-001, Ada Lovelace, London",
                "C-002, Grace Hopper, New York",
                "C-003, Linus Torvalds, Helsinki");
    }

    @Test
    void readsAdaGraceAndLinusBackFromAFile() throws IOException, TransferException {
        Path notebook = tempDir.resolve("customers.txt");
        Files.write(notebook, Customers.lines());
        InMemoryConnector<String> outbox = new InMemoryConnector<>("outbox", List.of());

        TransferResult result = service.execute(new Transfer<>(new FileLineConnector("notebook", notebook), outbox));
        List<Customer> people = outbox.contents().stream().map(Customer::fromLine).toList();

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(people).containsExactly(Customers.ADA, Customers.GRACE, Customers.LINUS);
    }

    @Test
    void copiesTheNotebookFromOneFileToAnother() throws IOException, TransferException {
        Path original = tempDir.resolve("original.txt");
        Path copy = tempDir.resolve("copy.txt");
        Files.write(original, Customers.lines());

        TransferResult result = service.execute(new Transfer<>(
                new FileLineConnector("original", original),
                new FileLineConnector("copy", copy)));

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(Files.readAllLines(copy)).containsExactlyElementsOf(Customers.lines());
    }

    @Test
    void refusesToWriteTheNotebookIntoADirectory() {
        InMemoryConnector<String> inbox = new InMemoryConnector<>("inbox", Customers.lines());
        FileLineConnector notebook = new FileLineConnector("notebook", tempDir);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(inbox, notebook)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isZero();
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(Files.isDirectory(tempDir)).isTrue();
    }
}
