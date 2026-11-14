package com.iamkaf.bonded.util;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Optional;

/** Registry and tag lookups whose shape changed between Minecraft versions. */
public final class RegistryCompat {
    private RegistryCompat() {
    }

    public static <T> Registry<T> registry(RegistryAccess access, ResourceKey<? extends Registry<? extends T>> key) {
        //? if >=1.21.2
        return access.lookupOrThrow(key);
        //? if <1.21.2
        /*return access.registryOrThrow(key);*/
    }

    public static <T> Optional<HolderSet.Named<T>> tag(Registry<T> registry, TagKey<T> tag) {
        //? if >=1.21.2
        return registry.get(tag);
        //? if <1.21.2
        /*return registry.getTag(tag);*/
    }

    public static <T> Optional<HolderSet.Named<T>> tag(HolderGetter<T> getter, TagKey<T> tag) {
        return getter.get(tag);
    }

    /** Looks up an item by id. Unknown ids resolve to air, like the vanilla registry. */
    public static Item item(Identifier id) {
        //? if >=1.21.2
        return BuiltInRegistries.ITEM.getValue(id);
        //? if <1.21.2
        /*return BuiltInRegistries.ITEM.get(id);*/
    }

    public static Optional<HolderSet.Named<Item>> itemTag(TagKey<Item> tag) {
        return tag(BuiltInRegistries.ITEM, tag);
    }

    public static Ingredient ingredient(HolderSet<Item> items) {
        //? if >=1.21.2
        return Ingredient.of(items);
        //? if <1.21.2
        /*return Ingredient.of(items.stream().map(holder -> new ItemStack(holder.value())));*/
    }
}
