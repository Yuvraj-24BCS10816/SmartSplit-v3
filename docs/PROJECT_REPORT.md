# SmartSplit v3 — Project Report Outline

Use this outline to update the university-format report. Replace all older runtime screenshots with images captured from the final v3 build before submission; do not label v2 screenshots as v3 evidence.

## 1. Abstract
SmartSplit v3 is a Java-first desktop application for group expense management and settlement tracking. It provides group and participant management, expense entry, equal/exact/percentage allocations, net balances, settlement suggestions, and reports. JavaFX provides the interface, Java services implement business rules, JDBC connects to SQLite, and JUnit tests exercise key financial calculations. Version 3 improves the JavaFX visual design, form feedback, ledger filtering, password storage and test coverage while retaining the local desktop architecture.

## 2. Problem statement and objectives
Explain the problems caused by fragmented records and manual calculations. State objectives: reliable expense entry, allocation validation, net balance calculation, settlement suggestions, audit-friendly history, local persistence and demonstration of Java OOP.

## 3. Technology and architecture
- Java 25: models, services, strategies, validation, security utility and algorithms.
- JavaFX 21: desktop UI, tables, charts, dialogs, input previews and CSS.
- JDBC and SQLite: persistence, foreign keys, prepared statements and transactional writes.
- Maven and JUnit 5: dependency management and repeatable tests.

Architecture: JavaFX interface → Service layer → DAO layer/JDBC → SQLite. The `SplitStrategy` interface provides equal, exact and percentage strategies.

## 4. Core algorithms
Document equal allocation with rounding remainder, exact allocation with sum validation, percentage allocation with 100% validation, `net = paid − owed + completed settlement effect`, and greedy debtor/creditor matching.

## 5. Version 3 user experience
Describe the split-screen authentication pages, dark navigation rail, dashboard summary and recent activity, group cards, member management, live split-allocation feedback, searchable expense ledger, balance summaries, settlement history and report charts.

## 6. Security and data integrity
New passwords use PBKDF2-HMAC-SHA256 with a unique salt. Existing v2 unsalted SHA-256 hashes are verified and upgraded after a successful login. Prepared statements and database constraints protect normal persistence operations. Local participants are separate demo identities without an interactive login workflow.

## 7. Testing
The unit-test suite covers equal splitting and remainder handling, exact split validation, percentage calculation/validation including very small totals, password hash verification with legacy compatibility, and registration-field validation. Run the Maven suite on Windows and include its actual output and manual test evidence in the final submission.

## 8. Screenshots
Capture these from v3: Login, Register, Dashboard, My Groups, Group Overview, Members, Add Expense with live allocation preview, Expenses search/filter, Balances, Settlement Suggestions, Settlements, Reports. Ensure the visuals and captions refer to v3.

## 9. Limitations and future work
The current application uses local SQLite storage and does not transfer funds. Potential future work includes CSV/PDF export, stronger account recovery, multi-device synchronization, receipts, and mobile access.
