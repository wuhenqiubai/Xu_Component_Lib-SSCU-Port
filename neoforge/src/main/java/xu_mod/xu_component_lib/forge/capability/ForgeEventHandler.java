package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xu_mod.xu_component_lib.Platform;
import xu_mod.xu_component_lib.XuComponentLib;
import xu_mod.xu_component_lib.api.ComponentAPI;
import xu_mod.xu_component_lib.api.SerializableComponent;

public class ForgeEventHandler {
    @SubscribeEvent
    public void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof Player player) {
            for (var entry : ComponentAPI.PLAYER.getRegisterMap().entrySet()) {
                ResourceLocation id = entry.getKey();
                var componentFactory = entry.getValue();
                SerializableComponent<Player> comp = componentFactory.apply(player);
                comp.init(player);
                ComponentCapability<Player> capability = new ComponentCapability<>(comp, player);
                event.addCapability(id, new SimpleCapabilityProvider<>(CapabilityRegistry.getPlayerCap(id), capability));
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            for (var entry : ComponentAPI.ENTITY.getRegisterMap().entrySet()) {
                ResourceLocation id = entry.getKey();
                var componentFactory = entry.getValue();
                SerializableComponent<LivingEntity> comp = componentFactory.apply(livingEntity);
                comp.init(livingEntity);
                ComponentCapability<LivingEntity> capability = new ComponentCapability<>(comp, livingEntity);
                event.addCapability(id, new SimpleCapabilityProvider<>(CapabilityRegistry.getEntityCap(id), capability));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ComponentAPI.onPlayerRespawn(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }
}
