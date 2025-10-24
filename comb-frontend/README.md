# Comb as You Are (Angular Frontend)

Single-page application for managing the Phorest tech test microservices. It provides:

- JWT-based login against the gateway (`/api/v1/auth/token`)
- Dashboard overview with total client count and loyalty leaderboard
- CSV import widgets for clients, appointments, services, and purchases

## Development setup

1. Ensure Node.js (>=18) is installed.
2. From `comb-frontend/` run:

   ```bash
   npm install
   npm start
   ```

   The app defaults to `http://localhost:4200` and proxies calls directly to the gateway (`http://localhost:8080/api/v1`).

## Environment configuration

Edit `src/environments/environment.ts` to point to the gateway base URL if it differs from `http://localhost:8080/api/v1`.

## Testing

Run the Angular test suite with:

```bash
npm test
```

Browser-based tests run using Karma in Chrome Headless by default.
