# Software Development Methodology Skill

This AgentSkill scaffolds project lifecycle documents and checklists for software projects.

Features
- Create RFC / requirement documents
- Generate planning, development, testing, deployment, and retrospective templates
- Provide code comment guidelines, bug-tracking policy, PR/review checklist, CI/testing suggestions
- Support dry-run, commit, and PR creation modes

Usage
- Run the skill with a project name and mode: `create-project --name "My Project" --mode dry-run|commit|pr`
- Options:
  - --name: project name (required)
  - --owner: owner/lead
  - --mode: dry-run (create files locally), commit (commit files to a feature branch), pr (create branch+PR)
  - --approve: when present, auto-approve publish steps (use with care)

Outputs
- docs/rfcs/<project>-requirements.md
- docs/process/<project>-plan.md
- docs/process/<project>-test-plan.md
- docs/process/<project>-release-checklist.md
- docs/process/<project>-retrospective.md

Safety
- Default is dry-run. commit/pr require explicit mode flags.
- Major changes (like LFS history rewrite) will always require manual approval.
