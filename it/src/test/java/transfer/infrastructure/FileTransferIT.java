package transfer.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import transfer.application.TransferService;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;

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
    void memoryToFile() throws TransferException, IOException {
        InMemoryConnector<String> source = new InMemoryConnector<>("memory", List.of("ada", "grace"));
        Path output = tempDir.resolve("out.txt");

        TransferResult result = service.execute(new Transfer<>(source, new FileLineConnector("file", output)));

        assertThat(result.elementCount()).isEqualTo(2);
        assertThat(Files.readAllLines(output)).containsExactly("ada", "grace");
    }

    @Test
    void fileToMemory() throws IOException, TransferException {
        Path input = tempDir.resolve("in.txt");
        Files.write(input, List.of("ada", "grace"));
        InMemoryConnector<String> target = new InMemoryConnector<>("memory", List.of());

        TransferResult result = service.execute(new Transfer<>(new FileLineConnector("file", input), target));

        assertThat(result.elementCount()).isEqualTo(2);
        assertThat(target.contents()).containsExactly("ada", "grace");
    }

    @Test
    void fileToFile() throws IOException, TransferException {
        Path input = tempDir.resolve("in.txt");
        Path output = tempDir.resolve("out.txt");
        Files.write(input, List.of("a", "b", "c"));

        TransferResult result = service.execute(new Transfer<>(
                new FileLineConnector("in", input),
                new FileLineConnector("out", output)));

        assertThat(result.elementCount()).isEqualTo(3);
        assertThat(Files.readAllLines(output)).containsExactly("a", "b", "c");
    }

    @Test
    void directoryTargetFailsWithNothingWritten() {
        InMemoryConnector<String> source = new InMemoryConnector<>("memory", List.of("ada", "grace"));
        FileLineConnector target = new FileLineConnector("directory", tempDir);

        Throwable thrown = catchThrowable(() -> service.execute(new Transfer<>(source, target)));

        assertThat(thrown).isInstanceOf(TransferException.class);
        TransferException failure = (TransferException) thrown;
        assertThat(failure.elementsWritten()).isZero();
        assertThat(failure).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(Files.isDirectory(tempDir)).isTrue();
    }
}
