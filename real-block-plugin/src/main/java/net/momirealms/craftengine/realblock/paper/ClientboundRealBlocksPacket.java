package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.ClientCustomPacket;
import net.momirealms.craftengine.core.plugin.network.mod.CustomPackets;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.List;

record ClientboundRealBlocksPacket(List<RealBlockDefinition> definitions) implements ClientCustomPacket {
    static final int FORMAT_VERSION = 1;
    static final Key ID = Key.ce("real_blocks");
    static final NetworkCodec<FriendlyByteBuf, ClientboundRealBlocksPacket> CODEC = ClientCustomPacket.codec(
            ClientboundRealBlocksPacket::write,
            ClientboundRealBlocksPacket::read
    );

    ClientboundRealBlocksPacket {
        definitions = List.copyOf(definitions);
    }

    static void register() {
        CustomPackets.registerClientbound(ID, CODEC, false);
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(FORMAT_VERSION);
        buffer.writeCollection(definitions, (definitionBuffer, definition) -> {
            definitionBuffer.writeKey(definition.id());
            definitionBuffer.writeCollection(definition.states(), (stateBuffer, state) -> {
                stateBuffer.writeVarInt(state.customIndex());
                writeShape(stateBuffer, state.shape());
            });
        });
    }

    private static ClientboundRealBlocksPacket read(FriendlyByteBuf buffer) {
        int formatVersion = buffer.readVarInt();
        if (formatVersion != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported real block format version " + formatVersion);
        }
        return new ClientboundRealBlocksPacket(buffer.readCollection(
                FriendlyByteBuf.limitValue(ArrayList::new, 4096),
                definitionBuffer -> new RealBlockDefinition(
                        definitionBuffer.readKey(),
                        definitionBuffer.readCollection(
                                FriendlyByteBuf.limitValue(ArrayList::new, 4096),
                                stateBuffer -> new RealBlockDefinition.RealBlockStateDefinition(
                                        stateBuffer.readVarInt(),
                                        readShape(stateBuffer)
                                )
                        )
                )
        ));
    }

    private static void writeShape(FriendlyByteBuf buffer, RealBlockShape shape) {
        writeBoxes(buffer, shape.outline());
        writeBoxes(buffer, shape.collision());
        writeBoxes(buffer, shape.support());
        writeBoxes(buffer, shape.occlusion());
    }

    private static RealBlockShape readShape(FriendlyByteBuf buffer) {
        return new RealBlockShape(readBoxes(buffer), readBoxes(buffer), readBoxes(buffer), readBoxes(buffer));
    }

    private static void writeBoxes(FriendlyByteBuf buffer, List<ShapeBox> boxes) {
        buffer.writeCollection(boxes, (boxBuffer, box) -> {
            boxBuffer.writeFloat(box.minX());
            boxBuffer.writeFloat(box.minY());
            boxBuffer.writeFloat(box.minZ());
            boxBuffer.writeFloat(box.maxX());
            boxBuffer.writeFloat(box.maxY());
            boxBuffer.writeFloat(box.maxZ());
        });
    }

    private static List<ShapeBox> readBoxes(FriendlyByteBuf buffer) {
        return buffer.readCollection(
                FriendlyByteBuf.limitValue(ArrayList::new, 64),
                boxBuffer -> new ShapeBox(
                        boxBuffer.readFloat(),
                        boxBuffer.readFloat(),
                        boxBuffer.readFloat(),
                        boxBuffer.readFloat(),
                        boxBuffer.readFloat(),
                        boxBuffer.readFloat()
                )
        );
    }

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ClientboundRealBlocksPacket> codec() {
        return CODEC;
    }
}
