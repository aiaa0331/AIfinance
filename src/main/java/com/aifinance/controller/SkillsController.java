package com.aifinance.controller;

import com.aifinance.model.SkillInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/skills")
public class SkillsController {

    private static final Path SKILLS_DIR = Path.of(System.getProperty("user.home"), ".claude", "skills");

    private static final Map<String, String> ZH = Map.ofEntries(
        Map.entry("agent-harness-construction",   "设计并优化 AI Agent 的动作空间、工具定义与观测格式，提升 Agent 任务完成率。"),
        Map.entry("agentic-engineering",           "评估优先、任务分解、成本感知模型路由的 Agentic 工程实践方法论。"),
        Map.entry("ai-first-engineering",          "AI 代码比例高的团队工程模式——任务设计、评估体系与质量门禁整套框架。"),
        Map.entry("ai-regression-testing",         "AI 辅助开发的回归测试策略：沙箱 API 测试、无数据库依赖、自动化 Bug 检测工作流。"),
        Map.entry("api-design",                    "REST API 设计规范：资源命名、状态码、分页、过滤、错误响应、版本管理与限流。"),
        Map.entry("autonomous-loops",              "自主 Claude Code 循环的架构模式——从简单顺序流水线到带反馈控制的复杂循环。"),
        Map.entry("backend-patterns",              "后端架构模式：API 设计、数据库优化、服务端最佳实践，生产级可扩展系统参考。"),
        Map.entry("blueprint",                     "系统设计蓝图模板，用于规划复杂功能或架构方案的结构化文档。"),
        Map.entry("brainstorming",                 "头脑风暴引导技术，系统化探索创意与解决方案空间。"),
        Map.entry("canvas-design",                 "使用设计哲学在 .png / .pdf 中创作精美视觉作品的完整工作流。"),
        Map.entry("claude-api",                    "Anthropic Claude API 最佳实践（Python/TypeScript）：Messages API、流式输出、工具调用、Prompt Caching。"),
        Map.entry("coding-standards",              "TypeScript/JavaScript/React/Node.js 通用编码规范、最佳实践与设计模式。"),
        Map.entry("configure-ecc",                 "Everything Claude Code 交互式安装向导，帮助选择并安装合适的 Skill 集合。"),
        Map.entry("content-hash-cache-pattern",    "用 SHA-256 内容哈希缓存文件处理结果——路径无关、自动失效、无需外部状态。"),
        Map.entry("context-budget",                "审计 Claude Code 上下文窗口消耗，跨 Agent/Skill/MCP/Rules 识别浪费并给出优化建议。"),
        Map.entry("continuous-agent-loop",         "带质量门禁、评估与恢复控制的持续自主 Agent 循环架构模式。"),
        Map.entry("continuous-learning-v2",        "基于 Instinct 的学习系统：通过 Hooks 观察会话，创建带置信度的原子化经验并持续改进。"),
        Map.entry("cost-aware-llm-pipeline",       "LLM API 成本优化：按任务复杂度路由模型、预算追踪、缓存策略与批处理。"),
        Map.entry("customs-trade-compliance",      "海关文件、关税分类、HS 分类逻辑、Incoterms 应用、FTA 利用与多司法管辖区合规专业知识。"),
        Map.entry("data-scraper-agent",            "构建全自动 AI 数据采集 Agent，支持任意公开数据源（招聘板、价格、评论等）。"),
        Map.entry("database-migrations",           "数据库迁移最佳实践：Schema 变更、数据迁移、回滚策略与零停机部署。"),
        Map.entry("ddd",                           "查看桌面最新截图，用于视觉验证和 UI 效果检查（运行 /ddd 触发）。"),
        Map.entry("deep-research",                 "多源深度调研（Firecrawl + Exa MCP）：网络搜索、综合结论、生成带引用的研究报告。"),
        Map.entry("deployment-patterns",           "部署流程与 CI/CD 流水线：Docker 容器化、健康检查、回滚策略与蓝绿发布。"),
        Map.entry("discussion-archive",            "归档和结构化存储对话与技术讨论内容，便于后续检索。"),
        Map.entry("dispatching-parallel-agents",   "将 2+ 个独立任务并行分发给多个 Sub-Agent 同时执行，无共享状态依赖。"),
        Map.entry("docker-patterns",               "Docker/Docker Compose 本地开发、容器安全、网络配置与持久化存储模式。"),
        Map.entry("documentation-lookup",          "通过 Context7 MCP 查阅最新库/框架文档，而非依赖可能过时的训练数据。"),
        Map.entry("docx",                          "Word 文档（.docx）创建、读取、编辑与格式化操作的完整工具链。"),
        Map.entry("e2e-testing",                   "Playwright E2E 测试：Page Object Model、CI/CD 集成、截图/视频/追踪等测试产物管理。"),
        Map.entry("enterprise-agent-ops",          "长期运行 Agent 工作负载的企业级运营：可观测性、安全边界与生命周期管理。"),
        Map.entry("eval-harness",                  "Claude Code 会话的形式化评估框架，实现评估驱动开发（EDD）原则。"),
        Map.entry("exa-search",                    "通过 Exa MCP 进行神经语义搜索，适用于网页、代码研究、公司调研等场景。"),
        Map.entry("executing-plans",               "在独立会话中执行已编写的实施计划，含阶段性检查点和审查流程。"),
        Map.entry("fal-ai-media",                  "通过 fal.ai MCP 统一生成图像、视频、音频，覆盖文生图、文生视频等多模态能力。"),
        Map.entry("fast-download",                 "用 Gopeed（首选）或 aria2c 多连接加速下载大文件、HuggingFace 模型、GitHub Release 等。"),
        Map.entry("finishing-a-development-branch","实现完成、测试通过后，决定如何将功能分支合并到主线（PR/直接合并/Squash）。"),
        Map.entry("frontend-patterns",             "React/Next.js 前端开发模式：状态管理、性能优化与 UI 组件设计最佳实践。"),
        Map.entry("git-workflow",                  "Git 工作流：分支策略、Commit 规范、Merge vs Rebase、冲突解决与代码审查流程。"),
        Map.entry("golang-patterns",               "惯用 Go 编程模式与规范：构建健壮、高效、可维护 Go 应用的最佳实践。"),
        Map.entry("golang-testing",                "Go 测试模式：表驱动测试、子测试、基准测试、Fuzzing 与覆盖率分析。"),
        Map.entry("gpt-researcher",                "调用本地自部署 GPT Researcher 做自主深度调研，产出带引用的 Markdown 研究报告。"),
        Map.entry("iterative-retrieval",           "渐进式精细化上下文检索模式，解决子 Agent 上下文窗口受限问题。"),
        Map.entry("java-coding-standards",         "Spring Boot 服务的 Java 编码规范：命名约定、不变性、Optional 使用、Stream 与异常处理。"),
        Map.entry("jpa-patterns",                  "JPA/Hibernate 模式：实体设计、关系映射、查询优化、事务管理与审计。"),
        Map.entry("kotlin-coroutines-flows",       "Kotlin 协程与 Flow 模式（Android/KMP）：结构化并发、Flow 操作符、StateFlow/SharedFlow。"),
        Map.entry("kotlin-exposed-patterns",       "JetBrains Exposed ORM：DSL 查询、DAO 模式、事务管理与 HikariCP 连接池配置。"),
        Map.entry("kotlin-ktor-patterns",          "Ktor 服务端模式：路由 DSL、插件、认证、Koin 依赖注入与 kotlinx.serialization。"),
        Map.entry("kotlin-patterns",               "惯用 Kotlin 编程模式与规范：构建健壮高效 Kotlin 应用的最佳实践。"),
        Map.entry("kotlin-testing",                "Kotlin 测试：Kotest、MockK、协程测试、属性测试与 Kover 覆盖率分析。"),
        Map.entry("learned",                       "从历史会话中提取并沉淀的个人化经验规律与项目专属知识。"),
        Map.entry("mcp-builder",                   "创建高质量 MCP（Model Context Protocol）Server 的设计指南，让 LLM 与外部工具交互。"),
        Map.entry("mcp-server-patterns",           "用 Node/TypeScript SDK 构建 MCP Server：工具定义、资源、提示、Zod 验证、stdio/HTTP 传输。"),
        Map.entry("pdf",                           "PDF 文件读取、内容提取、页面分析与格式转换的完整操作技能。"),
        Map.entry("postgres-patterns",             "PostgreSQL 查询优化、Schema 设计、索引策略与安全规范（基于 Supabase 最佳实践）。"),
        Map.entry("pptx",                          "PowerPoint（.pptx）文件创建、读取、编辑与幻灯片设计的完整工具链。"),
        Map.entry("prompt-optimizer",              "系统化优化 LLM Prompt，提升准确性、一致性与 API 调用成本效率。"),
        Map.entry("python-patterns",               "Pythonic 编程习惯、PEP 8 规范、类型提示与构建健壮高效 Python 应用的最佳实践。"),
        Map.entry("python-testing",                "Python 测试策略：pytest、TDD 方法论、Fixtures、Mocking、参数化与覆盖率分析。"),
        Map.entry("receiving-code-review",         "接收代码审查反馈时的标准处理流程，避免盲目实施建议引发新问题。"),
        Map.entry("regex-vs-llm-structured-text",  "解析结构化文本时选择正则还是 LLM 的决策框架——从正则开始，LLM 做兜底。"),
        Map.entry("repo-scan",                     "跨技术栈源码资产审计：文件分类、第三方库检测与依赖关系映射。"),
        Map.entry("requesting-code-review",        "发起代码审查的标准流程与提交前检查清单。"),
        Map.entry("rules-distill",                 "扫描 Skills 提取跨领域原则，提炼为可追加/修订/新建的 Rules 文件。"),
        Map.entry("save-memory",                   "将当前会话的关键进展、决策与踩坑保存到项目持久记忆，供未来会话复用。"),
        Map.entry("search-first",                  "编码前先调研：搜索现有工具、库和模式，避免重复造轮子。"),
        Map.entry("security-review",               "代码安全审查：OWASP Top 10、密钥泄露、注入攻击、认证授权漏洞检测与修复。"),
        Map.entry("security-scan",                 "扫描 Claude Code 配置目录（.claude/）的安全漏洞、错误配置与权限风险。"),
        Map.entry("skill-creator",                 "创建新 Skill、改进现有 Skill 并量化衡量 Skill 执行效果。"),
        Map.entry("springboot-patterns",           "Spring Boot 架构模式：REST API 设计、分层服务、数据访问、缓存与异步处理。"),
        Map.entry("springboot-security",           "Spring Security 最佳实践：认证/授权、输入校验、CSRF 防护、密钥管理与限流。"),
        Map.entry("springboot-tdd",                "Spring Boot TDD 实践：JUnit 5、Mockito、MockMvc、Testcontainers 与 JaCoCo 覆盖率。"),
        Map.entry("springboot-verification",       "Spring Boot 项目验证闭环：构建→静态分析→测试覆盖→安全扫描全流程。"),
        Map.entry("strategic-compact",             "在逻辑节点建议手动压缩上下文，防止任务阶段间上下文碎片化影响质量。"),
        Map.entry("subagent-driven-development",   "用 Sub-Agent 驱动开发：将复杂任务拆分后分发给专门子 Agent 并行处理。"),
        Map.entry("systematic-debugging",          "系统化调试方法论：从症状到根因的分步排查流程与工具选择指南。"),
        Map.entry("tdd-workflow",                  "测试驱动开发工作流：写测试→失败→最小实现→通过→重构，强制 80%+ 覆盖率。"),
        Map.entry("team-builder",                  "交互式 Agent 组合选择器，用于构建并分发并行 Agent 协作团队。"),
        Map.entry("test-driven-development",       "实现任何功能或修复 Bug 前必须先写测试，严格 TDD 红绿重构循环。"),
        Map.entry("un-comtrade-fetch",             "从 UN Comtrade API 拉取国际贸易统计数据（进出口量值、HS 编码、国别流向）。"),
        Map.entry("using-git-worktrees",           "在需要功能隔离或并行实验时使用 Git Worktree，避免污染主工作区。"),
        Map.entry("using-superpowers",             "对话开始时建立技能发现机制，要求通过 Skill 工具调用所有可用技能，不允许直接输出。"),
        Map.entry("verification-before-completion","声称工作完成/提交/创建 PR 之前，必须运行验证流程确认代码实际可用。"),
        Map.entry("verification-loop",             "Claude Code 会话的综合验证系统：构建、测试、类型检查、覆盖率一体化验证。"),
        Map.entry("video-editing",                 "AI 辅助视频剪辑工作流：智能剪切、结构整理与片段增强，适用于真实拍摄素材。"),
        Map.entry("webapp-testing",                "用 Playwright 与本地 Web 应用交互并测试：截图验证、表单填写、API 接口测试。"),
        Map.entry("writing-plans",                 "在开始实施前编写结构化实施计划，确保方向对齐并获得反馈。"),
        Map.entry("writing-skills",                "高质量文档与技术写作技巧：文档组织结构、清晰表达方式与格式规范。"),
        Map.entry("xlsx",                          "电子表格（.xlsx/.csv）文件读取、创建、编辑、数据分析与格式化操作。")
    );

