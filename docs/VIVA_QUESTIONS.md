# SmartSplit v3 — Viva Quick Preparation

1. **Why is this a Java project rather than a full-stack project?** The domain models, split algorithms, balance engine, settlement logic, validation, services and JDBC persistence are implemented in Java. JavaFX is the desktop presentation layer.
2. **Why JavaFX?** It provides Java-based controls, event handling, tables, charts and CSS styling for a desktop GUI.
3. **Why SQLite?** It is a lightweight, file-based relational database suitable for a local desktop application.
4. **What is JDBC?** Java Database Connectivity, used to execute SQL and access database records from Java.
5. **Why use `BigDecimal` for money?** It avoids binary floating-point representation errors and supports explicit rounding.
6. **Where is abstraction used?** `SplitStrategy` defines the common contract for expense allocation.
7. **Where is polymorphism used?** Equal, Exact and Percentage strategies can be invoked through the same `SplitStrategy` interface.
8. **What is the Strategy design pattern?** It encapsulates interchangeable algorithms behind a common interface, allowing the selected split method to vary without placing every algorithm in the UI controller.
9. **What is DAO?** Data Access Object; it separates JDBC and SQL persistence code from business logic.
10. **How is net balance calculated?** Total paid − total owed + the effect of completed settlements. Positive means receivable; negative means payable.
11. **How are settlements generated?** Positive balances are creditors, negative balances are debtors, and the algorithm greedily matches outstanding amounts.
12. **Why use `PreparedStatement`?** It separates SQL syntax from input values and reduces SQL-injection risk.
13. **How does exact splitting validate input?** Every participant must have a non-negative value and their sum must equal the expense total.
14. **How does percentage splitting validate input?** Percentages must total exactly 100% before monetary shares are stored.
15. **How is equal-split rounding handled?** Shares are rounded to cents and the final participant receives the remainder so the sum stays equal to the original expense.
16. **Why are local demo participants supported?** They allow the project to demonstrate group allocation without registering and signing into multiple accounts.
17. **How are passwords stored?** PBKDF2-HMAC-SHA256 with a unique salt is used for new passwords. Legacy v2 SHA-256 hashes are verified and upgraded after a successful sign-in.
18. **What is stored in SQLite?** Users, groups, membership, categories, expenses, each participant's expense share, and settlement records.
19. **What is tested automatically?** Split totals, rounding, invalid split values, and password verification/legacy hash compatibility.
20. **What should be checked manually?** Full JavaFX workflows, expense entry, database persistence, settlement completion and report totals.
21. **Why do services and DAOs have separate roles?** Services enforce business rules; DAOs isolate database queries and persistence operations.
22. **What is the limitation?** The application is local-first and records settlement status; it does not make real bank or UPI transfers.
