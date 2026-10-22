package transfer.sample;

import java.util.List;

public final class Customers {
    public static final Customer ADA = new Customer("C-001", "Ada Lovelace", "London");
    public static final Customer GRACE = new Customer("C-002", "Grace Hopper", "New York");
    public static final Customer LINUS = new Customer("C-003", "Linus Torvalds", "Helsinki");
    public static final List<Customer> ALL = List.of(ADA, GRACE, LINUS);

    public static List<String> lines() {
        return ALL.stream().map(Customer::line).toList();
    }

    private Customers() {
    }
}
