package com.iamkaf.bonded.bonuses;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.util.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ArmorHealthBonus5k1Heart implements BondBonus {
    public static final ResourceLocation ID = Bonded.resource("armor_health_bonus_5000");
    // 1 heart
    private static final float BONUS = 2f;

    @Override
    public boolean shouldApply(ItemStack gear, GearTypeLeveler gearType, ItemLevelContainer container) {
        return gear.getItem() instanceof ArmorItem && container.getBond() >= 5000;
    }

    @Override
    public AttributeModifierHolder getBonus(ItemStack gear, GearTypeLeveler gearTypeLeveler,
            ItemLevelContainer container) {
        ArmorItem item = (ArmorItem) gear.getItem();
        return new AttributeModifierHolder(
                Attributes.MAX_HEALTH,
                new AttributeModifier(ID, BONUS, AttributeModifier.Operation.ADD_VALUE),
                ItemUtils.slotToSlotGroup(item.getEquipmentSlot())
        );
    }
}
