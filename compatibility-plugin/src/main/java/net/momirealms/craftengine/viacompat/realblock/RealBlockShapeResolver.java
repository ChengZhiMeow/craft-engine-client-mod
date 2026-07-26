package net.momirealms.craftengine.viacompat.realblock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RealBlockShapeResolver {
    private final RealBlockShape fallback;
    private final List<Rule> rules;

    RealBlockShapeResolver(RealBlockShape fallback, List<Rule> rules) {
        if (fallback == null && rules.isEmpty()) {
            throw new IllegalArgumentException("at least one real block shape is required");
        }
        this.fallback = fallback;
        List<Rule> orderedRules = new ArrayList<>(rules);
        orderedRules.sort(Comparator.comparingInt((Rule rule) -> rule.properties().size()).reversed());
        this.rules = List.copyOf(orderedRules);
    }

    RealBlockShape resolve(Map<String, String> properties) {
        for (Rule rule : rules) {
            if (properties.entrySet().containsAll(rule.properties().entrySet())) {
                return rule.shape();
            }
        }
        if (fallback != null) {
            return fallback;
        }
        throw new IllegalArgumentException("no shape matches state " + formatProperties(properties));
    }

    void validateProperties(Set<String> availableProperties) {
        for (Rule rule : rules) {
            for (String property : rule.properties().keySet()) {
                if (!availableProperties.contains(property)) {
                    throw new IllegalArgumentException(
                            "shape selector references unknown property '" + property + "'"
                    );
                }
            }
        }
    }

    static Map<String, String> parseSelector(String selector) {
        String value = selector.trim();
        if (value.equals("default") || value.isEmpty()) {
            return Map.of();
        }
        Map<String, String> properties = new LinkedHashMap<>();
        for (String assignment : value.split(",")) {
            String[] pair = assignment.trim().split("=", 2);
            if (pair.length != 2 || pair[0].isBlank() || pair[1].isBlank()) {
                throw new IllegalArgumentException(
                        "invalid shape selector '" + selector + "'; expected property=value pairs"
                );
            }
            String previous = properties.put(pair[0].trim(), pair[1].trim());
            if (previous != null) {
                throw new IllegalArgumentException(
                        "duplicate property '" + pair[0].trim() + "' in shape selector '" + selector + "'"
                );
            }
        }
        return Map.copyOf(properties);
    }

    private static String formatProperties(Map<String, String> properties) {
        return properties.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "," + right)
                .orElse("<default>");
    }

    record Rule(Map<String, String> properties, RealBlockShape shape) {
        Rule {
            properties = Map.copyOf(properties);
        }
    }
}
