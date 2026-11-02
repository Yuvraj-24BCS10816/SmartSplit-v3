# SmartSplit v3 — Group Expense Management & Settlement System

**Project Based Learning in Java · BE CSE · Desktop application**

**Project author:** Yuvraj Singh Sidhu · UID 24BCS10816

SmartSplit is a Java-first desktop application for recording group expenses, allocating each cost among participants, calculating financial balances, and preparing transparent settlement suggestions. Version 3 builds on the working v2 codebase and focuses on a cleaner product experience, clearer financial feedback, safer local account handling, and better end-to-end usability without changing the application into a web/full-stack project.

## Release notes

See [`CHANGELOG_V3.md`](CHANGELOG_V3.md) for the v3 changes.

## What is new in v3

- A refreshed JavaFX design system with a split-screen sign-in/register experience, dark navigation rail, consistent buttons, elevated surfaces, styled tables, modern form controls, and a responsive scrollable content area.
- Improved dashboard with overview metrics, recent expense activity, group cards, and quick actions.
- Richer group workspace and participant pages.
- Local participant creation so a classroom demo does not require four separate sign-ins.
- A redesigned expense form with live split validation and a live per-participant allocation preview.
- Search and split-method filters in the expense ledger.
- Clearer balance summary, settlement history, and analytics charts.
- Consistent Indian currency formatting, including readable negative amounts such as `−₹1,000.00`.
- Password storage upgraded to salted PBKDF2-HMAC-SHA256. Existing v2 SHA-256 hashes are verified once and upgraded after successful login, so existing local accounts can continue to work.
- Additional automated tests for split rounding and password verification.
- Improved launch scripts for the Maven installation used during development.

## Main features

- Registration, sign-in, sign-out, and a one-click demo login.
- Multiple expense groups with descriptions.
- Registered accounts and local demo participants.
- Equal, exact-amount, and percentage-based expense splitting.
- `BigDecimal` financial arithmetic and cent-level remainder handling.
- SQLite persistence through JDBC.
- Expense categories, search, filters, and delete confirmation.
- Net balance calculation and greedy debtor-to-creditor settlement suggestions.
- Pending/completed settlement history with duplicate-pending protection.
- Dashboard summaries, category breakdown chart, member-balance chart, and report statistics.
- Confirmation when closing the application while signed in; normal OS window controls remain available.

## Technology stack

| Area | Technology | Role |
|---|---|---|
| Main implementation | Java 25 | Models, services, algorithms, validation and database logic |
| Desktop UI | JavaFX 21 | Screens, controls, charts and interactions |
| Persistence | SQLite | Local relational database |
| Connectivity | JDBC | SQL queries and transactions |
| Build | Apache Maven 3.10+ | Dependencies, compilation, testing and launching |
| Tests | JUnit 5 | Unit tests for split strategies and password utilities |

The application is deliberately a **Java desktop project**. It does not use React, a web server, Spring Boot, or a separate REST backend.

## Requirements

- JDK 25 or newer.
- Apache Maven 3.9+.
- Windows 10/11 is the current configured target (JavaFX dependencies use Windows classifiers).
- Internet access on the first Maven build so Maven can download its dependencies.

Verify the environment in a fresh terminal:

```powershell
java -version
javac -version
mvn -version
```

If Maven is installed at `C:\maven\apache-maven-3.10.0`, the supplied scripts will use it even if `mvn` is not on `PATH`.

## Run on Windows

**Recommended:** open the `SmartSplit_v3_Final` folder in VS Code, then double-click `run.bat` or execute:

```powershell
& "C:\maven\apache-maven-3.10.0\bin\mvn.cmd" clean test
& "C:\maven\apache-maven-3.10.0\bin\mvn.cmd" javafx:run
```

If Maven is already on `PATH`:

```powershell
mvn clean test
mvn javafx:run
```

To use the script from PowerShell:

```powershell
.\run.ps1
```

The launch script runs the test suite before opening the JavaFX app. For a quick direct launch, use the `javafx:run` command above.

## Demo account and recommended walkthrough

- **Username:** `demo`
- **Password:** `demo123`

The first launch initializes the SQLite schema and seeds the demo account plus three reusable demo users (`rahul`, `aman`, and `karan`). To prepare the demonstration, sign in and click **Demo Workspace** / **Load Demo Data**. The sample group is reused on later clicks, and sample expenses are added only if that demo group is empty.

Recommended demo flow:

1. Sign in using **Use Demo Account**.
2. Load the demo workspace.
3. Open the demo group and show its participants.
4. Open Expenses and demonstrate the Equal, Exact, and Percentage entries.
5. Add an expense manually and show the live allocation preview and validation.
6. Open Balances and generate the settlement plan.
7. Save pending settlement suggestions, mark one completed, and verify the balance changes.
8. Open Reports and explain the category chart, member balances, total, average and largest expense.
9. Sign out, then use the native window close button to close the application.

### Add participants without separate accounts

Open a group → **Members** → **Create Participant**. Enter a name and optionally a username. Leave the username blank to generate a unique local identifier. The participant is used for group expense allocation; they do not need to log in for the demo.

## Display scaling and window sizing

- The JavaFX window sizes itself to the primary monitor's usable desktop bounds on startup. This avoids clipping on laptops where Windows display scaling (for example, 125% or 150%) makes the usable logical desktop smaller than the physical screenshot dimensions.
- The window remains resizable and retains the normal native title bar controls. On a wide display it can open at the normal desktop size; on smaller displays it opens within the available area.

## Database and data safety

- SQLite is stored locally at `database/smartsplit.db`, relative to the project working directory.
- The database is initialized automatically when the application starts.
- Keep a backup before deleting or replacing the database.
- For a clean demonstration, use a fresh extracted v3 folder. Do not copy an older `smartsplit.db` into it unless you intentionally want to keep the older account/group history.
- Do not store real passwords or financial data in a demonstration database.

## Project structure

```text
src/main/java/com/smartsplit
├── Main.java            JavaFX application entry point and screen composition
├── model/               User, Group, Member, Expense, Balance and Settlement
├── strategy/            Equal / Exact / Percentage split strategies
├── service/              Authentication, expenses, groups, balances, settlements and reports
├── dao/                  JDBC data access objects
├── database/             SQLite connection and schema initialization
├── exception/            Domain and persistence exceptions
└── util/                 Validation, secure password hashing and currency formatting

src/main/resources/css/style.css   SmartSplit v3 visual design system
src/test/java/com/smartsplit/       JUnit tests
 database/schema.sql                Reference schema
 docs/                              Demo, report outline, checklist and viva notes
```

## Java concepts to explain in the viva

- Encapsulation through domain models and service boundaries.
- Abstraction and polymorphism through `SplitStrategy` and its implementations.
- Strategy design pattern for selecting the correct split algorithm.
- Lists and maps for participant, split, and balance processing.
- `BigDecimal` for monetary arithmetic and rounding control.
- Exception handling and validation for invalid amounts/allocations.
- JDBC, prepared statements, relational constraints and transactional expense persistence.
- SQLite persistence.
- JavaFX scenes, layouts, tables, charts, event handling and CSS.
- JUnit 5 for repeatable tests.
- PBKDF2-based password hashing and backward-compatible upgrade of old local password hashes.

## Final submission note

Run v3 on your own Windows setup and capture screenshots from the v3 interface before finalizing the report and presentation. Screenshots captured from v2 should not be labelled as v3 evidence.
