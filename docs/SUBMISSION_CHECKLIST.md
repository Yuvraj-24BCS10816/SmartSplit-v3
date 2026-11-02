# SmartSplit v3 — Submission Checklist

## Environment and build
- [ ] Install/use JDK 25 and Maven 3.9+.
- [ ] Run `mvn clean test` successfully.
- [ ] Launch with `mvn javafx:run` or `run.bat`.
- [ ] Verify that the standard Windows title bar and close controls are available.

## Application workflows
- [ ] Demo login and registration.
- [ ] Sign out and closing confirmation.
- [ ] Create, open and delete a group (use test data only for delete testing).
- [ ] Create a local participant; add/remove a member when safe.
- [ ] Equal split, including an amount that produces a rounding remainder.
- [ ] Exact split with valid and invalid totals.
- [ ] Percentage split with 100%, then with an invalid total.
- [ ] Search/filter the expense ledger and delete a test expense.
- [ ] Balance calculation and settlement suggestion generation.
- [ ] Save and complete a settlement; verify the balance is updated.
- [ ] Dashboard and report calculations.
- [ ] Restart app and confirm SQLite data persistence.

## Report and presentation
- [ ] Use screenshots captured from **SmartSplit v3**, not older v2 screens.
- [ ] Confirm all captions match the actual screenshot and application behavior.
- [ ] Replace any student/faculty/signature placeholders required by the university format.
- [ ] Verify references, page numbering and table of contents after final edits.
- [ ] Practice explaining `SplitStrategy`, `ExpenseService`, `BalanceService`, `SettlementService`, DAOs, JDBC and PBKDF2 password hashing.

## Release hygiene
- [ ] Back up any existing `database/smartsplit.db` before changing versions.
- [ ] Use a fresh v3 extraction for the final demo, or intentionally migrate only the database you want to preserve.
- [ ] Do not include real passwords or sensitive financial records in the submitted ZIP.
