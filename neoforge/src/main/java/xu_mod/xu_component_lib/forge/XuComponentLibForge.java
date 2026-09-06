package xu_mod.xu_component_lib.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import xu_mod.xu_component_lib.XuComponentLib;
import xu_mod.xu_component_lib.forge.capability.CapabilityRegistry;
import xu_mod.xu_component_lib.forge.capability.ForgeEventHandler;
import xu_mod.xu_component_lib.forge.network.NetworkHandler;

@Mod(XuComponentLib.MOD_ID)
public class XuComponentLibForge {
    public XuComponentLibForge(IEventBus modBus) {
        XuComponentLib.init();
        // 先 create 组件 capability（RegisterCapabilitiesEvent 依赖已存在），再注册 provider。
        CapabilityRegistry.registerAll();
        // mod event bus：capability 注册 + payload 网络同步注册
        modBus.addListener(new ForgeEventHandler()::registerCaps);
        modBus.addListener(NetworkHandler::register);
        // game event bus：玩家复活继承（static method ref，只挂 onPlayerClone，避免连坐 @SubscribeEvent 的 registerCaps 到 game bus）
        NeoForge.EVENT_BUS.addListener(ForgeEventHandler::onPlayerClone);
    }
}
