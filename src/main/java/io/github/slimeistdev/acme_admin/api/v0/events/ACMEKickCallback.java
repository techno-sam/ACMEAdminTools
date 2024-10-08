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

package io.github.slimeistdev.acme_admin.api.v0.events;

import io.github.slimeistdev.acme_admin.api.v0.ICancellable;
import io.github.slimeistdev.acme_admin.api.v0.causes.IKickCause;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ACMEKickCallback {
    Event<ACMEKickCallback> EVENT = EventFactory.createArrayBacked(ACMEKickCallback.class,
        (listeners) -> (target, cause, cancellation) -> {
            for (ACMEKickCallback listener : listeners) {
                listener.onKick(target, cause, cancellation);
            }
        });

    /**
     * Called when a player is about to be kicked by ACME action.
     * @param target The player being kicked
     * @param cause The cause of the kick
     * @param cancellation An object that can be used to cancel or un-cancel the kick. Cancelling the kick will not prevent remaining listeners from being called.
     */
    void onKick(@NotNull ServerPlayer target, @NotNull IKickCause cause, @NotNull ICancellable cancellation);
}
