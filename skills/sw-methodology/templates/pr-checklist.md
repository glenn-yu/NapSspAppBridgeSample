# PR Review Checklist

Before creating PR
- [ ] Branch name follows convention (feature/bugfix/hotfix/*)
- [ ] Describe change in title and body; include linked issue(s)
- [ ] Update CHANGELOG.md draft if user-facing
- [ ] Run linters and unit tests locally

What reviewer checks
- [ ] CI passes for all required pipelines
- [ ] Code reads well; complex logic has comments or tests
- [ ] No secrets or credentials in diffs
- [ ] Performance considerations covered for hot paths
- [ ] Tests added/updated for regressions
- [ ] Documentation updated (README/docs) if behavior changes

Merging
- [ ] At least one approving review (two for critical changes)
- [ ] No unresolved review comments
- [ ] Tag or milestone set when merging for release

Post-merge
- [ ] Close related issues or link PR in issue
- [ ] Monitor dashboards/alerts if production-impacting
