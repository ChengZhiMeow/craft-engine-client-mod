package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class RealBlockConfigParser extends IdSectionConfigParser {
    private static final String[] SECTION_ID = {"real_blocks"};
    private static final LoadingStage REAL_BLOCK_STAGE = new LoadingStage("real_blocks");
    private final RealBlockManager manager;
    private final ConfigParser nativeBlockParser;

    RealBlockConfigParser(RealBlockManager manager, ConfigParser nativeBlockParser) {
        this.manager = manager;
        this.nativeBlockParser = nativeBlockParser;
    }

    @Override
    public void addConfig(CachedConfigSection config) {
        nativeBlockParser.addConfig(config);
        super.addConfig(config);
    }

    @Override
    public String[] sectionId() {
        return SECTION_ID;
    }

    @Override
    public LoadingStage loadingStage() {
        return REAL_BLOCK_STAGE;
    }

    @Override
    public List<LoadingStage> dependencies() {
        return List.of(LoadingStages.BLOCK);
    }

    @Override
    public void preProcess() {
        manager.beginLoad();
    }

    @Override
    protected void parseSection(Pack pack, Path path, Key id, ConfigSection section) {
        try {
            Object rawConnectionShape = section.get("connection-shape");
            Object rawLegacyCable = section.get("cable");
            Object rawShapes = section.get("shapes");
            if (rawShapes != null && !(rawShapes instanceof Map<?, ?>)) {
                throw new IllegalArgumentException(section.assemblePath("shapes") + " must be a map");
            }
            if (rawConnectionShape != null && rawLegacyCable != null) {
                throw new IllegalArgumentException(
                        section.path() + " cannot contain both connection-shape and legacy cable"
                );
            }
            Object generatedShape = rawConnectionShape != null ? rawConnectionShape : rawLegacyCable;
            if (generatedShape != null && rawShapes != null) {
                throw new IllegalArgumentException(
                        section.path() + " cannot contain both a generated connection shape and shapes"
                );
            }
            if (generatedShape != null) {
                manager.addDefinition(
                        path,
                        id,
                        parseConnectionShape(section, generatedShape, rawLegacyCable != null)
                );
                return;
            }
            RealBlockShape fallback = section.get("collision") == null
                    ? null
                    : parseShape(section);
            List<RealBlockShapeResolver.Rule> rules = new ArrayList<>();
            if (rawShapes instanceof Map<?, ?> shapes) {
                for (Map.Entry<?, ?> entry : shapes.entrySet()) {
                    String selector = String.valueOf(entry.getKey());
                    ConfigSection shapeSection = ConfigSection.of(
                            section.assemblePath("shapes") + "." + selector,
                            entry.getValue()
                    );
                    rules.add(new RealBlockShapeResolver.Rule(
                            RealBlockShapeResolver.parseSelector(selector),
                            parseShape(shapeSection)
                    ));
                }
            }
            manager.addDefinition(path, id, new RealBlockShapeResolver(fallback, rules));
        } catch (RuntimeException exception) {
            manager.logger().warn(path, "Invalid real_blocks entry " + id + ": " + exception.getMessage(), exception);
            throw exception;
        }
    }

    @Override
    public void postProcess() {
        manager.finishLoad();
    }

    @Override
    public int count() {
        return manager.definitionCount();
    }

    @Override
    public boolean async() {
        return false;
    }

    private static RealBlockShape parseShape(ConfigSection section) {
        List<ShapeBox> collision = parseBoxes(section.get("collision"), section.assemblePath("collision"), true);
        List<ShapeBox> outline = section.get("outline") == null
                ? collision
                : parseBoxes(section.get("outline"), section.assemblePath("outline"), false);
        List<ShapeBox> support = section.get("support") == null
                ? collision
                : parseBoxes(section.get("support"), section.assemblePath("support"), false);
        List<ShapeBox> occlusion = parseBoxes(section.get("occlusion"), section.assemblePath("occlusion"), false);
        return new RealBlockShape(outline, collision, support, occlusion);
    }

    static RealBlockShapeResolver parseConnectionShape(
            ConfigSection section,
            Object rawConnectionShape,
            boolean legacyCable
    ) {
        String key = legacyCable ? "cable" : "connection-shape";
        if (!(rawConnectionShape instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(section.assemblePath(key) + " must be a map");
        }
        ConfigSection connectionShape = ConfigSection.of(section.assemblePath(key), rawConnectionShape);
        String type = connectionShape.getString("type", "center-arms");
        if (!"center-arms".equals(type)) {
            throw new IllegalArgumentException(
                    connectionShape.assemblePath("type") + " has unsupported value " + type
            );
        }
        float radius = connectionShape.getFloat("radius", 2.0f);
        if (!Float.isFinite(radius) || radius <= 0.0f || radius >= 8.0f) {
            throw new IllegalArgumentException(
                    connectionShape.assemblePath("radius") + " must be within (0, 8)"
            );
        }
        String legacyProperty = connectionShape.getString("connections-property", "connections");
        String property = connectionShape.getString("property", legacyProperty);
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException(
                    connectionShape.assemblePath("property") + " cannot be blank"
            );
        }
        List<RealBlockShapeResolver.Rule> rules = new ArrayList<>(64);
        for (int mask = 0; mask < 64; mask++) {
            rules.add(new RealBlockShapeResolver.Rule(
                    Map.of(property, Integer.toString(mask)),
                    CenterArmShapeGenerator.create(radius, mask)
            ));
        }
        return new RealBlockShapeResolver(null, rules);
    }

    static List<ShapeBox> parseBoxes(Object raw, String path, boolean required) {
        if (raw == null) {
            if (required) {
                throw new IllegalArgumentException(path + " is required");
            }
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new IllegalArgumentException(path + " must be a list");
        }
        List<ShapeBox> boxes = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            boxes.add(parseBox(list.get(i), path + "[" + i + "]"));
        }
        return boxes;
    }

    private static ShapeBox parseBox(Object raw, String path) {
        List<?> values;
        if (raw instanceof List<?> list) {
            values = list;
        } else if (raw instanceof String string) {
            values = List.of(string.split(","));
        } else {
            throw new IllegalArgumentException(path + " must be a six-number list");
        }
        if (values.size() != 6) {
            throw new IllegalArgumentException(path + " must contain exactly six coordinates");
        }
        float[] coordinates = new float[6];
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            try {
                coordinates[i] = value instanceof Number number
                        ? number.floatValue()
                        : Float.parseFloat(value.toString().trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(path + " contains a non-number: " + value);
            }
        }
        return new ShapeBox(
                coordinates[0], coordinates[1], coordinates[2],
                coordinates[3], coordinates[4], coordinates[5]
        );
    }
}
