package com.aifinance.service;

import com.aifinance.model.AgencyAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AgencyAgentService {

    private static final Map<String, String> ZH_DESCRIPTIONS = Map.ofEntries(
        // ── Finance ──────────────────────────────────────────────────────────
        Map.entry("Bookkeeper & Controller",  "簿记员与财务主管 Agent。精通日常会计运营、财务对账与月末关账，确保财务记录准确完整，符合 GAAP 合规要求。"),
        Map.entry("Financial Analyst",        "财务分析师 Agent。专注财务建模、预测与场景分析，将原始财务数据转化为驱动战略与投资决策的商业洞察。"),
        Map.entry("FP&A Analyst",             "FP&A 分析师 Agent。精通预算编制、差异分析与滚动预测，连接财务数字与业务叙事，优化资源配置与运营绩效。"),
        Map.entry("Investment Researcher",    "投资研究员 Agent。深度基本面与量化分析，识别投资机会、评估风险，覆盖公开股票、私募及另类资产类别。"),
        Map.entry("Tax Strategist",           "税务策略师 Agent。精通税务优化、多司法管辖区合规与转让定价，在确保全面合规的前提下最小化企业税务负担。"),
        // ── Engineering ──────────────────────────────────────────────────────
        Map.entry("Senior Developer",         "高级全栈开发工程师 Agent。精通 Laravel/Livewire/Three.js 与高级 CSS，构建兼具功能性与视觉品质的高端 Web 体验。"),
        Map.entry("AI Engineer",              "AI 工程师 Agent。专注 AI 能力集成与智能系统开发，将 LLM、向量搜索、Agent 工作流落地为生产级应用。"),
        Map.entry("Backend Architect",        "后端架构师 Agent。设计高可扩展、低耦合的服务端系统，负责 API 设计、数据库建模与微服务拆分。"),
        Map.entry("Frontend Developer",       "前端开发工程师 Agent。构建响应式、无障碍的现代 UI，精通 React/Vue/TypeScript 及性能优化。"),
        Map.entry("Security Engineer",        "安全工程师 Agent。负责漏洞检测、渗透测试与安全代码审计，确保系统符合 OWASP 与行业安全标准。"),
        Map.entry("DevOps Automator",         "DevOps 自动化 Agent。构建 CI/CD 流水线、容器化部署与基础设施即代码，提升研发效率与发布稳定性。"),
        Map.entry("Data Engineer",            "数据工程师 Agent。设计和维护数据管道、ETL 流程与数据仓库，确保数据质量与可用性。"),
        Map.entry("Mobile App Builder",       "移动端开发 Agent。使用 React Native / Flutter 构建跨平台移动应用，兼顾性能与用户体验。"),
        Map.entry("Software Architect",       "软件架构师 Agent。制定系统级技术决策，平衡可扩展性、可维护性与业务需求，输出架构方案与技术路线。"),
        Map.entry("Rapid Prototyper",         "快速原型师 Agent。在极短时间内将创意变成可运行 Demo，验证技术可行性与产品方向。"),
        Map.entry("Code Reviewer",            "代码审查 Agent。从安全性、性能、可维护性多维度审查代码，给出具体改进建议。"),
        Map.entry("Technical Writer",         "技术文档工程师 Agent。将复杂技术细节转化为清晰易懂的 API 文档、用户手册与开发指南。"),
        Map.entry("Database Optimizer",       "数据库优化 Agent。分析慢查询、设计索引策略、优化 Schema，提升数据库读写性能。"),
        Map.entry("SRE",                      "站点可靠性工程师 Agent。负责可观测性建设、故障响应与容量规划，确保系统 SLA/SLO 达标。"),
        Map.entry("Incident Response Commander", "故障指挥官 Agent。统筹协调线上事故响应流程，快速定位根因并推动恢复，同步内外部沟通。"),
        // ── Marketing ────────────────────────────────────────────────────────
        Map.entry("Xiaohongshu Specialist",   "小红书营销专家 Agent。精通生活方式内容创作与种草策略，驱动品牌在小红书的自然流量增长与社区建设。"),
        Map.entry("Douyin Strategist",        "抖音策略师 Agent。擅长短视频内容策划与流量算法运营，将品牌打造成抖音爆款内容机器。"),
        Map.entry("WeChat Official Account",  "微信公众号运营 Agent。精通图文内容创作、排版与用户运营，提升公众号阅读量与粉丝粘性。"),
        Map.entry("Weibo Strategist",         "微博策略师 Agent。擅长热点借势与话题营销，在微博构建品牌声量与话题传播力。"),
        Map.entry("Zhihu Strategist",         "知乎策略师 Agent。通过专业问答与长文内容建立品牌知识权威，驱动高净值用户的信任与转化。"),
        Map.entry("Bilibili Content Strategist", "B 站内容策略师 Agent。面向年轻用户群体创作专业向知识性视频，打造品牌在 B 站的内容矩阵。"),
        Map.entry("Kuaishou Strategist",      "快手策略师 Agent。深耕下沉市场内容与电商直播，贴近真实生活场景驱动品牌转化。"),
        Map.entry("LinkedIn Content Creator", "LinkedIn 内容创作 Agent。打造专业 B2B 思想领导力内容，建立个人或品牌的行业影响力。"),
        Map.entry("SEO Specialist",           "SEO 优化 Agent。通过关键词研究、内容优化与技术 SEO 提升搜索引擎排名，驱动自然流量增长。"),
        Map.entry("Content Creator",          "内容创作 Agent。跨平台创作高质量内容，包括文章、社交帖子、视频脚本与营销文案。"),
        Map.entry("Social Media Strategist",  "社交媒体策略师 Agent。制定跨平台社交媒体战略，统一品牌声音并提升用户参与度与粉丝增长。"),
        Map.entry("Growth Hacker",            "增长黑客 Agent。通过数据驱动的产品与营销实验快速找到 PMF，实现低成本高速用户增长。"),
        Map.entry("TikTok Strategist",        "TikTok 策略师 Agent。把握短视频趋势与算法机制，制作高完播率内容推动品牌全球传播。"),
        Map.entry("Twitter Engager",          "Twitter/X 运营 Agent。通过精准互动与热点回应，建立品牌在 Twitter 的专业形象与粉丝社区。"),
        Map.entry("China Market Localization Strategist", "中国市场本地化策略师 Agent。帮助海外品牌进行文化适配与产品本地化，打通中国市场进入路径。"),
        // ── Sales ─────────────────────────────────────────────────────────────
        Map.entry("Sales Coach",              "销售教练 Agent。通过角色扮演、话术训练与复盘分析提升销售团队能力，缩短新人成单周期。"),
        Map.entry("Deal Strategist",          "成单策略师 Agent。分析复杂销售机会，制定针对性的赢单策略、应对竞争对手并推进关键决策人。"),
        Map.entry("Account Strategist",       "客户策略师 Agent。深度分析重点客户，制定关系拓展与业务增长路径，提升客户生命周期价值。"),
        Map.entry("Sales Discovery Coach",    "销售发现教练 Agent。训练销售掌握 SPIN/MEDDIC 等发现技巧，精准挖掘客户痛点与购买动机。"),
        Map.entry("Sales Engineer",           "售前工程师 Agent。将复杂技术产品转化为客户可理解的解决方案，支撑销售完成技术评估与 POC。"),
        Map.entry("Outbound Strategist",      "外呼策略师 Agent。设计多触点外联序列，个性化开发陌生客户，提升邮件与电话的回复转化率。"),
        Map.entry("Pipeline Analyst",         "销售漏斗分析 Agent。分析销售管道数据，识别卡点与风险，预测收入并给出阶段性改进建议。"),
        Map.entry("Proposal Strategist",      "提案策略师 Agent。撰写有说服力的销售方案与 RFP 响应，突出价值主张并针对评分标准优化。"),
        // ── Product ───────────────────────────────────────────────────────────
        Map.entry("Product Manager",          "产品经理 Agent。从用户研究到需求定义、路线图规划与优先级排序，推动产品从 0 到 1 落地。"),
        Map.entry("Feedback Synthesizer",     "用户反馈分析 Agent。从多渠道汇聚用户反馈，提取高频痛点与机会，转化为可执行的产品洞察。"),
        Map.entry("Sprint Prioritizer",       "迭代优先级 Agent。基于 RICE/ICE 等框架评分，帮助团队在 Sprint 规划中做出最优资源分配决策。"),
        Map.entry("Trend Researcher",         "趋势研究 Agent。追踪行业前沿动态、竞品动向与新兴技术，为产品战略提供前瞻性市场洞察。"),
        // ── Design ────────────────────────────────────────────────────────────
        Map.entry("UX Architect",             "用户体验架构师 Agent。设计信息架构、用户流程与交互逻辑，确保产品体验的一致性与易用性。"),
        Map.entry("UI Designer",              "UI 设计师 Agent。创作视觉风格统一、符合品牌调性的界面设计，平衡美观与功能性。"),
        Map.entry("UX Researcher",            "用户研究 Agent。通过访谈、可用性测试与数据分析挖掘用户真实需求，为设计决策提供依据。"),
        Map.entry("Brand Guardian",           "品牌守护 Agent。维护品牌一致性，审查跨渠道的视觉与语言表达，确保品牌形象统一。"),
        Map.entry("Image Prompt Engineer",    "图像提示词工程师 Agent。精心设计 AI 图像生成提示词，产出高质量的摄影级视觉内容。"),
        Map.entry("Visual Storyteller",       "视觉叙事 Agent。将数据与信息转化为引人入胜的图表、信息图与视觉叙事。"),
        // ── Academic ─────────────────────────────────────────────────────────
        Map.entry("Anthropologist",           "人类学家 Agent。运用民族志方法构建文化连贯的社会系统，让世界观和社会结构具有真实的生命力。"),
        Map.entry("Geographer",               "地理学家 Agent。分析地形、气候、资源与人类聚居模式，构建科学自洽的地理世界。"),
        Map.entry("Historian",                "历史学家 Agent。基于一手/二手史料进行历史分析与时期考证，确保历史设定的真实性与细节丰富度。"),
        Map.entry("Narratologist",            "叙事学家 Agent。运用普罗普到坎贝尔等叙事理论框架，分析和指导故事结构、角色弧与情节设计。"),
        Map.entry("Psychologist",             "心理学家 Agent。基于人格理论与认知科学，构建心理可信的角色动机与人物关系。"),
        // ── Testing ──────────────────────────────────────────────────────────
        Map.entry("API Tester",               "API 测试 Agent。设计和执行 REST/GraphQL 接口测试，覆盖边界条件、鉴权与错误处理场景。"),
        Map.entry("Accessibility Auditor",    "无障碍审计 Agent。按 WCAG 标准检查 Web 应用的可访问性，为残障用户提供包容性体验。"),
        Map.entry("Performance Benchmarker",  "性能基准测试 Agent。设计负载测试方案，分析系统在高并发下的吞吐量、延迟与资源消耗。"),
        Map.entry("Test Results Analyzer",    "测试结果分析 Agent。解读测试报告，识别高风险缺陷模式，优化测试覆盖策略。"),
        // ── Specialized ──────────────────────────────────────────────────────
        Map.entry("Compliance Auditor",       "合规审计 Agent。评估业务流程与技术系统的合规风险，生成差距分析报告并提出整改建议。"),
        Map.entry("Legal Document Review",    "法律文件审查 Agent。分析合同条款、识别风险条款并提供修改建议，降低法律合规风险。"),
        Map.entry("KYC Screener",             "KYC 合规筛查 Agent。处理客户身份核查与反洗钱筛查，覆盖制裁名单与 PEP 识别。"),
        Map.entry("Blockchain Security Auditor", "区块链安全审计 Agent。审计智能合约代码，识别重入攻击、整数溢出等常见漏洞并给出修复方案。"),
        Map.entry("Customer Service",         "客服 Agent。处理多渠道客户咨询，提供准确信息与问题解决方案，提升客户满意度。"),
        Map.entry("Recruitment Specialist",   "招聘专家 Agent。从 JD 优化到候选人筛选与面试评估，加速人才获取流程。"),
        Map.entry("Supply Chain Strategist",  "供应链策略师 Agent。分析供应链风险与瓶颈，优化库存策略与供应商关系管理。"),
        Map.entry("Language Translator",      "语言翻译 Agent。提供高质量多语言翻译，保留原文语气、风格与文化背景。"),
        Map.entry("Government Digital Presales Consultant", "政府数字化售前顾问 Agent。熟悉政府采购流程与数字化政策，协助制定合规的方案建议书。"),
        // ── Strategy ─────────────────────────────────────────────────────────
        Map.entry("NEXUS — Network of EXperts, Unified in Strategy", "NEXUS 多 Agent 战略协调框架。将独立专家 Agent 整合为协同情报网络，通过剧本与指挥流程将 Agency 变成力量倍增器。"),
        // ── Paid Media ────────────────────────────────────────────────────────
        Map.entry("PPC Strategist",           "PPC 策略师 Agent。管理搜索竞价广告的关键词策略、出价优化与广告文案，最大化 ROAS。"),
        Map.entry("Paid Social Strategist",   "付费社交策略师 Agent。在 Meta/TikTok/微信等平台设计精准受众投放策略，驱动品牌认知与转化。"),
        Map.entry("Programmatic Buyer",       "程序化广告采购 Agent。通过 DSP 平台执行 RTB 策略，优化 CPM 与广告库存组合。"),
        Map.entry("Creative Strategist",      "创意策略师 Agent。将品牌故事转化为跨渠道广告创意方向，测试与迭代高效素材。"),
        // ── Project Management ────────────────────────────────────────────────
        Map.entry("Project Shepherd",         "项目牧羊人 Agent。追踪项目里程碑与风险，协调跨团队依赖，确保项目按时高质量交付。"),
        Map.entry("Senior Project Manager",   "高级项目经理 Agent。管理复杂多线并行项目，平衡范围、时间与资源，驱动干系人对齐。"),
        // ── Spatial Computing ─────────────────────────────────────────────────
        Map.entry("visionOS Spatial Engineer", "visionOS 空间计算工程师 Agent。使用 SwiftUI + RealityKit 开发沉浸式空间体验，适配 Apple Vision Pro。"),
        Map.entry("XR Immersive Developer",   "XR 沉浸式开发 Agent。在 AR/VR/MR 设备上构建交互式空间应用，融合真实与虚拟世界。")
    );

    // Categories that contain actual agent definition files
    private static final Set<String> AGENT_CATEGORIES = Set.of(
            "academic", "design", "engineering", "finance", "game-development",
            "marketing", "paid-media", "product", "project-management",
            "sales", "spatial-computing", "specialized", "strategy",
            "support", "testing"
    );

    private static final Map<String, String> CATEGORY_DISPLAY = Map.ofEntries(
            Map.entry("academic", "Academic"),
            Map.entry("design", "Design"),
            Map.entry("engineering", "Engineering"),
            Map.entry("finance", "Finance"),
            Map.entry("game-development", "Game Development"),
            Map.entry("marketing", "Marketing"),
            Map.entry("paid-media", "Paid Media"),
            Map.entry("product", "Product"),
            Map.entry("project-management", "Project Management"),
            Map.entry("sales", "Sales"),
            Map.entry("spatial-computing", "Spatial Computing"),
            Map.entry("specialized", "Specialized"),
            Map.entry("strategy", "Strategy"),
            Map.entry("support", "Support"),
            Map.entry("testing", "Testing")
    );

    @Value("${agency.agents.path:#{systemProperties['user.home']}/IdeaProjects/agency-agents}")
    private String agentsBasePath;

    private final Map<String, AgencyAgent> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;

    public List<AgencyAgent> findAll() {
        ensureLoaded();
        return new ArrayList<>(cache.values());
    }

    public Optional<AgencyAgent> findById(String id) {
        ensureLoaded();
        return Optional.ofNullable(cache.get(id));
    }

    public int count() {
        ensureLoaded();
        return cache.size();
    }

    private synchronized void ensureLoaded() {
        if (loaded) return;
        try {
            loadAgents();
            loaded = true;
        } catch (Exception e) {
            // Silently allow empty catalog if path not found
        }
    }

    private void loadAgents() throws IOException {
        Path base = Path.of(agentsBasePath);
        if (!Files.exists(base)) return;

        try (Stream<Path> dirs = Files.list(base)) {
            dirs.filter(Files::isDirectory)
                    .filter(d -> AGENT_CATEGORIES.contains(d.getFileName().toString()))
                    .forEach(categoryDir -> loadCategory(categoryDir));
        }
    }

    private void loadCategory(Path categoryDir) {
        String category = categoryDir.getFileName().toString();
        String displayName = CATEGORY_DISPLAY.getOrDefault(category, capitalize(category));
        try (Stream<Path> files = Files.walk(categoryDir, 2)) {
            files.filter(f -> f.toString().endsWith(".md"))
                    .filter(f -> !isNonAgentFile(f.getFileName().toString()))
                    .forEach(f -> {
                        AgencyAgent agent = parseAgentFile(f, category, displayName);
                        if (agent != null) cache.put(agent.id(), agent);
                    });
        } catch (IOException e) {
            // Skip unreadable directories
        }
    }

    private boolean isNonAgentFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.startsWith("readme") || lower.startsWith("contributing")
                || lower.startsWith("license") || lower.startsWith("security")
                || lower.startsWith("executive") || lower.startsWith("quickstart")
                || lower.startsWith("changelog") || lower.startsWith("index");
    }

    private AgencyAgent parseAgentFile(Path file, String category, String displayName) {
        try {
            String content = Files.readString(file);
            if (!content.startsWith("---")) return null;

            int firstEnd = content.indexOf("\n---", 3);
            if (firstEnd < 0) return null;

            String frontmatter = content.substring(4, firstEnd);
            String body = content.substring(firstEnd + 4).stripLeading();

            Map<String, String> fm = parseFrontmatter(frontmatter);
            String name = fm.get("name");
            if (name == null || name.isBlank()) return null;

            String id = category + "/" + file.getFileName().toString().replace(".md", "");
            String description = fm.getOrDefault("description", "");
            String emoji = fm.getOrDefault("emoji", categoryEmoji(category));
            String color = stripQuotes(fm.getOrDefault("color", categoryColor(category)));
            String vibe = fm.getOrDefault("vibe", "");
            String descriptionZh = ZH_DESCRIPTIONS.getOrDefault(name,
                    categoryZhPrefix(category) + "专家 Agent。" + (vibe.isBlank() ? "" : vibe));

            return new AgencyAgent(id, name, description, descriptionZh, emoji, color, vibe, category, displayName, body);
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, String> parseFrontmatter(String frontmatter) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : frontmatter.split("\n")) {
            int colon = line.indexOf(':');
            if (colon <= 0) continue;
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            result.put(key, stripQuotes(value));
        }
        return result;
    }

    private String stripQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1)
            return s.substring(1, s.length() - 1);
        if (s.startsWith("'") && s.endsWith("'") && s.length() > 1)
            return s.substring(1, s.length() - 1);
        return s;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("-", " ");
    }

    private String categoryZhPrefix(String cat) {
        return switch (cat) {
            case "engineering" -> "工程技术";
            case "marketing" -> "营销推广";
            case "finance" -> "财务金融";
            case "sales" -> "销售商务";
            case "design" -> "设计创意";
            case "product" -> "产品管理";
            case "academic" -> "学术研究";
            case "testing" -> "质量测试";
            case "strategy" -> "战略规划";
            case "specialized" -> "专业领域";
            case "support" -> "运营支持";
            case "paid-media" -> "付费媒体";
            case "project-management" -> "项目管理";
            case "game-development" -> "游戏开发";
            case "spatial-computing" -> "空间计算";
            default -> "通用职能";
        };
    }

    private String categoryEmoji(String cat) {
        return switch (cat) {
            case "engineering" -> "⚙️";
            case "marketing" -> "📣";
            case "finance" -> "💹";
            case "sales" -> "🤝";
            case "design" -> "🎨";
            case "product" -> "📦";
            case "academic" -> "🎓";
            case "testing" -> "🧪";
            case "strategy" -> "♟️";
            case "specialized" -> "🔧";
            case "support" -> "🛟";
            case "paid-media" -> "📢";
            case "project-management" -> "📋";
            case "game-development" -> "🎮";
            case "spatial-computing" -> "🥽";
            default -> "🤖";
        };
    }

    private String categoryColor(String cat) {
        return switch (cat) {
            case "engineering" -> "#1d4ed8";
            case "marketing" -> "#be185d";
            case "finance" -> "#166534";
            case "sales" -> "#c2410c";
            case "design" -> "#7c3aed";
            case "product" -> "#0e7490";
            case "academic" -> "#b45309";
            case "testing" -> "#4338ca";
            case "strategy" -> "#1f2937";
            case "specialized" -> "#374151";
            case "support" -> "#065f46";
            case "paid-media" -> "#9d174d";
            case "project-management" -> "#1e40af";
            case "game-development" -> "#7e1d1d";
            case "spatial-computing" -> "#1a1a2e";
            default -> "#374151";
        };
    }
}
