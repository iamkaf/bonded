package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import java.util.List;

public final class TemperedGold {
    /** Durability factor shared by every armor piece, scaled per slot by {@link ArmorItem.Type#getDurability}. */
    public static final int ARMOR_DURABILITY = 8;

    public static final Tier TIER = new Tier() {
        @Override
        public int getUses() {
            return 500;
        }

        @Override
        public float getSpeed() {
            return 4.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 52;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return tagIngredient(Tags.TEMPERED_GOLD_TOOL_MATERIALS);
        }
    };

    private static Holder<ArmorMaterial> armorMaterial;

    private TemperedGold() {
    }

    /** Registers the armor material the first time an armor item needs it. */
    public static synchronized Holder<ArmorMaterial> armorMaterial() {
        if (armorMaterial == null) {
            ArmorMaterial gold = ArmorMaterials.GOLD.value();
            armorMaterial = Registry.registerForHolder(
                    BuiltInRegistries.ARMOR_MATERIAL,
                    Bonded.resource("tempered_gold"),
                    new ArmorMaterial(
                            gold.defense(),
                            gold.enchantmentValue(),
                            SoundEvents.ARMOR_EQUIP_GOLD,
                            () -> tagIngredient(Tags.REPAIRS_TEMPERED_GOLD_ARMOR),
                            List.of(new ArmorMaterial.Layer(Bonded.resource("tempered_gold"))),
                            gold.toughness(),
                            gold.knockbackResistance()
                    )
            );
        }
        return armorMaterial;
    }

    // Read at repair time, once the tag is loaded.
    private static Ingredient tagIngredient(TagKey<Item> tag) {
        return Ingredient.of(BuiltInRegistries.ITEM.getTag(tag).stream()
                .flatMap(holders -> holders.stream())
                .map(holder -> new ItemStack(holder.value())));
    }
}
