package net.momirealms.craftengine.neoforge.network.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import net.momirealms.craftengine.neoforge.block.RealBlockShape;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ClientboundRealBlocksPacketTest {

    @Test
    void roundTripsRegistryIdsStateMappingsAndVoxelShapes() {
        RealBlockShape shape = new RealBlockShape(
                List.of(new RealBlockShape.Box(6, 0, 6, 10, 16, 10)),
                List.of(new RealBlockShape.Box(6, 0, 6, 10, 16, 10)),
                List.of(new RealBlockShape.Box(6, 0, 6, 10, 16, 10)),
                List.of()
        );
        ClientboundRealBlocksPacket packet = new ClientboundRealBlocksPacket(List.of(
                new BlockManager.RealBlockDefinition(
                        ResourceLocation.parse("zako:test_block"),
                        List.of(
                                new BlockManager.RealBlockStateDefinition(123, shape),
                                new BlockManager.RealBlockStateDefinition(124, shape)
                        )
                )
        ));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ClientboundRealBlocksPacket.CODEC.encode(buffer, packet);
        ClientboundRealBlocksPacket decoded = ClientboundRealBlocksPacket.CODEC.decode(buffer);

        assertEquals(packet, decoded);
    }
}
