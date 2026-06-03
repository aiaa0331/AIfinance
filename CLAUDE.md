# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

Spring Boot 3.2 + Maven web application — an AI Agents portal running on port **8380**. Java 17.

## Build & Run

```bash
mvn package -DskipTests          # build
java -jar target/aifinance-0.0.1-SNAPSHOT.jar   # run on :8380
```

Set `ANTHROPIC_API_KEY` env var before running to enable chat by default, or supply the key in the UI.

## Architecture

**Backend** (`src/main/java/com/aifinance/`):
- `service/AgentCatalog` — 10 hardcoded Anthropic financial-services agents
- `service/AgencyAgentService` — parses `~/IdeaProjects/agency-agents/**/*.md` frontmatter at startup (~184 agents)
- `service/AnthropicService` — streams Claude API responses via WebFlux SSE
- `controller/AgentController` — `/api/agents`, `POST /api/chat` (SSE)
- `controller/AgencyAgentController` — `/api/agency/agents`, `POST /api/agency/chat` (SSE)
- `controller/SystemsController` — `/api/systems`, `/api/systems/status` (live port checks)

**Frontend** (`src/main/resources/static/`):
- `index.html` — portal hub: 5 system cards with live status
- `finance.html` — 10 finance agents + streaming chat
- `agency.html` — 184 agency agents, search + category filter + streaming chat
- `trading.html` — TradingAgents showcase (links to :8060)
- `devfleet.html` — DevFleet (:3101/:18801) + Case Agent (:8500) showcase

## Key Config

`application.properties`:
- `server.port=8380`
- `agency.agents.path=${user.home}/IdeaProjects/agency-agents`
- `anthropic.api-key=${ANTHROPIC_API_KEY:}`

## External Systems Referenced

| System | Port | Path |
|--------|------|------|
| TradingAgents | 8060 | `~/IdeaProjects/TradingAgents` |
| DevFleet UI | 3101 | `~/IdeaProjects/claude-devfleet` |
| DevFleet API | 18801 | same |
| Case Agent | 8500 | `~/IdeaProjects/case-agent` |
| Agency Agents | — | `~/IdeaProjects/agency-agents` |

<!-- AUTO-LEARNINGS:START -->
## Session Learnings

### Commands
- curl -s http://localhost:9999/api/projects 2>/dev/null | python3 -c "import sys,json; data=json.load(sys.stdin); print([p.get('port') for p in data])"
- curl -s http://localhost:8380/api/agents | python3 -c "import sys,json; a=json.load(sys.stdin); [print(f'{x[\"id\"]}: {x[\"vertical\"]}') for x in a]"
- curl -s http://localhost:8380/ | head -20
- curl -s http://localhost:8380/api/agency/stats | python3 -m json.tool
- curl -s http://localhost:9999/api/projects 2>/dev/null | python3 -c "

### Key Files
- `CLAUDE.md`
- `pom.xml`
- `src/main/java/com/aifinance/AifinanceApplication.java`
- `src/main/java/com/aifinance/model/AgentDefinition.java`
- `src/main/java/com/aifinance/model/ChatRequest.java`
- `src/main/java/com/aifinance/service/AgentCatalog.java`
- `src/main/java/com/aifinance/service/AnthropicService.java`
- `src/main/java/com/aifinance/controller/AgentController.java`
- `src/main/resources/application.properties`
- `src/main/resources/static/index.html`

<!-- AUTO-LEARNINGS:END -->
