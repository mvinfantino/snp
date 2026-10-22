package transfer.sample;

import java.util.Objects;

public record Customer(String id, String name, String city) {
    public Customer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(city, "city");
        if (id.isBlank() || name.isBlank() || city.isBlank()) {
            throw new IllegalArgumentException("A customer needs an id, a name, and a city");
        }
        if (id.contains(",") || name.contains(",") || city.contains(",")) {
            throw new IllegalArgumentException("Customer fields cannot contain commas");
        }
    }

    public String line() {
        return id + ", " + name + ", " + city;
    }

    public String introduction() {
        return name + " (" + id + ") from " + city;
    }

    public static Customer fromLine(String line) {
        String[] parts = Objects.requireNonNull(line, "line").split(", ", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Cannot read a customer from '" + line + "'");
        }
        return new Customer(parts[0], parts[1], parts[2]);
    }
}
