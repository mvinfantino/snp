# data-transfer

Generic data stores with source and target connectors. A transfer plugs one source store into one target store. `TransferService` copies every element and returns the count, or fails with the number of elements already written.

## Run

```
sbt run
sbt test
sbt "it/test"
```

`sbt run` copies a small in-memory sample, then writes the same elements to a temporary file, and prints each element count.

## Layout

- `transfer.domain` — store roles, a transfer, and failure
- `transfer.application` — `TransferService`
- `transfer.infrastructure` — in-memory and file-line connectors
