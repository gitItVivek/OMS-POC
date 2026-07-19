# docs-agent-service

Spring AI (OpenAI) agent that scans OMS `*Controller.java` files and regenerates Markdown API docs under `docs/api/`.

## Setup

1. Copy `.env.example` to `.env` and set `OPENAI_API_KEY` (OpenAI account must have available quota).
2. From repo root (or this module), run a one-shot generation:

If OpenAI returns `429` / quota errors, the agent **falls back** to deterministic inventory-based Markdown under `docs/api/` so the pipeline still produces docs.

```bash
# PowerShell (repo root)
$env:OPENAI_API_KEY = (Get-Content docs-agent-service\.env | Where-Object { $_ -match '^OPENAI_API_KEY=' }) -replace '^OPENAI_API_KEY=',''
mvn -pl docs-agent-service -am spring-boot:run "-Dspring-boot.run.arguments=--docs.agent.run-on-startup=true --docs.agent.repo-root=. --docs.agent.exit-after-run=true"
```

Or start the server and call:

```bash
curl -X POST http://localhost:8099/api/docs/generate
```

Inventory-only (no LLM):

```bash
curl http://localhost:8099/api/docs/inventory
```

## MCP

With the app running, Streamable-HTTP MCP is enabled via `spring-ai-starter-mcp-server-webmvc`.
Tools are the same inventory/read/write helpers used by the ChatClient agent.

## GitHub Actions

On push to `master`, `.github/workflows/update-api-docs.yml` builds this module, runs one-shot generation, and commits `docs/api` with `[skip ci]`.

**Required secret:** repository Settings → Secrets and variables → Actions → `OPENAI_API_KEY`.

**Required permission:** workflow already sets `contents: write`.
