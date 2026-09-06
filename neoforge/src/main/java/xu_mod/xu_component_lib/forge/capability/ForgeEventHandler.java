package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;
import xu_mod.xu_component_lib.api.ComponentAPI;
import xu_mod.xu_component_lib.api.SerializableComponent;

public class ForgeEventHandler {

    // Capability 注册（NeoForge 1.21）：按实体类型注册 provider。
    // Player 组件 → EntityType.PLAYER；LivingEntity 组件 → 所有 LivingEntity 派生实体类型。
    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        for (var entry : ComponentAPI.PLAYER.getRegisterMap().entrySet()) {
            ResourceLocation id = entry.getKey();
            var componentFactory = entry.getValue();
            EntityCapability<ComponentCapability<Player>, @Nullable Void> cap = CapabilityRegistry.getPlayerCap(id);
            event.registerEntity(cap, EntityType.PLAYER, (player, ctx) -> {
                SerializableComponent<Player> comp = componentFactory.apply(player);
                comp.init(player);
                return new ComponentCapability<>(comp, player);
            });
        }
        for (var entry : ComponentAPI.ENTITY.getRegisterMap().entrySet()) {
            ResourceLocation id = entry.getKey();
            var componentFactory = entry.getValue();
            EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> cap = CapabilityRegistry.getEntityCap(id);
            // 遍历所有实体类型，凡 LivingEntity 派生类都注册该组件（对齐 fabric 侧 CCA 的 registerFor(LivingEntity) 语义）
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (LivingEntity.class.isAssignableFrom(type.getBaseClass())) {
                    registerLivingCapability(event, cap, type, componentFactory);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerLivingCapability(
            RegisterCapabilitiesEvent event,
            EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> cap,
            EntityType<?> type,
            java.util.function.Function<LivingEntity, SerializableComponent<LivingEntity>> componentFactory) {
        event.registerEntity(cap, (EntityType) type, (livingEntity, ctx) -> {
            SerializableComponent<LivingEntity> comp = componentFactory.apply((LivingEntity) livingEntity);
            comp.init((LivingEntity) livingEntity);
            return new ComponentCapability<>(comp, (LivingEntity) livingEntity);
        });
    }

    // 玩家复活：继承旧数据并触发 onRespawn（game bus）
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ComponentAPI.onPlayerRespawn(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }
}
