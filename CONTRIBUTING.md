# Contributing

Keep changes small and focused on one phase. Prefer explicit Java code, useful
tests, and short documentation that records a real decision or limitation.

Before opening a pull request, run:

```bash
bash ./mvnw clean verify
```

Do not commit secrets, generated build output, or changes from a later phase
before the current phase has been reviewed.
