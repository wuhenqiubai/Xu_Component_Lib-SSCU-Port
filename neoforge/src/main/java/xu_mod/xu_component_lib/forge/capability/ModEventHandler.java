package xu_mod.xu_component_lib.forge.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModEventHandler {
    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ComponentCapability.class);
    }
}
