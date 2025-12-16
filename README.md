# Personal Financial Manager (PFM) Core

> **A High-Performance, Framework-less Financial Ledger in Pure Java 21.**

## 1. Project Mission & Philosophy
This repository is an engineering study in building **high-concurrency, deterministic, and observable** backend systems without the hidden complexity ("magic") of standard industry frameworks like Spring Boot, Hibernate, or Jdbi.

My goal is not just to build a Personal Financial Manager, but to implement a **Principled Modular Monolith** that prioritizes:

* **Explicitness over Convenience:** Code should be readable without understanding meta-frameworks or annotation processing.
* **Compile-Time Safety:** Heavy reliance on strong typing and contracts (Protobuf) rather than runtime reflection.
* **Hermeticity:** Tests are self-contained, execute in milliseconds, and use fakes rather than mocks.
* **Modern Concurrency:** Utilizing Java 21 Virtual Threads (Project Loom) for high-throughput I/O without reactive complexity.

## 2. The "No Magic" Architecture

### 2.1 Pure Dependency Injection (Manual DI)
Instead of relying on classpath scanning (e.g., `@Autowired`, Guice `Modules`), I use a strict "Composition Root" pattern.
* **Why:** This eliminates "startup time" surprises and circular dependency errors. It makes the object graph immediately obvious to any reader.
* **Implementation:** The `Main.java` class explicitly constructs the graph. If a service requires a repository, it is passed via the constructor. There are **zero** reflection-based injection containers.
* [Link to Composition Root (Main.java)](pfm-backend/src/main/java/dev/zambone/Main.java)

### 2.2 Contract-First API (gRPC + Protobuf)
REST/JSON is often fragile due to loose typing. I define the **Contract** first using Protocol Buffers.
* **Why:** Ensures backward compatibility, strict type safety across services, and high-performance binary serialization.
* **Implementation:** Service definitions (e.g., `HouseholdService`) generate the Java base classes. The implementation logic simply extends these generated bases.
* [Link to Proto Definitions](pfm-backend/src/main/proto/pfm/household/v1/household_service.proto)

### 2.3 Predictable Data Access (Raw JDBC)
I deliberately avoid ORMs (Hibernate/JPA).
* **Why:** ORMs introduce the "N+1 select" problem, unpredictable memory caching, and complex proxy debugging. In a financial ledger, data integrity and query performance are paramount.
* **Implementation:** I use raw `PreparedStatement` and manual `ResultSet` mapping. I explicitly manage `Connection` lifecycles and transactions to ensure atomic consistency.
* [Link to Repository Implementation](pfm-backend/src/main/java/dev/zambone/household/storage/SqlHouseholdRepository.java)

### 2.4 Virtual Threads (Project Loom)
I utilize `Executors.newVirtualThreadPerTaskExecutor()` for request handling.
* **Why:** Allows writing simple, blocking-style code (easy to read and debug) that scales to millions of concurrent connections, matching the throughput of complex Reactive (Non-blocking) stacks.

## 3. Technology Stack

* **Language:** Java 21 LTS
* **Transport:** gRPC / Protobuf 3
* **Database:** PostgreSQL 16
* **Migration:** Flyway (SQL-based version control)
* **Testing:** JUnit 5 + Google Truth (Fluent Assertions)
* **Build System:** Gradle (Kotlin DSL)

## 4. Domain & Roadmap

### Current Implementation
* **User Context:** Secure password hashing (Argon2 concept), User Identity management.
* **Household Domain:** Multi-tenant structures allowing users to group accounts and invite members.
* **Infrastructure:** Postgres `DataSource` management, Docker Compose environment, and Flyway migrations.

### Engineering Roadmap
* **The Ledger (Core):** Implementing a Double-Entry Accounting system.
  * *Challenge:* Ensuring strict atomicity (ACID) across multi-table updates using manual transaction management.
* **Observability:** Manual implementation of Tracing and Metrics (OpenTelemetry) to visualize request latency without "auto-instrumentation" agents.
* **AuthZ/AuthN:** Replacing the current "Trust-Header" mechanism with a robust JWT/PASETO implementation.
* **Frontend Bridge:** Envoy Proxy configuration for gRPC-Web support.

## 5. How to Run

### Prerequisites
* Java 21
* Docker & Docker Compose

### Setup
```bash
# Start Infrastructure (Postgres)
docker-compose up -d

# Run Migrations & Start Server
./gradlew run
