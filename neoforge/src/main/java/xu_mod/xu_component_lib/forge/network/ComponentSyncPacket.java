package xu_mod.xu_component_lib.forge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import xu_mod.xu_component_lib.forge.capability.CapabilityRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public record ComponentSyncPacket(int entityId, ResourceLocation componentId, CompoundTag data, boolean isPlayer) {
    public static void encode(ComponentSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
        buf.writeResourceLocation(pkt.componentId);
        buf.writeNbt(pkt.data);
        buf.writeBoolean(pkt.isPlayer);
    }

    public static ComponentSyncPacket decode(FriendlyByteBuf buf) {
        return new ComponentSyncPacket(buf.readInt(), buf.readResourceLocation(), buf.readNbt(), buf.readBoolean());
    }
    public static void handle(ComponentSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(pkt.entityId);
            if (entity == null) return;
            if (pkt.isPlayer && entity instanceof Player player) {
                var cap = CapabilityRegistry.getPlayerCap(pkt.componentId);
                if (cap != null) {
                    player.getCapability(cap).ifPresent(compCap -> compCap.applySyncData(pkt.data));
                }
            } else if (!pkt.isPlayer && entity instanceof LivingEntity living) {
                var cap = CapabilityRegistry.getEntityCap(pkt.componentId);
                if (cap != null) {
                    living.getCapability(cap).ifPresent(compCap -> compCap.applySyncData(pkt.data));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
