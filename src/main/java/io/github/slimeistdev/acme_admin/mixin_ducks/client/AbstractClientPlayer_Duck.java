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

package io.github.slimeistdev.acme_admin.mixin_ducks.client;

import io.github.slimeistdev.acme_admin.content.particles.MarkedExistenceTracker;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface AbstractClientPlayer_Duck {
    void acme_admin$addModeratorSyncedEffect(MobEffectInstance effectInstance);
    void acme_admin$removeModeratorSyncedEffect(MobEffectInstance effectInstance);
    Set<MobEffectInstance> acme_admin$getModeratorSyncedEffects();

    @Nullable
    MarkedExistenceTracker acme_admin$getMarkedExistenceTracker();
}
