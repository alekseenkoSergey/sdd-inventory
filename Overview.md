# Inventory overview

The project is a **multi-user inventory tracking application** for personal or small-business use.
Each user signs in through Google SSO and receives a completely private workspace: inventory data, categories, storage locations, stock movements, and notifications are isolated per account.
Users can organize inventory into categories and locations, create and manage items, archive or restore them, and view key inventory metrics such as total items, low-stock items, out-of-stock items, and recent stock movements.

Inventory quantity is managed through an explicit **stock movement history** rather than by directly editing the current quantity.
Supported movements include opening balance, stock in, stock out, and positive or negative adjustments.
Creating an item with an initial quantity automatically creates an opening-balance movement, while subsequent changes must go through stock movements.
The system prevents operations that would make quantity negative and blocks new movements for archived items.
Users can search inventory by name, notes, or SKU and filter it by category, location, active/archived status, and stock state.
Each item exposes its current quantity, unit, low-stock threshold, category, location, status, and recent movement history.

The application provides **real-time low-stock and out-of-stock notifications**.
An active item is considered low stock when its quantity is above zero but at or below its configured threshold, and out of stock when its quantity reaches zero.
Alerts are reevaluated after stock movements and threshold changes and are delivered to the user while connected, without exposing notifications belonging to other accounts.
The subject area intentionally excludes collaboration and shared inventory: there are no team workspaces, shared warehouses, invitations, public links, or cross-user access.
Account linking between Google and GitHub is not required, barcode scanning is outside the scope, and stock-movement deletion is not required.
