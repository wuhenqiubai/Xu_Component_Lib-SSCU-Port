package xu_mod.xu_component_lib.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.network.PacketDistributor;
import xu_mod.xu_component_lib.XuComponentLib;
import xu_mod.xu_component_lib.api.ComponentAPI;
import xu_mod.xu_component_lib.api.ComponentType;
import xu_mod.xu_component_lib.api.SerializableComponent;
import xu_mod.xu_component_lib.forge.capability.CapabilityRegistry;
import xu_mod.xu_component_lib.forge.capability.ComponentCapability;
import xu_mod.xu_component_lib.forge.network.ComponentSyncPacket;
import xu_mod.xu_component_lib.forge.network.NetworkHandler;

import java.util.function.Function;

public class PlatformImpl {
    public static <T> void registerComponent(ComponentType<T> type, ResourceLocation id, Function<T, SerializableComponent<T>> component) {
        // 提醒一下注册晚了
        if (CapabilityRegistry.IsInitialized) {
            XuComponentLib.LOGGER.error("Cannot register player component after capability registry has been initialized");
            return;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> SerializableComponent<T> getComponent(ComponentType<T> type, T owner, ResourceLocation id) {
        if (type == ComponentAPI.PLAYER && owner instanceof Player player) {
            Capability<ComponentCapability<Player>> cap = CapabilityRegistry.getPlayerCap(id);
            if (cap == null) return null;
            return (SerializableComponent<T>) (Object) player.getCapability(cap).map(ComponentCapability::getComponent).orElse(null);
        }
        if (type == ComponentAPI.ENTITY && owner instanceof LivingEntity entity) {
            Capability<ComponentCapability<LivingEntity>> cap = CapabilityRegistry.getEntityCap(id);
            if (cap == null) return null;
            return (SerializableComponent<T>) (Object) entity.getCapability(cap).map(ComponentCapability::getComponent).orElse(null);
        }
        throw new AssertionError();
    }

    public static <T> void syncComponent(ComponentType<T> type, T owner, ResourceLocation id) {
        if (type == ComponentAPI.PLAYER && owner instanceof Player player) {
            if (player.level().isClientSide) return;
            Capability<ComponentCapability<Player>> cap = CapabilityRegistry.getPlayerCap(id);
            if (cap == null) return;
            player.getCapability(cap).ifPresent(compCap -> {
                CompoundTag syncData = compCap.getSyncData();
                ComponentSyncPacket packet = new ComponentSyncPacket(player.getId(), id, syncData, true);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), packet);
            });
            return;
        }
        if (type == ComponentAPI.ENTITY && owner instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;
            Capability<ComponentCapability<LivingEntity>> cap = CapabilityRegistry.getEntityCap(id);
            if (cap == null) return;
            entity.getCapability(cap).ifPresent(compCap -> {
                CompoundTag syncData = compCap.getSyncData();
                ComponentSyncPacket packet = new ComponentSyncPacket(entity.getId(), id, syncData, false);
                NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
            });
            return;
        }
        throw new AssertionError();
    }
}
