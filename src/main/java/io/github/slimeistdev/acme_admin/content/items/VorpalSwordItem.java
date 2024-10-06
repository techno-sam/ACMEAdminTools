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

import io.github.slimeistdev.acme_admin.registration.ACMEDamageTypes;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VorpalSwordItem extends SwordItem {
    public VorpalSwordItem(Properties properties) {
        super(Tiers.NETHERITE, -4, -2.4F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && AuthUtils.isAuthorized(player)) {
            Level level = target.level();

            // Summon visual lightning
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningBolt != null) {
                lightningBolt.setPos(target.getX(), target.getY(), target.getZ());
                lightningBolt.setVisualOnly(true);
                level.addFreshEntity(lightningBolt);
            }

            target.hurt(ACMEDamageTypes.VORPAL_SWORD.create(level, attacker), Float.MAX_VALUE);

            return true;
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        for (int i = 0; i <= 1; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.vorpal_sword.tooltip."+i));
        }

        if (level != null)
            AuthUtils.appendAuthTooltip(level, tooltipComponents);
    }
}
