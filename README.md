# data-transfer

A transfer copies what is in a source store and leaves it in a target store. When it finishes, it says how many elements moved. If it stops early, it says how many were already written and leaves those writes in place.

## Run

```
sbt run
sbt test
sbt "it/test"
```

`sbt run` copies three customers from an inbox to an outbox, writes them into a temporary file, then reads that file back:

```
Copied 3 customers: Ada Lovelace, Grace Hopper, Linus Torvalds.
Wrote 3 customers to <temporary path>
Ada Lovelace (C-001) from London
Grace Hopper (C-002) from New York
Linus Torvalds (C-003) from Helsinki
```

`sbt test` checks the copy in memory. `sbt "it/test"` checks the same copy through real files.

## The three customers

- C-001, Ada Lovelace, London
- C-002, Grace Hopper, New York
- C-003, Linus Torvalds, Helsinki

A line in the file looks like `C-001, Ada Lovelace, London`.

## Where things live

- `transfer.domain` holds a source, a target, a transfer, and what a failure looks like.
- `transfer.application` holds `TransferService`, which does the copy.
- `transfer.infrastructure` holds the in-memory store and the text-file store.
- `transfer.sample` holds Ada, Grace, and Linus.

## Another connector

A new store is a class that implements `Source`, `Target`, or both. That is where a database would plug in. This project does not start MongoDB or Redis.
