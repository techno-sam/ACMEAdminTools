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

package io.github.slimeistdev.acme_admin.mixin.client.moderator_synced_effects;

import com.google.common.collect.ImmutableSet;
import io.github.slimeistdev.acme_admin.content.effects.MarkedEffect;
import io.github.slimeistdev.acme_admin.content.effects.ModeratorSyncedEffect;
import io.github.slimeistdev.acme_admin.mixin.common.misc.MobEffectInstanceAccessor;
import io.github.slimeistdev.acme_admin.mixin_ducks.client.AbstractClientPlayer_Duck;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin extends LivingEntityMixin implements AbstractClientPlayer_Duck {
    @Unique
    private final Set<MobEffectInstance> acme_admin$moderatorSyncedEffects = new HashSet<>();

    @Unique
    private boolean acme_admin$isMarked = false;

    @Override
    @Final
    public void acme_admin$addModeratorSyncedEffect(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() instanceof ModeratorSyncedEffect mse) {
            acme_admin$moderatorSyncedEffects.add(effectInstance);
            mse.onAdded(effectInstance, (AbstractClientPlayer) (Object) this);

            acme_admin$isMarked = acme_admin$moderatorSyncedEffects.stream()
                .map(MobEffectInstance::getEffect)
                .anyMatch(MarkedEffect.class::isInstance);
        }
    }

    @Override
    @Final
    public void acme_admin$removeModeratorSyncedEffect(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() instanceof ModeratorSyncedEffect mse) {
            acme_admin$moderatorSyncedEffects.remove(effectInstance);
            mse.onRemoved(effectInstance, (AbstractClientPlayer) (Object) this);

            acme_admin$isMarked = acme_admin$moderatorSyncedEffects.stream()
                .map(MobEffectInstance::getEffect)
                .anyMatch(MarkedEffect.class::isInstance);
        }
    }

    @Override
    @Final
    public Set<MobEffectInstance> acme_admin$getModeratorSyncedEffects() {
        return ImmutableSet.copyOf(acme_admin$moderatorSyncedEffects);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Iterator<MobEffectInstance> iterator = acme_admin$moderatorSyncedEffects.iterator();

        while (iterator.hasNext()) {
            MobEffectInstance effectInstance = iterator.next();

            if (effectInstance.getEffect() instanceof ModeratorSyncedEffect mse) {
                mse.onTick(effectInstance, (AbstractClientPlayer) (Object) this);
            }

            if (!effectInstance.isInfiniteDuration()) {
                ((MobEffectInstanceAccessor) effectInstance).invokeTickDownDuration();

                if (effectInstance.getDuration() <= 0) {
                    if (effectInstance.getEffect() instanceof ModeratorSyncedEffect mse) {
                        mse.onRemoved(effectInstance, (AbstractClientPlayer) (Object) this);
                    }
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void acme_admin$overrideCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (acme_admin$isMarked) {
            cir.setReturnValue(true);
        }
    }
}
