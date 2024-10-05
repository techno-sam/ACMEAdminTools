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

package io.github.slimeistdev.acme_admin.mixin.common.moderator_synced_effects;

import io.github.slimeistdev.acme_admin.content.effects.ModeratorSyncedEffect;
import io.github.slimeistdev.acme_admin.networking.ACMEServerNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onEffectAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;addAttributeModifiers(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/attributes/AttributeMap;I)V"))
    @SuppressWarnings("ConstantValue")
    private void sendModeratorEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        if (effectInstance.getEffect() instanceof ModeratorSyncedEffect && ((Object) this) instanceof ServerPlayer player) {
            ACMEServerNetworking.syncEffect(player, effectInstance, true);
        }
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/attributes/AttributeMap;I)V"))
    @SuppressWarnings("ConstantValue")
    private void removeModeratorEffect(MobEffectInstance effectInstance, CallbackInfo ci) {
        if (effectInstance.getEffect() instanceof ModeratorSyncedEffect && ((Object) this) instanceof ServerPlayer player) {
            ACMEServerNetworking.syncEffect(player, effectInstance, false);
        }
    }
}
