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
import io.github.slimeistdev.acme_admin.api.v0.causes.IBanCause;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ACMEBanCallback {
    Event<ACMEBanCallback> EVENT = EventFactory.createArrayBacked(ACMEBanCallback.class,
        (listeners) -> (target, source, cause, cancellation) -> {
            for (ACMEBanCallback listener : listeners) {
                listener.onBan(target, source, cause, cancellation);
            }
        });

    /**
     * Called when a player is about to be banned by ACME action.
     * @param target The player being banned
     * @param source The string source to be recorded in the ban list
     * @param cause The cause of the ban
     * @param cancellation An object that can be used to cancel or un-cancel the ban. Cancelling the ban will not prevent remaining listeners from being called.
     */
    void onBan(@NotNull ServerPlayer target, @Nullable String source, @NotNull IBanCause cause, @NotNull ICancellable cancellation);
}
