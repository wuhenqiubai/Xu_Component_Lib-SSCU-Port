package xu_mod.xu_component_lib.forge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    // NeoForge 1.21 payload 注册：服务端→客户端 同步组件数据。
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                ComponentSyncPacket.TYPE,
                ComponentSyncPacket.STREAM_CODEC,
                ComponentSyncPacket::handle
        );
    }
}
