package xu_mod.xu_component_lib.forge.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleCapabilityProvider<T extends ComponentCapability<?>> implements ICapabilitySerializable<CompoundTag> {
    private final Capability<T> capability;
    private final T instance;
    private final LazyOptional<T> lazyOptional;

    public SimpleCapabilityProvider(Capability<T> capability, T instance) {
        this.capability = capability;
        this.instance = instance;
        this.lazyOptional = LazyOptional.of(() -> instance);
    }

    @Override
    public @NotNull <U> LazyOptional<U> getCapability(@NotNull Capability<U> cap, @Nullable Direction side) {
        return cap == capability ? lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        instance.deserializeNBT(arg);
    }
}