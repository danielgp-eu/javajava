# AGENTS.md — Quick guide for AI coding agents

Purpose: help an AI agent become productive immediately in this repository by summarizing architecture, workflows, conventions and examples.

1) Big picture
- Project type: Java 26 Maven multi-component utility with both CLI and small web UI.
- Main entry points:
  - `javajava.JavaJavaClass` — Picocli-based CLI exposing ~14 subcommands (see `src/main/java/javajava/JavaJavaClass.java`).
  - `javajava.JavaJavaWebClass` — Undertow HTTP handler + JTE templates for the web UI (see `src/main/java/javajava/JavaJavaWebClass.java`).
- Packaging: assembly creates a "jar-with-dependencies" fat JAR in `target/` (check `pom.xml` assembly plugin). The app can be run as a CLI or started as a small web server.

2) Important files & directories (exact references)
- `pom.xml` — project JDK target (Java 26), dependencies (Picocli, Undertow, JTE, Jackson, Log4j2), plugins (maven-assembly, jacoco, dependency-check, qodana). Critical for build/test/security gates.
- `src/main/java/javajava/JavaJavaClass.java` — CLI command registration (subcommand patterns, Picocli annotations).
- `src/main/java/javajava/JavaJavaWebClass.java` — Web UI startup, template wiring (JTE), Undertow handlers.
- `src/main/resources/web/templates/` — JTE templates used by web UI (e.g. `index.jte`).
- `src/main/resources/log4j2.xml` — log configuration and rotating file appenders used by app and tests.
- `src/main/resources/project.properties` — runtime defaults (bind address, ports, path behavior).
- `jte-classes/` — precompiled/generated JTE classes that show template usage patterns.

3) How to build, test and run (exact commands)
- Full build (assembly + checks):
  mvn -DskipTests=false clean package
- Run tests only:
  mvn test
- Run security & quality gates (dependency-check + qodana in CI):
  mvn verify
- Run CLI from built jar (example):
  java -jar target/javajava-*-jar-with-dependencies.jar --help
  java -jar target/javajava-*-jar-with-dependencies.jar analyze-pom-files --help
- Start web UI locally (from IDE or jar):
  java -jar target/javajava-*-jar-with-dependencies.jar web --port 8080
  (See `project.properties` and `JavaJavaWebClass` for available flags)

4) Project-specific conventions & patterns
- Picocli subcommands: commands are registered as nested classes or separate classes and wired in `JavaJavaClass`. Use `--help` from the fat jar to discover runtime flags.
- Templates: JTE is used server-side; templates live under `src/main/resources/web/templates` and compiled outputs are in `jte-classes/`. When changing templates, recompile via the Maven build.
- Logging: uses Log4j2 with file appenders configured in `log4j2.xml`. Expect rotating hourly files under `logs/` and watch file names used by tests and runtime.
- Packaging: the assembly plugin produces a single executable jar; CI signs and publishes artifacts (GPG, checksums configured in `pom.xml`). Do not attempt to run classes from target/classes without running `mvn package` first when templates or generated resources changed.

5) Integration points & external dependencies
- OWASP Dependency-Check is configured in `pom.xml` and invoked during `verify`; high CVE severity may fail CI.
- Qodana static analysis and JaCoCo coverage thresholds are enforced via Maven plugins.

6) Quick examples for agents modifying the code
- To add a CLI subcommand: follow existing pattern in `JavaJavaClass` — create a new class annotated with `@Command`, implement `call()`/`run()`, and register it in the main command registration.
- To add a web route/template: update `JavaJavaWebClass` handlers and add a new JTE template under `src/main/resources/web/templates`. Run `mvn package` to regenerate `jte-classes/` and include changes in the assembly.

7) Where to look for tests and quality checks
- Unit tests: `src/test/java/javajava/` (JUnit 6). Look for parameterized tests and example fixtures.
- Unit tests: `src/test/java/javajava/` (JUnit Jupiter / JUnit 6). Tests use the `org.junit.jupiter.*` API and the 
  pom property `junit-jupiter.version` controls the version. Look for parameterized tests and example fixtures.
- Coverage enforcement: JaCoCo plugin in `pom.xml` (see `<prepare-agent>` and `<report>` goals).

8) Notes & gotchas
- Java 26 is used; ensure agent uses a matching runtime for compilation and local runs.
- The fat jar name includes version metadata from Maven — use wildcard `javajava-*-jar-with-dependencies.jar` when scripting locally.
- Logging/paths may be environment specific (see `project.properties`) — tests and the app expect Windows path-style defaults in that file.

References: `pom.xml`, `src/main/java/javajava/JavaJavaClass.java`, `src/main/java/javajava/JavaJavaWebClass.java`, `src/main/resources/log4j2.xml`, `src/main/resources/project.properties`, `src/main/resources/web/templates/index.jte`, `jte-classes/`.
