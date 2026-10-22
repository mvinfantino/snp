package transfer;

import transfer.application.TransferService;
import transfer.domain.Transfer;
import transfer.domain.TransferException;
import transfer.domain.TransferResult;
import transfer.infrastructure.FileLineConnector;
import transfer.infrastructure.InMemoryConnector;
import transfer.sample.Customer;
import transfer.sample.Customers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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

        InMemoryConnector<Customer> inbox = new InMemoryConnector<>("inbox", Customers.ALL);
        InMemoryConnector<Customer> outbox = new InMemoryConnector<>("outbox", List.of());
        TransferResult copied = service.execute(new Transfer<>(inbox, outbox));
        System.out.println("Copied " + copied.elementCount() + " customers: " + names(outbox.contents()) + ".");

        Path notebook = Files.createTempDirectory("data-transfer").resolve("customers.txt");
        InMemoryConnector<String> lines = new InMemoryConnector<>("customer-lines", Customers.lines());
        TransferResult written = service.execute(new Transfer<>(lines, new FileLineConnector("notebook", notebook)));
        System.out.println("Wrote " + written.elementCount() + " customers to " + notebook);

        InMemoryConnector<String> readBack = new InMemoryConnector<>("notebook-lines", List.of());
        service.execute(new Transfer<>(new FileLineConnector("notebook", notebook), readBack));
        readBack.contents().stream()
                .map(Customer::fromLine)
                .map(Customer::introduction)
                .forEach(System.out::println);
    }

    private static String names(List<Customer> customers) {
        return customers.stream().map(Customer::name).collect(Collectors.joining(", "));
    }

    private Main() {
    }
}
