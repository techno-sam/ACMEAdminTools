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

package io.github.slimeistdev.acme_admin.mixin.common.moderation_effects;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.acme_admin.registration.ACMEDamageTypeTags;
import io.github.slimeistdev.acme_admin.registration.ACMEMobEffects;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("ConstantValue")
    private void inhibitSwing(CallbackInfoReturnable<Integer> cir) {
        if (((Object) this) instanceof Player player && ACMEMobEffects.inhibited(player)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getJumpPower", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("ConstantValue")
    private void inhibitJump(CallbackInfoReturnable<Float> cir) {
        if (((Object) this) instanceof Player player && ACMEMobEffects.inhibited(player)) {
            cir.setReturnValue(0F);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("ConstantValue")
    private void inhibitJumpFromGround(CallbackInfo ci) {
        if (((Object) this) instanceof Player player && ACMEMobEffects.inhibited(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void bypassTotemDeathProtection(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource.is(ACMEDamageTypeTags.BYPASSES_TOTEMS)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean keepModerationEffects(LivingEntity instance, Operation<Boolean> original) {
        return ACMEMobEffects.safeRemoveAllEffects(instance, original::call);
    }

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void moderationEffectsArePlayerOnly(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (ACMEMobEffects.isModerationEffect(effectInstance.getEffect()) && AuthUtils.isImmuneToModerationEffects(this)) {
            cir.setReturnValue(false);
        }
    }
}
