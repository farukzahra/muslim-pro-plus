# AGENTS.md — muslim-pro-plus

Projeto de análise e patch do APK Muslim Pro (12.6.10), rodando em emulador
Android local. Não é um app próprio: é ferramentaria + artefatos de engenharia
reversa.

## Estrutura

| Pasta | Conteúdo | Versionado |
|---|---|---|
| `apk/` | APK original baixado | não |
| `decompiled/` | fonte Java do JADX | não |
| `apktool_out/` | decode do apktool | não |
| `build/` | dex/arsc extraídos, APK patchado | não |
| `tools/` | scripts de patch (`*.py`) | sim |
| `evidence/` | evidências, dumps e manifest decodificado | json/xml sim, png não |
| `docs/superpowers/` | specs e planos | sim |

## Ferramentas

- Python 3.13 (`tools/*.py`)
- JADX 1.5.6 em `tools/jadx/`
- `jadx-headless-mcp-0.7.1-all.jar` (provider do REA)
- apktool 2.11.1 (`tools/apktool_2.11.1.jar`) — **não** remonta este APK
- Android SDK: `adb`, `zipalign`, `apksigner` (build-tools 36.1.0)
- Emulador: AVD `Pixel6`, Android 14 (`emulator-5554`)

`JAVA_HOME` padrão: `C:\Program Files\Android\Android Studio\jbr` (JDK 21).

## Pipeline de patch

1. Extrair `resources.arsc` e `classes*.dex` do APK.
2. `tools/patch_arsc.py` para strings; `tools/dex_patch.py` para bytecode.
3. `tools/repack.py` troca as entradas mantendo a compressão.
4. `zipalign -p -f 4` e `apksigner sign` (debug keystore).
5. `adb uninstall` + `adb install` (assinatura muda).
6. Verificar com `adb shell dumpsys activity top` (hierarquia de views) e
   `adb logcat -b crash`.

## Regras do repo

- Tarefa pendente se executa, não se pergunta: fazer, verificar com evidência e
  reportar o que sobrou. Perguntar só em decisão irreversível.
- Nunca commitar `apk/`, `decompiled/`, `apktool_out/`, `build/` nem `.jar`.
  O código é proprietário da Bitsmedia; o repo guarda só a ferramentaria.
- Todo patch precisa de evidência: log, dump de view ou screenshot em
  `evidence/` antes de dizer que funcionou.
- Testar sempre no `Pixel6` antes de empacotar.
- Assinatura é debug: atualização pela Play Store exige desinstalar.

## Agent workflow (mandatory skills)

| Phase | Skill / command | Rule |
|-------|-----------------|------|
| Design | `brainstorming` | No feature code until spec approved → `docs/superpowers/specs/` |
| Plan | `writing-plans` | `docs/superpowers/plans/YYYY-MM-DD-*.md` |
| Recurring guardrails | `dont-forget` | Prefer CI/hooks/codegen over "remember to…" |
| Automation | `automate-before-manual` | `../secrets/` before manual steps |
| Share links | `validate-before-share` | HTTP 200 + content before sending URL |
| Local test | `finish-with-dev-server` | Documented ports; conflict → kill own / next port |
| Architecture | `ask-before-architecture` | Ask before stack decisions |
| Diagrams | `diagrams-mermaid` | Mermaid + validate before show |
| UI / bugs | `ui-change-e2e` | E2E on screen change or bugfix |
| Build | stack skills + `tdd` | Per project stack |
| Verify | `verification-before-completion` | Evidence before "done" |
| Debug | `systematic-debugging` | Root cause before fix |
| Ship | `/commit-push` | `semantic-version` → `caveman-commit` → push → Actions → prod URL |
| Delivery recap | `/recap` + `recap` | HTML "pedido vs entregue" in `docs/session-recaps/` |
| Next-agent handoff | `/handoff` + `handoff` | Compact doc in OS temp dir for a fresh session |

**Gates:** no feature without approved spec; no "done" without verification;
version bump only on `/commit-push`; obligations that must not be forgotten →
automate (`dont-forget`).

## Idioma

Responder em português do Brasil.

Invoke `/init` to (re)bootstrap skills and folders.
