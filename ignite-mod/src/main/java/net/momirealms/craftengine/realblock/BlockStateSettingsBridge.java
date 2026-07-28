package net.momirealms.craftengine.realblock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public interface BlockStateSettingsBridge {
    Settings craftengine$settings();

    void craftengine$copySettingsFrom(BlockState source);

    record Settings(
            int lightEmission,
            boolean useShapeForLightOcclusion,
            boolean air,
            boolean ignitedByLava,
            boolean liquid,
            boolean legacySolid,
            PushReaction pushReaction,
            MapColor mapColor,
            float destroySpeed,
            boolean requiresCorrectToolForDrops,
            boolean canOcclude,
            BlockBehaviour.StatePredicate redstoneConductor,
            BlockBehaviour.StatePredicate suffocating,
            BlockBehaviour.StatePredicate viewBlocking,
            BlockBehaviour.StatePredicate postProcess,
            BlockBehaviour.StatePredicate emissiveRendering,
            BlockBehaviour.OffsetFunction offsetFunction,
            boolean spawnTerrainParticles,
            NoteBlockInstrument instrument,
            boolean replaceable,
            boolean randomlyTicking
    ) {
    }
}
