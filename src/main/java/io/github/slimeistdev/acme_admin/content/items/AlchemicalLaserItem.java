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

import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AlchemicalLaserItem extends Item {
    public static final double RANGE = 128.0;

    public AlchemicalLaserItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!AuthUtils.isAuthorized(player) || !(player instanceof ServerPlayer serverPlayer))
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(player.getItemInHand(usedHand));
        if (mobEffects.isEmpty())
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        HitResult hitResult = player.pick(serverPlayer.isCreative() ? 5 : 4.5, 1.0f, false);

        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 viewVec = player.getViewVector(1.0f);
        Vec3 end = eyePos.add(viewVec.scale(RANGE));

        AABB entityBox = player.getBoundingBox().expandTowards(viewVec.scale(RANGE));

        double actualRange = RANGE * RANGE;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            actualRange = hitResult.getLocation().distanceToSqr(eyePos);
        }

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            player,
            eyePos, end,
            entityBox,
            e -> !e.isSpectator() && e.isPickable(),
            actualRange
        );

        if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            for (MobEffectInstance mobEffectInstance : mobEffects) {
                if (mobEffectInstance.getEffect().isInstantenous()) {
                    mobEffectInstance.getEffect().applyInstantenousEffect(player, player, livingEntity, mobEffectInstance.getAmplifier(), 1.0);
                } else {
                    livingEntity.addEffect(new MobEffectInstance(mobEffectInstance), player);
                }
            }

            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        for (int i = 0; i <=2; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.alchemical_laser.tooltip."+i));
        }
        PotionUtils.addPotionTooltip(stack, tooltipComponents, 1.0F);
        if (level != null)
            AuthUtils.appendAuthTooltip(level, tooltipComponents);
    }
}
