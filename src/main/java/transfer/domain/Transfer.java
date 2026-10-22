package transfer.domain;

import java.util.Objects;

public record Transfer<T>(Source<T> source, Target<T> target) {
    public Transfer {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
    }
}
