# Bug Tracking & Priority Policy

Purpose
- Standardize how bugs are reported, prioritized, and fixed.

Fields for bug report
- Title
- Steps to reproduce (minimal, reproducible)
- Expected behavior
- Actual behavior
- Environment (OS, device, versions)
- Logs / stack trace / screenshots
- Severity & Priority suggestion

Severity levels
- Sev 1 (Critical): Production outage, data loss, major revenue impact — requires immediate hotfix.
- Sev 2 (High): Core functionality broken for a subset of users — schedule in next patch.
- Sev 3 (Medium): Non-critical feature broken or degraded — backlog/next sprint.
- Sev 4 (Low): Minor UI/typo/quality-of-life issues.

Priority mapping
- P0: Fix now (hotfix) — Sev1 usually maps to P0.
- P1: High priority — next sprint/patch.
- P2: Normal — backlog.
- P3: Low — grooming.

Workflow
1. Create bug issue with template.
2. Triage: assign severity/priority and owner (triage within 24h).
3. Reproduce and attach minimal reproduction steps.
4. Patch -> PR -> Review -> Merge -> Deploy.
5. Postmortem for Sev1 incidents.

Labels
- Use labels like `bug/sev1`, `bug/sev2`, `bug/needs-triage`, `bug/confirmed`.
