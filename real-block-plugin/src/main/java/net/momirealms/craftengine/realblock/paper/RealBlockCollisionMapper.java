package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.realblock.api.BlockCollision;
import net.momirealms.craftengine.realblock.api.CollisionBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

final class RealBlockCollisionMapper {

    private RealBlockCollisionMapper() {
    }

    static Map<String, BlockCollision> createSnapshot(Map<Key, RealBlockDefinition> definitions) {
        Map<String, BlockCollision> snapshot = new LinkedHashMap<>(definitions.size());
        definitions.forEach((id, definition) -> snapshot.put(id.toString(), toApiCollision(definition)));
        return Map.copyOf(snapshot);
    }

    private static BlockCollision toApiCollision(RealBlockDefinition definition) {
        List<BlockCollision.State> states = IntStream.range(0, definition.states().size())
                .mapToObj(stateIndex -> new BlockCollision.State(
                        stateIndex,
                        definition.states().get(stateIndex).shape().collision().stream()
                                .map(RealBlockCollisionMapper::toApiBox)
                                .toList()
                ))
                .toList();
        return new BlockCollision(definition.id().toString(), states);
    }

    private static CollisionBox toApiBox(ShapeBox box) {
        return new CollisionBox(
                box.minX(),
                box.minY(),
                box.minZ(),
                box.maxX(),
                box.maxY(),
                box.maxZ()
        );
    }
}
