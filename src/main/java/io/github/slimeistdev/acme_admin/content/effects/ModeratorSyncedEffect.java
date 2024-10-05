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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Marker interface to mark that an effect should be sent to ONLY OP-ed clients
 * <br>
 * Also contains methods to handle receipt clientside
 */
public interface ModeratorSyncedEffect {
    @Environment(EnvType.CLIENT)
    default void onAdded(MobEffectInstance effectInstance, AbstractClientPlayer player) {}

    @Environment(EnvType.CLIENT)
    default void onRemoved(MobEffectInstance effectInstance, AbstractClientPlayer player) {}

    @Environment(EnvType.CLIENT)
    default void onTick(MobEffectInstance effectInstance, AbstractClientPlayer player) {}
}
