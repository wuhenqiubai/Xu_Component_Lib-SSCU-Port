package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import xu_mod.xu_component_lib.api.ComponentAPI;
import xu_mod.xu_component_lib.api.SerializableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CapabilityRegistry {
    public static boolean IsInitialized = false;

    public static final HashMap<ResourceLocation, Capability<ComponentCapability<Player>>> PLAYER_CAPS = new HashMap<>();
    public static final Map<ResourceLocation, Capability<ComponentCapability<LivingEntity>>> ENTITY_CAPS = new HashMap<>();

    public static void registerAll() {
        for (Map.Entry<ResourceLocation, Function<Player, SerializableComponent<Player>>> entry : ComponentAPI.PLAYER.getRegisterMap().entrySet()) {
            ResourceLocation id = entry.getKey();
            Capability<ComponentCapability<Player>> cap = CapabilityManager.get(new CapabilityToken<>() {});
            PLAYER_CAPS.put(id, cap);
        }
        for (Map.Entry<ResourceLocation, Function<LivingEntity, SerializableComponent<LivingEntity>>> entry : ComponentAPI.ENTITY.getRegisterMap().entrySet()) {
            ResourceLocation id = entry.getKey();
            Capability<ComponentCapability<LivingEntity>> cap = CapabilityManager.get(new CapabilityToken<>() {});
            ENTITY_CAPS.put(id, cap);
        }
        IsInitialized = true;
    }

    public static Capability<ComponentCapability<Player>> getPlayerCap(ResourceLocation id) {
        return PLAYER_CAPS.get(id);
    }

    public static Capability<ComponentCapability<LivingEntity>> getEntityCap(ResourceLocation id) {
        return ENTITY_CAPS.get(id);
    }
}
