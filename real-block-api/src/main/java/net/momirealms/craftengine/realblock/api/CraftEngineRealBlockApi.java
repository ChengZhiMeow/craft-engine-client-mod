package net.momirealms.craftengine.realblock.api;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Public API provided through Bukkit's services manager by CraftEngineRealBlock.
 */
public interface CraftEngineRealBlockApi {

    /**
     * Looks up all collision states for a configured real block.
     *
     * @param blockId a namespaced CraftEngine block ID, for example {@code zako:test_block}
     * @return collision information, or empty when the ID is not a loaded real block
     */
    Optional<BlockCollision> getBlockCollision(String blockId);

    /**
     * Looks up the collision state currently placed at a Bukkit block.
     *
     * <p>This reads the live world state and must be called from the thread that owns the block.</p>
     */
    Optional<BlockCollision.State> getBlockCollisionState(Block block);

    /**
     * Looks up the collision state represented by a Bukkit block-state snapshot.
     */
    Optional<BlockCollision.State> getBlockCollisionState(BlockState blockState);

    /**
     * Looks up collision boxes for one zero-based CraftEngine state index.
     *
     * @return empty when the block or state does not exist; a present empty list means that
     * the state intentionally has no collision
     */
    default Optional<List<CollisionBox>> getCollisionBoxes(String blockId, int stateIndex) {
        return getBlockCollision(blockId).flatMap(collision -> collision.collisionBoxes(stateIndex));
    }

    /**
     * Returns the collision boxes for the state currently placed at a Bukkit block.
     */
    default Optional<List<CollisionBox>> getCollisionBoxes(Block block) {
        return getBlockCollisionState(block).map(BlockCollision.State::boxes);
    }

    /**
     * Returns the collision boxes represented by a Bukkit block-state snapshot.
     */
    default Optional<List<CollisionBox>> getCollisionBoxes(BlockState blockState) {
        return getBlockCollisionState(blockState).map(BlockCollision.State::boxes);
    }
}
