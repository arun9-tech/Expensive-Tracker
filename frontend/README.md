# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) (or [oxc](https://oxc.rs) when used in [rolldown-vite](https://vite.dev/guide/rolldown)) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.

## Common Errors

### 404 (Not Found)

**Meaning in Simple Words:**
Your webpage is trying to load a file (like CSS, JS, or image), but the file does not exist at that path or cannot be found by the server.

**Example:**
You wrote `<link rel="stylesheet" href="/style.css">` but the real file is in `./assets/style.css`.

**Fix / Solution:**
Verify all file paths within your `index.html`, `script.js`, and `styles.css` files. Ensure you are using correct relative paths. For example:
- `<link rel="stylesheet" href="styles.css">`
- `<script src="script.js"></script>`

### CSP Violation (Content Security Policy)

**Error Message Example:**
`Connecting to 'http://127.0.0.1:5500/.well-known/appspecific/com.chrome.devtools.json' violates the following Content Security Policy directive: "default-src 'none'". The request has been blocked.`

**Meaning in Simple Words:**
Your site (or local development server like Live Server) has a Content Security Policy that blocks certain external connections by default. Chrome DevTools often tries to connect to `chrome-devtools://` or `.well-known` paths, but a strict policy might disallow it.

**Fix / Solution:**
*   **During local development:** This specific message is often harmless and comes from Chrome DevTools. You can generally ignore it as it usually doesn't break your site's functionality.
*   **If you need to explicitly allow it (for debugging or specific scenarios):** You can loosen the rule in your `index.html` by adding a `<meta>` tag. For example:
    ```html
    <meta http-equiv="Content-Security-Policy" content="default-src 'self'; connect-src 'self' http://127.0.0.1:5500 http://localhost:8080 chrome-devtools://*; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';">
    ```
    **Note:** Be cautious with overly permissive CSP rules like `*` in production environments as they can introduce security risks. Adjust `connect-src` to only include necessary domains.
