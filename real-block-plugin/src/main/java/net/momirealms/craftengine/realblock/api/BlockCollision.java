package net.momirealms.craftengine.realblock.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Collision information for every state of a configured CraftEngine real block.
 */
public record BlockCollision(String blockId, List<State> states) {

    public BlockCollision {
        Objects.requireNonNull(blockId, "blockId");
        states = List.copyOf(states);
    }

    /**
     * Returns the collision boxes for the requested zero-based CraftEngine state index.
     *
     * @param stateIndex the state ordinal used by the real block's {@code ce_state} property
     * @return the state boxes, including an empty list for a state with no collision
     */
    public Optional<List<CollisionBox>> collisionBoxes(int stateIndex) {
        if (stateIndex < 0 || stateIndex >= states.size()) {
            return Optional.empty();
        }
        return Optional.of(states.get(stateIndex).boxes());
    }

    public record State(int stateIndex, List<CollisionBox> boxes) {

        public State {
            if (stateIndex < 0) {
                throw new IllegalArgumentException("stateIndex cannot be negative");
            }
            boxes = List.copyOf(boxes);
        }
    }
}
