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

import io.github.slimeistdev.acme_admin.registration.ACMEDamageTypes;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import io.github.slimeistdev.acme_admin.utils.BanUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class DoomEffect extends ACMEMobEffect {
    public DoomEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (AuthUtils.isImmuneToModerationEffects(livingEntity)) {
            return;
        }

        Level level = livingEntity.level();

        // Summon visual lightning
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        if (lightningBolt != null) {
            lightningBolt.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            lightningBolt.setVisualOnly(true);
            level.addFreshEntity(lightningBolt);
        }

        livingEntity.hurt(ACMEDamageTypes.DOOM.create(level), Float.MAX_VALUE);

        if (amplifier >= 1 && livingEntity instanceof ServerPlayer target) {
            BanUtils.banPlayer(target, "$$acme_admin:doom$$", "Doomed by your sins", null);
        }
    }
}
