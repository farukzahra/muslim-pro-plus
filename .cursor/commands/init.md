# Init — bootstrap agent workflow

User invoked **`/init`**. Read and follow the **`project-init`** skill (`skills/project-init/SKILL.md` or `~/.cursor/skills/project-init/SKILL.md`).

After skills install, ensure **`automate-before-manual`** (`.agents/skills/`), **`dont-be-lazy`**, and `.cursor/rules/automate-before-manual.mdc` exist. Parent sync: `../agent-skills/scripts/sync-agent-conventions.ps1`.

Do not run bootstrap without explicit user request. Do not commit or push.
