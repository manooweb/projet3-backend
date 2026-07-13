# ChâTop API Bruno collection

This collection targets the local Spring Boot backend on `http://localhost:9001`.

Recommended manual order:

1. Run `Auth / Login` with the demo user from the imported SQL script.
2. Run `Auth / Me` to save `userId`.
3. Run the rentals, message and user requests.

The backend stores the JWT in the `CHATOP_AUTH` HTTP-only cookie. The collection
pre-request script initializes the `XSRF-TOKEN` cookie when needed and copies it
into the `X-XSRF-TOKEN` header for unsafe methods.

`Auth / Register` is included for the API contract. Change `registerEmail` in
the `Local` environment before re-running it if the email already exists.

Core contract requests:

- `Auth / Register`
- `Auth / Login`
- `Auth / Me`
- `Rentals / List Rentals`
- `Rentals / Get Rental`
- `Rentals / Create Rental`
- `Rentals / Update Rental`
- `Messages / Send Message`
- `Users / Get User`

Helpers:

- `Auth / Logout`, useful for clearing the auth cookie
