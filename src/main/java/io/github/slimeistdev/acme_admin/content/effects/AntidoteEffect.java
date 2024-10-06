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

package io.github.slimeistdev.acme_admin.content.effects;

import io.github.slimeistdev.acme_admin.mixin.common.misc.LivingEntityAccessor;
import io.github.slimeistdev.acme_admin.registration.ACMEMobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class AntidoteEffect extends ACMEMobEffect {
    public AntidoteEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
        cure(livingEntity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration >= 1;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        cure(livingEntity);
    }

    private static void cure(LivingEntity target) {
        if (!target.level().isClientSide) {
            Iterator<MobEffectInstance> iterator = target.getActiveEffects().iterator();

            while (iterator.hasNext()) {
                MobEffectInstance effectInstance = iterator.next();

                if (!ACMEMobEffects.isModerationEffect(effectInstance.getEffect())) continue;

                ((LivingEntityAccessor) target).invokeOnEffectRemoved(effectInstance);
                iterator.remove();
            }
        }
    }
}
