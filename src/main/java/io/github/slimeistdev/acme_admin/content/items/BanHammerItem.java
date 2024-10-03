/*
 * ACME Admin Tools
 * Copyright (c) 2024 Sam Wagenaar and VivvyInks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.acme_admin.content.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.slimeistdev.acme_admin.registration.ACMEDamageTypes;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import io.github.slimeistdev.acme_admin.utils.BanUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BanHammerItem extends Item {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public BanHammerItem(Properties properties) {
        super(properties);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
        );
        this.defaultModifiers = builder.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && AuthUtils.isAuthorized(player) && target instanceof ServerPlayer serverTarget) {
            Level level = target.level();

            // Summon visual lightning
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningBolt != null) {
                lightningBolt.setPos(target.getX(), target.getY(), target.getZ());
                lightningBolt.setVisualOnly(true);
                lightningBolt.setCause(serverTarget);
                level.addFreshEntity(lightningBolt);
            }

            if (player.isShiftKeyDown()) {
                float amount = target.getHealth() - 0.5f;
                if (amount > 0.2f)
                    target.hurt(ACMEDamageTypes.of(level, ACMEDamageTypes.KISS_OF_DEATH, player), amount);
            } else {
                target.hurt(ACMEDamageTypes.of(level, ACMEDamageTypes.KISS_OF_DEATH, player), Float.MAX_VALUE);
            }

            BanUtils.banPlayer(serverTarget, player.getGameProfile().getName()+"["+player.getStringUUID()+"]", "Banned by Ban Hammer", null);

            return true;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        for (int i = 0; i <=2; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.ban_hammer.tooltip."+i));
        }
        if (level != null)
            AuthUtils.appendAuthTooltip(level, tooltipComponents);
    }
}
