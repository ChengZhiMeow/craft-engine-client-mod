package net.momirealms.craftengine.neoforge.jade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockItemModelMatcherTest {

    @Test
    void returnsTheOnlyCandidateForAModel() {
        BlockItemModelMatcher.UniqueIndex<String, String> index = new BlockItemModelMatcher.UniqueIndex<>();

        index.add("purple_bricks", "terracotta_expansion:purple_terracotta_bricks", "紫色陶瓦砖块");

        assertEquals("紫色陶瓦砖块", index.find("purple_bricks").orElseThrow());
    }

    @Test
    void acceptsDuplicateCreativeEntriesForTheSameCraftEngineItem() {
        BlockItemModelMatcher.UniqueIndex<String, String> index = new BlockItemModelMatcher.UniqueIndex<>();

        index.add("purple_bricks", "terracotta_expansion:purple_terracotta_bricks", "紫色陶瓦砖块");
        index.add("purple_bricks", "terracotta_expansion:purple_terracotta_bricks", "紫色陶瓦砖块副本");

        assertEquals("紫色陶瓦砖块", index.find("purple_bricks").orElseThrow());
    }

    @Test
    void rejectsAnAmbiguousModelInsteadOfShowingTheWrongName() {
        BlockItemModelMatcher.UniqueIndex<String, String> index = new BlockItemModelMatcher.UniqueIndex<>();

        index.add("shared_model", "example:first", "第一个方块");
        index.add("shared_model", "example:second", "第二个方块");

        assertTrue(index.find("shared_model").isEmpty());
    }
}
