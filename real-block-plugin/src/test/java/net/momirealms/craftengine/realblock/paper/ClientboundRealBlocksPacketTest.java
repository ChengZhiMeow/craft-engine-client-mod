package net.momirealms.craftengine.realblock.paper;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundRealBlocksPacketTest {

    @Test
    void usesTheSameWireLayoutAsTheNeoForgeDecoder() {
        RealBlockShape shape = new RealBlockShape(
                List.of(new ShapeBox(6, 0, 6, 10, 16, 10)),
                List.of(new ShapeBox(6, 0, 6, 10, 16, 10)),
                List.of(new ShapeBox(6, 0, 6, 10, 16, 10)),
                List.of()
        );
        ClientboundRealBlocksPacket packet = new ClientboundRealBlocksPacket(List.of(
                new RealBlockDefinition(
                        Key.of("zako:test_block"),
                        List.of(new RealBlockDefinition.RealBlockStateDefinition(321, shape))
                )
        ));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ClientboundRealBlocksPacket.CODEC.encode(buffer, packet);

        assertEquals(1, buffer.readVarInt(), "format version");
        assertEquals(1, buffer.readVarInt(), "block count");
        assertEquals("zako:test_block", buffer.readUtf(), "Minecraft resource location");
        assertEquals(1, buffer.readVarInt(), "state count");
        assertEquals(321, buffer.readVarInt(), "CraftEngine custom state index");
        for (int shapeIndex = 0; shapeIndex < 3; shapeIndex++) {
            assertEquals(1, buffer.readVarInt(), "box count");
            assertEquals(6, buffer.readFloat());
            assertEquals(0, buffer.readFloat());
            assertEquals(6, buffer.readFloat());
            assertEquals(10, buffer.readFloat());
            assertEquals(16, buffer.readFloat());
            assertEquals(10, buffer.readFloat());
        }
        assertEquals(0, buffer.readVarInt(), "empty occlusion box count");
        assertEquals(0, buffer.readableBytes(), "trailing bytes");
    }
}
