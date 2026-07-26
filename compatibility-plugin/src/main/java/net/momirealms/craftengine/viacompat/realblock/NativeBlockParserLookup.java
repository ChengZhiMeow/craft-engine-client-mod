package net.momirealms.craftengine.viacompat.realblock;

import net.momirealms.craftengine.core.plugin.config.ConfigParser;

import java.util.Arrays;

final class NativeBlockParserLookup {
    private NativeBlockParserLookup() {
    }

    static ConfigParser find(ConfigParser[] parsers) {
        return Arrays.stream(parsers)
                .filter(parser -> Arrays.stream(parser.sectionId()).anyMatch("blocks"::equals))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("CraftEngine native blocks parser does not exist"));
    }
}
