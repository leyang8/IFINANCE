# IFINANCE

This is a Java GUI individual project for a financial book-keeping application.

## Features

- Utilizes JavaDB for data storage.
- Implements a user-friendly GUI interface.
- Differentiates functionality based on user roles.
- Applying tree-like data structure
- Default admin account: 
  - Username: "admin"
  - Password: "admin"
- Example Non-Admin Account:
  - Username: "a"
  - Password: "a"

## Login Layer Functionality

- Authorization based on user roles.
- Admin-Users:
  - View and manage all registered non-admin users.
  - Add, modify, or delete non-admin users.
- Non-Admin Users:
  - Change their own password (updates in the database).
  - Authorized to use all other layer functions of the system.

## Manage Account Layer Functionality

- Non-admin users can customize their account groups.
- Account groups are displayed in a tree-like view.
- Default groups provided.
- Non-admin users can add, modify, or delete groups by right-clicking.
- Upper layer groups cannot be removed if child groups exist.




