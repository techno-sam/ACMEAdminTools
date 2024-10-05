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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractBanHammerItem extends Item {
    public AbstractBanHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public final boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && AuthUtils.isAuthorized(player) && target instanceof ServerPlayer serverTarget) {
            if (AuthUtils.isImmuneToModerationEffects(serverTarget))
                return false;

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
                float amount = target.getHealth() - 1f;
                if (amount > 0.2f)
                    target.hurt(ACMEDamageTypes.KISS_OF_DEATH.create(level, player), amount);
            } else {
                target.hurt(ACMEDamageTypes.KISS_OF_DEATH.create(level, player), Float.MAX_VALUE);
            }

            applyModerationAction(stack, serverTarget, player);

            return true;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    protected abstract void applyModerationAction(ItemStack stack, ServerPlayer target, Player attacker);

    protected abstract void appendCustomHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced);

    @Override
    public final void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        appendCustomHoverText(stack, level, tooltipComponents, isAdvanced);
        if (level != null)
            AuthUtils.appendAuthTooltip(level, tooltipComponents);
    }
}
