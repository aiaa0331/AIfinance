package com.aifinance.controller;

import com.aifinance.model.HookEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/hooks")
public class HooksController {

    private static final Path SETTINGS = Path.of(System.getProperty("user.home"), ".claude", "settings.json");
    private static final Pattern SCRIPT_PAT = Pattern.compile("hooks/([\\w.-]+\\.js)");
    private static final Map<String, String> SCRIPT_DESC = Map.ofEntries(
        Map.entry("block-no-verify",        "阻止 --no-verify / --no-gpg-sign 跳过 Git hooks"),
        Map.entry("auto-tmux-dev.js",       "自动在 tmux 中启动开发服务器"),
        Map.entry("run-with-flags.js",      "带条件标志执行子脚本（格式化、lint、编译检查）"),
        Map.entry("run-with-flags-sh",      "Shell 版带标志脚本执行器"),
        Map.entry("auto-claude-md.js",      "会话启动时自动加载/更新 CLAUDE.md"),
        Map.entry("session-start.js",       "会话初始化：加载项目上下文与记忆"),
        Map.entry("post-edit-python.js",    "Python 文件编辑后自动运行 ruff/black 格式化"),
        Map.entry("auto-revise-claude-md.js","会话结束时自动更新 CLAUDE.md 学习记录"),
        Map.entry("auto-code-review.js",    "会话停止时自动触发代码审查")
    );

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public Map<String, List<HookEntry>> listHooks() {
        if (!Files.exists(SETTINGS)) return Map.of();
        try {
            JsonNode root = mapper.readTree(SETTINGS.toFile());
            JsonNode hooks = root.path("hooks");
            if (hooks.isMissingNode()) return Map.of();

            Map<String, List<HookEntry>> result = new LinkedHashMap<>();
            List<String> eventOrder = List.of("SessionStart","PreToolUse","PostToolUse",
                    "PostToolUseFailure","PreCompact","Stop","SessionEnd");

            for (String event : eventOrder) {
                if (!hooks.has(event)) continue;
                List<HookEntry> entries = new ArrayList<>();
                hooks.get(event).forEach(handler -> {
                    String matcher = handler.has("matcher") ? handler.get("matcher").asText() : "*";
                    if (handler.has("hooks")) {
                        handler.get("hooks").forEach(h -> {
                            String cmd = h.has("command") ? h.get("command").asText() : "";
                            String scriptName = extractScript(cmd);
                            String desc = SCRIPT_DESC.getOrDefault(scriptName,
                                    SCRIPT_DESC.getOrDefault(scriptName.replace(".js",""), "自定义脚本"));
                            entries.add(new HookEntry(event, matcher, scriptName, desc));
                        });
                    }
                });
                if (!entries.isEmpty()) result.put(event, entries);
            }
            return result;
        } catch (IOException e) {
            return Map.of();
        }
    }

    private String extractScript(String cmd) {
        Matcher m = SCRIPT_PAT.matcher(cmd);
        if (m.find()) return m.group(1);
        if (cmd.contains("block-no-verify")) return "block-no-verify";
        // fallback: last path segment
        String[] parts = cmd.split("[/\\s]");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isBlank()) return parts[i];
        }
        return cmd.length() > 50 ? cmd.substring(0, 50) + "…" : cmd;
    }
}
