package transfer.domain;

import java.util.Objects;

public final class TransferException extends Exception {
    private final long elementsWritten;

    public TransferException(String sourceName, String targetName, long elementsWritten, Throwable cause) {
        super(message(sourceName, targetName, elementsWritten), Objects.requireNonNull(cause, "cause"));
        this.elementsWritten = elementsWritten;
    }

    public long elementsWritten() {
        return elementsWritten;
    }

    private static String message(String sourceName, String targetName, long elementsWritten) {
        Objects.requireNonNull(sourceName, "sourceName");
        Objects.requireNonNull(targetName, "targetName");
        if (elementsWritten < 0) {
            throw new IllegalArgumentException("elementsWritten must be >= 0");
        }
        return "Transfer from '%s' to '%s' failed after %d elements"
                .formatted(sourceName, targetName, elementsWritten);
    }
}
