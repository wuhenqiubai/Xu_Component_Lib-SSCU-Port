package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;
import xu_mod.xu_component_lib.api.ComponentAPI;

import java.util.HashMap;
import java.util.Map;

public class CapabilityRegistry {
    public static boolean IsInitialized = false;

    // NeoForge 1.21: 每个组件 id 对应一个 EntityCapability（context 为 Void，无需额外上下文）。
    public static final HashMap<ResourceLocation, EntityCapability<ComponentCapability<Player>, @Nullable Void>> PLAYER_CAPS = new HashMap<>();
    public static final Map<ResourceLocation, EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void>> ENTITY_CAPS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void registerAll() {
        for (ResourceLocation id : ComponentAPI.PLAYER.getRegisterMap().keySet()) {
            EntityCapability<ComponentCapability<Player>, @Nullable Void> cap =
                    EntityCapability.createVoid(id, (Class<ComponentCapability<Player>>) (Class<?>) ComponentCapability.class);
            PLAYER_CAPS.put(id, cap);
        }
        for (ResourceLocation id : ComponentAPI.ENTITY.getRegisterMap().keySet()) {
            EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> cap =
                    EntityCapability.createVoid(id, (Class<ComponentCapability<LivingEntity>>) (Class<?>) ComponentCapability.class);
            ENTITY_CAPS.put(id, cap);
        }
        IsInitialized = true;
    }

    public static EntityCapability<ComponentCapability<Player>, @Nullable Void> getPlayerCap(ResourceLocation id) {
        return PLAYER_CAPS.get(id);
    }

    public static EntityCapability<ComponentCapability<LivingEntity>, @Nullable Void> getEntityCap(ResourceLocation id) {
        return ENTITY_CAPS.get(id);
    }
}
