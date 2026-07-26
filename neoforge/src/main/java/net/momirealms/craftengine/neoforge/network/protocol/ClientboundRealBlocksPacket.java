package net.momirealms.craftengine.neoforge.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import net.momirealms.craftengine.neoforge.block.RealBlockShape;
import net.momirealms.craftengine.neoforge.network.ClientCustomPacket;
import net.momirealms.craftengine.neoforge.network.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ClientboundRealBlocksPacket(List<BlockManager.RealBlockDefinition> definitions) implements ClientCustomPacket {
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_REAL_BLOCKS = 4096;
    public static final int MAX_STATES_PER_BLOCK = 4096;
    public static final int MAX_BOXES_PER_SHAPE = 64;
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("craftengine", "real_blocks");
    public static final Type<ClientboundRealBlocksPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundRealBlocksPacket> CODEC = ClientCustomPacket.codec(
            ClientboundRealBlocksPacket::write,
            ClientboundRealBlocksPacket::read
    );

    public ClientboundRealBlocksPacket {
        definitions = List.copyOf(definitions);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(FORMAT_VERSION);
        buf.writeCollection(definitions, (buffer, definition) -> {
            buffer.writeResourceLocation(definition.id());
            buffer.writeCollection(definition.states(), (stateBuffer, state) -> {
                stateBuffer.writeVarInt(state.customIndex());
                writeShape(stateBuffer, state.shape());
            });
        });
    }

    private static ClientboundRealBlocksPacket read(FriendlyByteBuf buf) {
        int formatVersion = buf.readVarInt();
        if (formatVersion != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported real block format version " + formatVersion);
        }
        List<BlockManager.RealBlockDefinition> definitions = buf.readCollection(
                size -> new ArrayList<>(checkedSize(size, MAX_REAL_BLOCKS, "real block")),
                buffer -> {
                    ResourceLocation id = buffer.readResourceLocation();
                    List<BlockManager.RealBlockStateDefinition> states = buffer.readCollection(
                            size -> new ArrayList<>(checkedSize(size, MAX_STATES_PER_BLOCK, "real block state")),
                            stateBuffer -> new BlockManager.RealBlockStateDefinition(
                                    stateBuffer.readVarInt(),
                                    readShape(stateBuffer)
                            )
                    );
                    return new BlockManager.RealBlockDefinition(id, states);
                }
        );
        return new ClientboundRealBlocksPacket(definitions);
    }

    private static void writeShape(FriendlyByteBuf buf, RealBlockShape shape) {
        writeBoxes(buf, shape.outline());
        writeBoxes(buf, shape.collision());
        writeBoxes(buf, shape.support());
        writeBoxes(buf, shape.occlusion());
    }

    private static RealBlockShape readShape(FriendlyByteBuf buf) {
        return new RealBlockShape(readBoxes(buf), readBoxes(buf), readBoxes(buf), readBoxes(buf));
    }

    private static void writeBoxes(FriendlyByteBuf buf, List<RealBlockShape.Box> boxes) {
        buf.writeCollection(boxes, (buffer, box) -> {
            buffer.writeFloat(box.minX());
            buffer.writeFloat(box.minY());
            buffer.writeFloat(box.minZ());
            buffer.writeFloat(box.maxX());
            buffer.writeFloat(box.maxY());
            buffer.writeFloat(box.maxZ());
        });
    }

    private static List<RealBlockShape.Box> readBoxes(FriendlyByteBuf buf) {
        return buf.readCollection(
                size -> new ArrayList<>(checkedSize(size, MAX_BOXES_PER_SHAPE, "shape box")),
                buffer -> {
                    RealBlockShape.Box box = new RealBlockShape.Box(
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat()
                    );
                    validateBox(box);
                    return box;
                }
        );
    }

    private static int checkedSize(int size, int maximum, String name) {
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("Invalid " + name + " count " + size + " (maximum " + maximum + ")");
        }
        return size;
    }

    private static void validateBox(RealBlockShape.Box box) {
        float[] values = {box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()};
        for (float value : values) {
            if (!Float.isFinite(value) || value < 0 || value > 16) {
                throw new IllegalArgumentException("Real block box coordinate must be finite and between 0 and 16");
            }
        }
        if (box.minX() >= box.maxX() || box.minY() >= box.maxY() || box.minZ() >= box.maxZ()) {
            throw new IllegalArgumentException("Real block box minimum must be lower than maximum");
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ClientboundRealBlocksPacket> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Type<ClientboundRealBlocksPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(Context context) {
        context.client().execute(() -> {
            BlockManager.instance().installRealBlocks(definitions);
            context.client().reloadResourcePacks();
        });
    }
}
