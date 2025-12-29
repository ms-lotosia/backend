# GHCR Package Cleanup

The `deploy.yml` workflow now includes automatic cleanup of old GHCR package versions **at the beginning** of the workflow.

## How It Works

- **When**: Runs immediately after detecting changed services (before builds)
- **What**: Deletes old package versions, keeping only the latest **5 versions**
- **Why**: Frees up storage space before creating new images, preventing quota exhaustion

## Package Names

Your images are published as: `ghcr.io/<owner>/ms-lotosia/<service>`

The cleanup targets the full package name: `<owner>/ms-lotosia/<service>` for each service in your matrix.

## Authentication

The cleanup uses either:
1. `GHCR_DELETE_TOKEN` secret (recommended) - A PAT with `read:packages` and `delete:packages` scopes
2. `github.token` (fallback) - May work for personal accounts or repos with admin access to packages

## Configuration

### Option 1: Use PAT (Recommended)

1. Create a classic Personal Access Token with scopes:
   - `read:packages`
   - `delete:packages`

2. Add it as `GHCR_DELETE_TOKEN` in your repository secrets

### Option 2: Use GITHUB_TOKEN

If your packages are under a personal account and the repository has admin access, `github.token` may work without additional setup.

## Safety Features

- **Early Execution**: Runs before builds to free up storage space
- **Conservative Retention**: Keeps minimum 5 versions (adjustable)
- **Matrix Strategy**: Cleans up each service independently
- **Fail-fast: false**: Continues cleanup even if one service fails
- **Conditional**: Only runs when services are detected for building

## Customization

To modify retention policy, change `min-versions-to-keep` in the workflow:
```yaml
min-versions-to-keep: 20  # Adjust as needed
```

Consider your rollback window - if you need to rollback deployments, keep enough versions to cover your typical rollback timeframe.