    @GetMapping
    public List<SkillInfo> listSkills() {
        if (!Files.exists(SKILLS_DIR)) return List.of();
        List<SkillInfo> result = new ArrayList<>();
        try (Stream<Path> entries = Files.list(SKILLS_DIR)) {
            entries.sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(p -> {
                        SkillInfo s = parseSkill(p);
                        if (s != null) result.add(s);
                    });
        } catch (IOException e) { /* ignore */ }
        return result;
    }

    private SkillInfo parseSkill(Path path) {
        Path mdFile = null;
        try {
            if (Files.isRegularFile(path) && path.toString().endsWith(".md")) {
                mdFile = path;
            } else if (Files.isDirectory(path)) {
                try (Stream<Path> files = Files.list(path)) {
                    mdFile = files.filter(f -> f.toString().endsWith(".md")).findFirst().orElse(null);
                }
            }
            if (mdFile == null) return null;

            String content = Files.readString(mdFile);
            Map<String, String> fm = parseFrontmatter(content);
            String id = path.getFileName().toString().replace(".md", "");
            String body = extractBody(content);

            return new SkillInfo(
                id,
                fm.getOrDefault("name", id),
                fm.getOrDefault("description", ""),
                ZH.getOrDefault(id, ""),
                fm.getOrDefault("origin", "custom"),
                fm.getOrDefault("version", ""),
                body
            );
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, String> parseFrontmatter(String content) {
        Map<String, String> fm = new LinkedHashMap<>();
        if (!content.startsWith("---")) return fm;
        int end = content.indexOf("\n---", 3);
        if (end < 0) return fm;
        for (String line : content.substring(4, end).split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String val = line.substring(colon + 1).trim().replaceAll("^[\"']|[\"']$", "");
                fm.put(key, val);
            }
        }
        return fm;
    }

    private String extractBody(String content) {
        if (!content.startsWith("---")) return content;
        int end = content.indexOf("\n---", 3);
        if (end < 0) return content;
        return content.substring(end + 4).stripLeading();
    }
}
