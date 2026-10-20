# Commit and push

Commit project changes, push to the remote branch, wait for GitHub Actions when configured, and verify production with a real page check (not a health endpoint alone).

## Preconditions

- User invoked **`/commit-push`** or explicitly asked to commit and push.
- Never commit secrets (`.env`, credentials, keys, PATs).
- Never force-push to `main`/`master`.
- Never skip hooks unless the user explicitly asked.
- Never amend unless user rules allow it.

## Step 1 — Inspect (run in parallel)

```bash
git status
git diff
git diff --cached
git log -5 --oneline
git branch -vv
```

Read the diff. Decide whether a release-history bump is needed.

## Step 2 — Version bump (`semantic-version` skill)

**Only when** `docs/release-history.json` exists in this repo.

**This is the only step where the agent may bump version.** Do not edit `docs/release-history.json` outside `/commit-push`.

Read and follow the **`semantic-version`** skill. If the change is user-visible (UI, API, behavior users notice) or a template deliverable (`AGENTS.md`, `docs/stack.md`, `.cursor/commands/`, new skills, etc.):

1. Bump `docs/release-history.json` (feeds `/sobre` and changelogs).
2. Include that file in the commit.

Skip the bump for internal-only refactors, tests, CI-only changes, or docs with no user impact.

## Step 3 — Commit message (`caveman-commit` skill)

Read and follow the **`caveman-commit`** skill.

- Conventional Commits, **English**
- Subject ≤50 chars when possible
- Body only when "why" is not obvious

## Step 4 — Commit

```bash
git add <relevant files>
git commit -m "<subject>" -m "<optional body>"
```

On Windows PowerShell, use a here-string for multi-line messages if needed.

If nothing to commit, say so and stop — do not push.

## Step 5 — Link release entry to commit

**After** the main commit, if `docs/release-history.json` was updated and the newest entry lacks `commit`:

1. `git rev-parse --short HEAD`
2. Set `commit` on the new entry
3. Commit the link:

```bash
git add docs/release-history.json
git commit -m "chore: link release entry to commit <sha>"
```

## Step 6 — Push

```bash
git push origin HEAD
```

If upstream is missing: `git push -u origin HEAD`.

**Push auth (Windows):** if HTTPS asks for a password and `gh` is unavailable, use PAT from `C:\repo\secrets\github\pat.txt` (line starting with `ghp_`).

## Step 6a — Build and publish the packages (repos that ship a runnable artifact)

**Skip** when the repo ships nothing runnable (libraries, templates, docs-only, skill packs). Otherwise pushing the code is only half the delivery: a version on GitHub with no downloadable package is a broken release, and the user will find out before you do.

1. **Use the project's own publish script first** if it has one: `scripts/publish.ps1`, `scripts/publish.sh`, or a `publish` entry in `package.json`. It knows the project's asset names and release notes. Run it and read its output; do not reimplement it by hand.
2. **If there is none, build and upload by the stack's own means**: `cargo tauri build`, `dotnet publish`, `pyinstaller`, `wails build`. Then attach every produced package to the release for the current version.
3. **Take the version from the project manifest** (`Cargo.toml`, `package.json`, `tauri.conf.json`), never from a tag hardcoded in a script. A hardcoded tag is how a project ends up at 0.3.0 in the manifest and 0.2.0 on GitHub.
4. **Upload under stable names, with no version in them** (`LE-Turbo-setup.exe`, `app.exe`). A version inside the asset name breaks the README link on every release.
5. **Verify the links yourself before reporting.** HTTP HEAD `https://github.com/<owner>/<repo>/releases/latest/download/<asset>` and confirm a real byte count. A link you did not check is not a delivery.

Then record the version entry with `semantic-version`, using the commit that shipped it.

## Step 6b — Sync skills (`agent-skills` repo only)

**When** this repo is `agent-skills` (`C:\repo\agent-skills`, remote `farukzahra/agent-skills`):

After a **successful push**, run **without asking**:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\repo\agent-skills\scripts\sync-faruk-skills.ps1"
```

This installs all published skills **globally** (`-g`) and in **every git repo** under `C:\repo`, and refreshes `~/.cursor/skills/` + `~/.agents/skills/` from `skills/*`.

- **Do not skip** after push in this repo.
- **Do not ask** for rollout confirmation — bulk sync is part of `/commit-push` here.
- If push was skipped (nothing to commit), skip this step too.

Report how many repos were synced in the final summary.

## Step 7 — GitHub Actions (when configured)

**Skip this step** if `.github/workflows/` does not exist.

Deploy runs on GitHub Actions — do not SSH to VPS or run local deploy scripts as part of `/commit-push`.

1. Resolve repo: `docs/commit-push.json` → `github.owner` + `github.repo`, else `git remote get-url origin`.
2. Poll the workflow run for the pushed commit until success or timeout (~10 min):

```powershell
& "C:\Program Files\GitHub CLI\gh.exe" run list --repo <owner>/<repo> --limit 5
& "C:\Program Files\GitHub CLI\gh.exe" run watch --repo <owner>/<repo> --exit-status
```

Without `gh`: GitHub API with PAT from `C:\repo\secrets\github\pat.txt`.

3. **If any required job fails:** read logs (`gh run view <id> --log-failed`), fix, commit, push, and repeat from Step 7. **Do not** report `/commit-push` done while Actions is red.

## Step 8 — Verify production (real check)

**Skip** when `docs/commit-push.json` has `"verify": null` or the repo has no production URL (libraries, templates, Android-only CI).

Read **`docs/commit-push.json`** first (canonical per repo). Fallback order: `AGENTS.md` → `docs/deploy-vps.md` → `README.md`.

### What counts as verification

- Fetch the **`verifyUrl`** (or `productionUrl`) with HTTP GET.
- Confirm **200** and that the page content matches **`expectInBody`** or **`expectTitle`** (product name, hero text, `<title>`, etc.).
- **Do not** treat `/api/health` or `{"ok":true}` alone as sufficient when a user-facing URL is configured.
- Optionally also hit a route mentioned in the release (e.g. `/sobre` after a version bump).

### Android-only (`verify.type`: `android-ci`)

No web URL. Confirm the **android-ci** (or named) workflow succeeded. Report Play Store / internal track notes from `AGENTS.md` if present.

### Loop on failure

If production check fails but Actions is green: investigate (cache, wrong branch, env), fix, push again, re-verify.

## Step 9 — Report to user

Always include:

- Commit SHA(s) and message(s)
- Branch pushed
- New version from `release-history.json` if bumped
- **GitHub Actions:** run URL + status (or "no workflows in repo")
- **Production:** the **`productionUrl`** from config — the URL you validated, what you checked on the page, and pass/fail
- **`agent-skills` only:** skills sync result (`sync-faruk-skills.ps1` — global + repo count)

## Failures

- Pre-commit hook failed → fix, **new commit** (never amend a failed hook unless user rules allow)
- Push rejected → report; do not force-push
- No remote → tell user to add `origin`
- Actions or production red → fix and re-push before closing
