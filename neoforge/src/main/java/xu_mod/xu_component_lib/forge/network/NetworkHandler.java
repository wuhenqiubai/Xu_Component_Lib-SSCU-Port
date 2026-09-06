package xu_mod.xu_component_lib.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import xu_mod.xu_component_lib.XuComponentLib;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            XuComponentLib.rl("component_sync"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ComponentSyncPacket.class,
                ComponentSyncPacket::encode,
                ComponentSyncPacket::decode,
                ComponentSyncPacket::handle);
    }
}