# SmartSplit v3 — Demonstration Script

## Before the demo

1. Run `mvn clean test` (or `run.bat`) and confirm the build succeeds.
2. Start the app and choose **Use Demo Account** (`demo / demo123`).
3. Click **Demo Workspace**. The existing sample workspace is reused on repeated clicks.

## Live demonstration sequence

1. **Dashboard:** explain group count, group spending, what the signed-in user owes/should receive, and recent activity.
2. **Groups:** open the prepared demo group and show its participants, expense count, and total.
3. **Members:** create a local participant to demonstrate that extra login accounts are not required; optionally add the three seeded demo members.
4. **Expenses:** inspect the seeded Equal, Exact, and Percentage examples.
5. **Add Expense:** enter a description and amount. Change split method and show the live per-person allocation preview. For Exact, use values that sum to the total; for Percentage, make the percentages sum to 100%.
6. **Balances:** explain that positive net balances mean a participant should receive money and negative balances mean they owe money.
7. **Settlements:** save current suggestions as pending, select a pending row, and mark it completed. Reopen Balances to show the updated net balance.
8. **Reports:** explain total spending, average, largest expense, category pie chart and participant-balance chart.
9. **Sign out / close:** use Sign Out, then use the standard Windows close control. Explain that data persists in SQLite.

## Suggested manual test cases

- Invalid or zero amount is rejected.
- Exact allocations must sum to the expense total.
- Percentage allocations must sum to 100%.
- Equal split preserves the total when rounding is required.
- Search filters by description or payer; split-method filter works independently.
- Repeated loading of demo data does not keep adding demo expenses.
- A duplicate pending settlement is not saved twice.
- A completed settlement changes net balances.
- Data persists when the application restarts.
