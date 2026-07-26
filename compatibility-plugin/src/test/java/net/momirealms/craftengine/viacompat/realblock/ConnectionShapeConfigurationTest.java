package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionShapeConfigurationTest {

    @Test
    void canonicalConnectionShapeBuildsSixWayCenterArmGeometry() {
        RealBlockShapeResolver resolver = RealBlockConfigParser.parseConnectionShape(
                ConfigSection.of("real_blocks.zako:test_block", Map.of()),
                Map.of(
                        "type", "center-arms",
                        "radius", 2,
                        "property", "connections"
                ),
                false
        );

        RealBlockShape shape = resolver.resolve(Map.of("connections", "63"));

        assertEquals(7, shape.collision().size());
    }

    @Test
    void legacyCableConfigurationRemainsACompatibleAdapter() {
        ConfigSection root = ConfigSection.of("real_blocks.zako:test_block", Map.of());
        RealBlockShape canonical = RealBlockConfigParser.parseConnectionShape(
                root,
                Map.of(
                        "type", "center-arms",
                        "radius", 2,
                        "property", "connections"
                ),
                false
        ).resolve(Map.of("connections", "48"));
        RealBlockShape legacy = RealBlockConfigParser.parseConnectionShape(
                root,
                Map.of(
                        "radius", 2,
                        "connections-property", "connections"
                ),
                true
        ).resolve(Map.of("connections", "48"));

        assertEquals(canonical, legacy);
    }
}
