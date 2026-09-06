package xu_mod.xu_component_lib.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import xu_mod.xu_component_lib.XuComponentLib;
import xu_mod.xu_component_lib.api.ComponentAPI;
import xu_mod.xu_component_lib.api.ComponentType;
import xu_mod.xu_component_lib.api.SerializableComponent;
import xu_mod.xu_component_lib.forge.capability.CapabilityRegistry;
import xu_mod.xu_component_lib.forge.capability.ComponentCapability;
import xu_mod.xu_component_lib.forge.network.ComponentSyncPacket;

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
            EntityCapability<ComponentCapability<Player>, @Nullable Void> cap = CapabilityRegistry.getPlayerCap(id);
            if (cap == null) return null;
            ComponentCapability<Player> compCap = player.getCapability(cap);
            return compCap == null ? null : (SerializableComponent<T>) (Object) compCap.getComponent();
        }
        if (type == ComponentAPI.ENTITY && owner instanceof LivingEntity entity) {
            EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> cap = CapabilityRegistry.getEntityCap(id);
            if (cap == null) return null;
            ComponentCapability<LivingEntity> compCap = entity.getCapability(cap);
            return compCap == null ? null : (SerializableComponent<T>) (Object) compCap.getComponent();
        }
        throw new AssertionError();
    }

    public static <T> void syncComponent(ComponentType<T> type, T owner, ResourceLocation id) {
        if (type == ComponentAPI.PLAYER && owner instanceof Player player) {
            if (player.level().isClientSide) return;
            EntityCapability<ComponentCapability<Player>, @Nullable Void> cap = CapabilityRegistry.getPlayerCap(id);
            if (cap == null) return;
            ComponentCapability<Player> compCap = player.getCapability(cap);
            if (compCap == null) return;
            CompoundTag syncData = compCap.getSyncData();
            ComponentSyncPacket packet = new ComponentSyncPacket(player.getId(), id, syncData, true);
            PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
            return;
        }
        if (type == ComponentAPI.ENTITY && owner instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;
            EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> cap = CapabilityRegistry.getEntityCap(id);
            if (cap == null) return;
            ComponentCapability<LivingEntity> compCap = entity.getCapability(cap);
            if (compCap == null) return;
            CompoundTag syncData = compCap.getSyncData();
            ComponentSyncPacket packet = new ComponentSyncPacket(entity.getId(), id, syncData, false);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
            return;
        }
        throw new AssertionError();
    }
}
