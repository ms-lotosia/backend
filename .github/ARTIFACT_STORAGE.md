# Artifact Storage Management

## Current Configuration

This workflow has been optimized to minimize artifact storage usage:

### Artifacts Upload Strategy
- **Test Results**: Only uploaded on failure, 1-day retention
- **Dependency Check Reports**: Only uploaded on failure, 1-day retention
- **JAR Files**: Use GitHub Actions cache instead of artifacts (no storage quota impact)

### Storage Optimization Features
1. **Conditional Uploads**: Artifacts only uploaded when tests/checks fail
2. **Short Retention**: 1-day retention for all artifacts
3. **Cache Instead of Artifacts**: JARs passed between jobs via cache (doesn't count toward artifact storage)
4. **Ignore Missing Files**: `if-no-files-found: ignore` prevents errors

## Monitoring Artifact Storage

### Check Current Usage
1. Go to your repository on GitHub
2. Navigate to: **Settings** → **Actions** → **General** → **Artifact and log retention**
3. Scroll to **Storage usage** section

### Delete Old Artifacts
If you hit the storage quota:

1. Go to: **Repository** → **Actions** → **Artifacts** (in left sidebar)
2. Review and delete old artifacts manually
3. Or use GitHub CLI:
   ```bash
   gh api repos/{owner}/{repo}/actions/artifacts --paginate | \
     jq -r '.artifacts[] | select(.expired == false) | .id' | \
     xargs -I {} gh api --method DELETE repos/{owner}/{repo}/actions/artifacts/{}
   ```

### Automatic Cleanup
- Artifacts expire automatically after retention period (1 day)
- Storage usage recalculates every 6-12 hours
- Canceled workflow runs have their artifacts deleted immediately

## Troubleshooting

### "Artifact storage quota has been hit" Error
1. **Immediate**: Manually delete old artifacts from the Actions tab
2. **Wait**: Storage recalculates every 6-12 hours
3. **Verify**: Check that retention-days is set to 1 (not 30+)
4. **Review**: Ensure artifacts only upload on failure (`if: failure()`)

### Best Practices
- Use `actions/cache` for passing data between jobs (doesn't count toward quota)
- Only upload artifacts for debugging (failures)
- Keep retention as short as possible (1-3 days max)
- Use `if-no-files-found: ignore` to prevent errors
- Monitor storage usage regularly

## References
- [GitHub Actions Billing](https://docs.github.com/en/billing/managing-billing-for-github-actions/about-billing-for-github-actions)
- [Managing Artifacts](https://docs.github.com/en/actions/using-workflows/storing-workflow-data-as-artifacts)
- [Caching Dependencies](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows)

