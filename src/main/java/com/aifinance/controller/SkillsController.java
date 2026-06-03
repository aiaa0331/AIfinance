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
