package transfer;

import transfer.application.TransferService;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;
import transfer.infrastructure.FileLineConnector;
import transfer.infrastructure.InMemoryConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        try {
            run();
        } catch (TransferException | IOException failure) {
            System.err.println(failure.getMessage());
            System.exit(1);
        }
    }

    private static void run() throws TransferException, IOException {
        TransferService service = new TransferService();
        InMemoryConnector<String> source = new InMemoryConnector<>(
                "customers-in",
                List.of("ada", "grace", "linus"));

        InMemoryConnector<String> memoryTarget = new InMemoryConnector<>("customers-out", List.of());
        TransferResult memoryResult = service.execute(new Transfer<>(source, memoryTarget));
        System.out.println("In-memory transfer wrote " + memoryResult.elementCount() + " elements");

        Path file = Files.createTempDirectory("data-transfer").resolve("customers.txt");
        FileLineConnector fileTarget = new FileLineConnector("customers-file", file);
        TransferResult fileResult = service.execute(new Transfer<>(source, fileTarget));
        System.out.println("File transfer wrote " + fileResult.elementCount() + " elements to " + file);
    }

    private Main() {
    }
}
