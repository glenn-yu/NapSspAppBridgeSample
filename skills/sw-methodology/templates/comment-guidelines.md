# Code Comment Guidelines

Purpose
- Ensure comments explain the *why*, not the *what* (code should show what it does).

Rules
- Explain intent: describe the reason behind non-obvious decisions.
- Short examples: show expected input/output for tricky functions.
- TODO/TFIX format: use `TODO(username): reason` or `TFIX(username): reason` for temporary fixes.
- Link to tickets: when relevant, include a short link to the issue/PR (e.g. `ISSUE-123`).
- Keep comments up to date: remove or update comments when code changes.

Examples
- Good: `// Use exponential backoff because server returns 429 under load (ISSUE-456)`
- Bad: `// increment i` (obvious from code)

Reviewers
- During PR review, validate that comments add value and are accurate.
