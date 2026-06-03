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

            return new AgencyAgent(id, name, description, emoji, color, vibe, category, displayName, body);
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
