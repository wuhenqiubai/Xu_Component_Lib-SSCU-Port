package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import xu_mod.xu_component_lib.api.SerializableComponent;

public class ComponentCapability <T> implements INBTSerializable<CompoundTag> {
    private final SerializableComponent<T> component;

    public ComponentCapability(SerializableComponent<T> component, T owner) {
        this.component = component;
        component.init(owner);
    }

    public SerializableComponent<T> getComponent() {
        return component;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        component.save(tag, false);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        component.load(nbt, false);
    }

    public CompoundTag getSyncData() {
        CompoundTag tag = new CompoundTag();
        component.save(tag, true);
        return tag;
    }

    public void applySyncData(CompoundTag syncData) {
        component.load(syncData, true);
    }
}
