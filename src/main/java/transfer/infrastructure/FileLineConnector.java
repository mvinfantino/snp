package transfer.infrastructure;

import transfer.domain.Sink;
import transfer.domain.Source;
import transfer.domain.Target;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Stream;

public final class FileLineConnector implements Source<String>, Target<String> {
    private final String name;
    private final Path path;

    public FileLineConnector(String name, Path path) {
        this.name = Objects.requireNonNull(name, "name");
        this.path = Objects.requireNonNull(path, "path");
    }

    @Override
    public String name() {
        return name;
    }

    public Path path() {
        return path;
    }

    @Override
    public Stream<String> read() {
        try {
            return Files.lines(path, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read '" + name + "'", failure);
        }
    }

    @Override
    public Sink<String> openSink() {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            BufferedWriter writer = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            return new LineSink(writer);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot open '" + name + "' for writing", failure);
        }
    }

    private final class LineSink implements Sink<String> {
        private final BufferedWriter writer;
        private boolean closed;

        private LineSink(BufferedWriter writer) {
            this.writer = writer;
        }

        @Override
        public void write(String element) throws IOException {
            if (closed) {
                throw new IllegalStateException("Sink for '" + name + "' is closed");
            }
            writer.write(Objects.requireNonNull(element, "element"));
            writer.newLine();
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                writer.close();
            }
        }
    }
}
