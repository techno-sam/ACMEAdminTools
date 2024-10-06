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

package io.github.slimeistdev.acme_admin.mixin.client.alchemical_laser;

import io.github.slimeistdev.acme_admin.mixin_ducks.client.LocalPlayer_Duck;
import io.github.slimeistdev.acme_admin.registration.ACMEItems;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.slimeistdev.acme_admin.content.items.AlchemicalLaserItem.RANGE;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements LocalPlayer_Duck {
    @Shadow @Final protected Minecraft minecraft;
    @Unique
    private @Nullable Entity acme_admin$laserTargetedEntity = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateLaserEntity(CallbackInfo ci) {
        acme_admin$laserTargetedEntity = null;

        LocalPlayer this$ = (LocalPlayer) (Object) this;

        if (this$.getItemInHand(InteractionHand.MAIN_HAND).getItem() != ACMEItems.ALCHEMICAL_LASER && this$.getItemInHand(InteractionHand.OFF_HAND).getItem() != ACMEItems.ALCHEMICAL_LASER) {
            return;
        }

        if (!AuthUtils.isAuthorized(this$)) {
            return;
        }

        Vec3 eyePos = this$.getEyePosition(1.0f);
        Vec3 viewVec = this$.getViewVector(1.0f);
        Vec3 end = eyePos.add(viewVec.scale(RANGE));

        AABB entityBox = this$.getBoundingBox().expandTowards(viewVec.scale(RANGE));

        double actualRange = RANGE * RANGE;

        if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            actualRange = this.minecraft.hitResult.getLocation().distanceToSqr(eyePos);
        }

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            this$,
            eyePos, end,
            entityBox,
            e -> !e.isSpectator() && e.isPickable(),
            actualRange
        );

        if (entityHitResult != null) {
            acme_admin$laserTargetedEntity = entityHitResult.getEntity();
        }
    }

    @Override
    @Final
    public @Nullable Entity acme_admin$getLaserTargetedEntity() {
        return acme_admin$laserTargetedEntity;
    }
}
