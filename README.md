# mc-auth
This is a plugin for authentication in Spigot servers.

## Commands
- /login <password>
- /register <password> <confirmPassword>

# Under the hood
Passwords are hashed with `bcrypt` and stored in an SQLite database.
