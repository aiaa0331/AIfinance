package com.aifinance.controller;

import com.aifinance.service.AgencyAgentService;
import com.aifinance.service.AgentCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/systems")
public class SystemsController {

    private final AgentCatalog agentCatalog;
    private final AgencyAgentService agencyAgentService;
    private final WebClient webClient;

    public SystemsController(AgentCatalog agentCatalog,
                              AgencyAgentService agencyAgentService,
                              WebClient.Builder webClientBuilder) {
        this.agentCatalog = agentCatalog;
        this.agencyAgentService = agencyAgentService;
        this.webClient = webClientBuilder.build();
    }

    @GetMapping
    public List<Map<String, Object>> getSystems() {
        return List.of(
            system("finance", "AI Finance Agents",
                "专业金融 Agent 系列，覆盖投资银行、股票研究、财富管理、私募股权全流程。基于 Anthropic financial-services 官方仓库。",
                "📊", "#1e40af", agentCatalog.findAll().size(),
                8380, "/finance.html", "Java · Spring Boot · SSE Streaming"),
            system("agency", "Agency Agents",
                "205 个通用职能 Agent，涵盖工程、营销、销售、设计、测试等 15 大类，含中国主流平台专属 Agent（小红书、抖音、知乎…）。",
                "🤖", "#7c3aed", agencyAgentService.count(),
                8380, "/agency.html", "Claude API · 15 Categories"),
            system("trading", "TradingAgents",
                "多智能体 LLM 量化交易框架。Bull/Bear 研究员辩论 + Risk Manager 仲裁 + Trader 执行，支持 Claude/GPT/Gemini/DeepSeek。",
                "📈", "#166534", 8,
                8060, "/trading.html", "Python · LangGraph · Multi-LLM"),
            system("devfleet", "Claude DevFleet",
                "自主编码 Agent 平台。Agent 在隔离 git worktree 中执行 Mission，支持子任务分发和依赖调度，基于 Claude Code SDK。",
                "⚡", "#b45309", null,
                3101, "/devfleet.html", "Python · FastAPI · React"),
            system("case", "Case Agent",
                "海关走私案件风险情报系统。持续采集 42 个直属海关行政处罚案件，结构化抽取 + 风险预测 + 自动报告。",
                "🔍", "#7e1d1d", null,
                8500, "/case.html", "Python · FastAPI · LLM"),
            system("hermes", "Hermes Agent",
                "Nous Research 出品的自改进 CLI Agent。内置学习闭环——从经验中创建技能、跨会话记忆、用户建模。支持 Telegram/Discord 网关、cron 调度、子 Agent 并行。自定义插件：管制清单 + 价格对比。",
                "☤", "#4c1d95", null,
                9119, "/hermes.html", "Python · FastAPI · React Web UI · v0.10.0")
        );
    }

    private Map<String, Object> system(String id, String name, String desc,
                                        String icon, String color, Integer agentCount,
                                        int port, String url, String tech) {
        return Map.of(
            "id", id,
            "name", name,
            "description", desc,
            "icon", icon,
            "color", color,
            "agentCount", agentCount != null ? agentCount : -1,
            "port", port,
            "url", url,
            "tech", tech
        );
    }

    @GetMapping("/status")
    public Map<String, Boolean> getStatus() {
        return Map.of(
            "finance",  checkPort(8380),
            "agency",   checkPort(8380),
            "trading",  checkPort(8060),
            "devfleet", checkPort(3101),
            "case",     checkPort(8500),
            "hermes",   checkPort(9119)
        );
    }

    private boolean checkPort(int port) {
        try {
            webClient.get()
                    .uri("http://localhost:" + port)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(800))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
