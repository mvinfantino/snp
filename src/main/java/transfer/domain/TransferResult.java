package transfer.domain;

public record TransferResult(long elementCount) {
    public TransferResult {
        if (elementCount < 0) {
            throw new IllegalArgumentException("elementCount must be >= 0");
        }
    }
}
