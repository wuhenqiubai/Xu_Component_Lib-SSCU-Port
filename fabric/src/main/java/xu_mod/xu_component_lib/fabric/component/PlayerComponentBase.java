package xu_mod.xu_component_lib.fabric.component;

import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xu_mod.xu_component_lib.api.SerializableComponent;

public class PlayerComponentBase implements AutoSyncedComponent {
    public SerializableComponent<Player> component;

    public PlayerComponentBase(SerializableComponent<Player> component) {
        this.component = component;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.component.load(tag, false);
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.component.save(tag, false);
    }

    @Override
    public void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer recipient) {
        CompoundTag tag = new CompoundTag();
        this.component.save(tag, true);
        buf.writeNbt(tag);
    }

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            this.component.load(tag, true);
        }
    }
}
