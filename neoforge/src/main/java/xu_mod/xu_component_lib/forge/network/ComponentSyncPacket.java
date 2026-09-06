package xu_mod.xu_component_lib.forge.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xu_mod.xu_component_lib.XuComponentLib;
import xu_mod.xu_component_lib.forge.capability.CapabilityRegistry;
import xu_mod.xu_component_lib.forge.capability.ComponentCapability;

// NeoForge 1.21 payload 同步包：服务端→客户端，把某个组件的 sync 数据广播给目标实体。
public record ComponentSyncPacket(int entityId, ResourceLocation componentId, CompoundTag data, boolean isPlayer) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ComponentSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(XuComponentLib.rl("component_sync"));

    public static final StreamCodec<ByteBuf, ComponentSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ComponentSyncPacket::entityId,
            ResourceLocation.STREAM_CODEC, ComponentSyncPacket::componentId,
            ByteBufCodecs.COMPOUND_TAG, ComponentSyncPacket::data,
            ByteBufCodecs.BOOL, ComponentSyncPacket::isPlayer,
            ComponentSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComponentSyncPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(pkt.entityId);
            if (entity == null) return;
            if (pkt.isPlayer && entity instanceof Player player) {
                var cap = CapabilityRegistry.getPlayerCap(pkt.componentId);
                if (cap != null) {
                    ComponentCapability<Player> compCap = player.getCapability(cap);
                    if (compCap != null) compCap.applySyncData(pkt.data);
                }
            } else if (!pkt.isPlayer && entity instanceof LivingEntity living) {
                var cap = CapabilityRegistry.getEntityCap(pkt.componentId);
                if (cap != null) {
                    ComponentCapability<LivingEntity> compCap = living.getCapability(cap);
                    if (compCap != null) compCap.applySyncData(pkt.data);
                }
            }
        });
    }
}
