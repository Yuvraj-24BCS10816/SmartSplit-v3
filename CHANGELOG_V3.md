# SmartSplit v3 — Change Log

## v3.0.0

### Experience and visual design
- Replaced the earlier plain JavaFX skin with a cohesive modern design system: split-screen authentication, branded navigation rail, contemporary typography, refined tables, consistent buttons, elevated panels, accessible empty states and styled dialogs.
- Added a richer dashboard with group spending totals, outstanding amounts, recent activity and quick actions.
- Refined groups, participants, expense ledger, balances, settlements and reports screens.
- Added live allocation preview and immediate validation to the expense-entry dialog.
- Added expense search and split-method filtering.
- Preserved the native desktop title bar, minimize/maximize controls, sign-out action and close confirmation.

### Correctness and data integrity
- Changed percentage allocation rounding to round early shares down and assign the cent remainder to the final participant, preventing a negative final share on very small expenses while preserving the total.
- Improved currency formatting for negative balances.
- Restricted group deletion to the account that created the group; other members see a disabled action with an explanation.
- Added participant-username validation, clearer expense-deletion warnings, and duplicate pending-settlement protection.
- Migrated new password storage to salted PBKDF2-HMAC-SHA256 while preserving successful sign-in for v2 SHA-256 hashes and upgrading those hashes after login.

### Quality and maintainability
- Reformatted important DAO and settlement-service classes for readability.
- Added tests for small-value percentage rounding, password-hash verification and registration input validation.
- Updated README, demo script, submission checklist, viva material and project-report outline for v3.

## Final compile correction

- Fixed an unresolved `spacer` reference in the split-screen authentication layout by creating the intended growable JavaFX region before adding it to the hero layout.

## Display scaling / cropped-edge correction
- Made the initial window size responsive to the primary monitor's usable desktop bounds, including Windows display-scaling configurations.
- Positioned the initial window within the visual desktop area instead of relying on a fixed 1360 × 850 window that may extend beyond smaller scaled desktops.
- Removed the fixed minimum scene dimensions that could override the adaptive stage size and crop the top/left/right edges.
- Reduced the dashboard's preferred two-column widths so the groups and quick-actions cards fit more comfortably on scaled laptop displays.


## Final personalization

- Added the author credit **“Made by Yuvraj Singh Sidhu, 24BCS10816”** to the sign-in/register hero footer and the persistent application sidebar footer.
- Updated the README with the project author information.
