/*
 * ACME Admin Tools
 * Copyright (c) 2024 Sam Wagenaar and VivvyInks
 *
 * FootprintParticle (https://github.com/Rivmun/FootprintParticle)
 * Copyright (c) 2023 Rivmun
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

package io.github.slimeistdev.acme_admin.mixin.client.marked_effect;

import io.github.slimeistdev.acme_admin.content.effects.MarkedEffect;
import io.github.slimeistdev.acme_admin.content.particles.MarkedExistenceTracker;
import io.github.slimeistdev.acme_admin.content.particles.MarkedFootprintParticleOptions;
import io.github.slimeistdev.acme_admin.mixin_ducks.client.AbstractClientPlayer_Duck;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends LivingEntityMixin implements AbstractClientPlayer_Duck {
    @Unique
    private int acme_admin$markedLevel = -1;

    @Unique
    private @Nullable MarkedExistenceTracker acme_admin$markedExistenceTracker;

    @Unique
    private int acme_admin$markedFootprintTimer;

    @Unique
    private boolean acme_admin$wasOnGround;

    @Override
    @Final
    public @Nullable MarkedExistenceTracker acme_admin$getMarkedExistenceTracker() {
        if (acme_admin$markedLevel >= 0) {
            if (acme_admin$markedExistenceTracker == null) {
                acme_admin$markedExistenceTracker = new MarkedExistenceTracker((LivingEntity) (Object) this);
            }
        } else if (acme_admin$markedExistenceTracker != null) {
            acme_admin$markedExistenceTracker.markInactive();
            acme_admin$markedExistenceTracker = null;
        }

        return acme_admin$markedExistenceTracker;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        acme_admin$markedLevel = getActiveEffects().stream()
            .filter(i -> i.getEffect() instanceof MarkedEffect)
            .map(MobEffectInstance::getAmplifier)
            .max(Integer::compare)
            .orElse(-1);

        if (!(acme_admin$markedLevel >= 0) && acme_admin$markedExistenceTracker != null) {
            acme_admin$markedExistenceTracker.markInactive();
            acme_admin$markedExistenceTracker = null;
        }

        // footprint gen
        if (acme_admin$markedLevel >= 0) {
            if (acme_admin$markedFootprintTimer <= 0) {
                AbstractClientPlayer this$ = (AbstractClientPlayer) (Object) this;

                // Either on ground moving or landing
                if ((this$.getDeltaMovement().horizontalDistanceSqr() != 0 && this$.onGround()) || (!acme_admin$wasOnGround && this$.onGround())) {
                    this.acme_admin$footprintGenerator();
                }
                acme_admin$wasOnGround = this$.onGround();
            } else {
                acme_admin$markedFootprintTimer--;
            }
        } else {
            acme_admin$markedFootprintTimer = 0;
        }
    }

    @Override
    protected void acme_admin$onJump(CallbackInfo ci) {
        acme_admin$footprintGenerator();
    }

    @Override
    protected void acme_admin$onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (acme_admin$markedLevel >= 0) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void acme_admin$footprintGenerator() {
        AbstractClientPlayer this$ = (AbstractClientPlayer) (Object) this;

        final float secPerPrint = 0.5f;

        acme_admin$markedFootprintTimer = this$.isSprinting() ? (int) (secPerPrint * 13.33f) : (int) (secPerPrint * 20);

        double px = this$.getX();
        double py = this$.getY() + 0.01f;
        double pz = this$.getZ();

        // Horizontal Offset
        // Front and back
        int side = Math.random() > 0.5 ? 1 : -1;
        double hOffset = 0.0625f;
        px = px - hOffset * side * Mth.sin((float) Math.toRadians(this$.getRotationVector().y));
        pz = pz + hOffset * side * Mth.cos((float) Math.toRadians(this$.getRotationVector().y));

        // Left and right
        side = Math.random() > 0.5f ? 1 : -1;
        hOffset = 0.125f;
        px = px - hOffset * side * Mth.sin((float) Math.toRadians(this$.getRotationVector().y + 90));
        pz = pz + hOffset * side * Mth.cos((float) Math.toRadians(this$.getRotationVector().y + 90));

        // Generate
        double dx, dz;      // get facing
        if (this$.getDeltaMovement().horizontalDistanceSqr() == 0) {
            dx = -Mth.sin((float) Math.toRadians(this$.getRotationVector().y));
            dz =  Mth.cos((float) Math.toRadians(this$.getRotationVector().y));
        } else {
            dx = this$.getDeltaMovement().x();
            dz = this$.getDeltaMovement().z();
        }

        MarkedFootprintParticleOptions options = new MarkedFootprintParticleOptions(this$.getId(), 30 * (acme_admin$markedLevel + 1));
        this$.level().addParticle(options, px, py, pz, dx, 0, dz);
    }
}
