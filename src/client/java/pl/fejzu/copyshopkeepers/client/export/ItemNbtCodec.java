package pl.fejzu.copyshopkeepers.client.export;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;

public final class ItemNbtCodec {

    private ItemNbtCodec() {
    }

    public static NbtCompound encode(ItemStack stack, DynamicRegistryManager registries) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registries);
        DataResult<NbtElement> result = ItemStack.CODEC.encodeStart(ops, stack);
        NbtElement element = result.result().orElse(null);
        if (element instanceof NbtCompound compound) {
            return compound;
        }
        return null;
    }

    public static JsonElement toJson(NbtElement nbt) {
        return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt);
    }
}
