/*
 * ACME Admin Tools
 * Copyright (c) 2025 Sam Wagenaar and VivvyInks
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

package io.github.slimeistdev.acme_admin.compat.banhammer;

import eu.pb4.banhammer.api.BanHammer;
import eu.pb4.banhammer.api.PunishmentData;
import eu.pb4.banhammer.api.PunishmentType;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class BanHammerBanUtils {
    private static final UUID DOOM_BAN_SOURCE_UUID = UUID.fromString("fa8626b0-cc20-46cb-9cd4-a2de42ebdbf0");

    public static void ban(@NotNull ServerPlayer player, @Nullable String source, @Nullable String reason, @Nullable Date expiration) {
        BanHammer.punish(create(player, source, reason, new Date(), expiration, PunishmentType.BAN));
    }

    public static void kick(@NotNull ServerPlayer player, @Nullable String source, @Nullable String reason) {
        BanHammer.punish(create(player, source, reason, new Date(), null, PunishmentType.KICK));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static PunishmentData create(
        @NotNull ServerPlayer player,
        @Nullable String source,
        @Nullable String reason,
        @NotNull Date now,
        @Nullable Date expiration,
        @NotNull PunishmentType type
    ) {
        long nowL = now.getTime() / 1000L;

        long expirationL;
        if (expiration == null) {
            expirationL = -1L;
        } else {
            try {
                expirationL = expiration.getTime() / 1000L - nowL;
            } catch (Exception e) {
                expirationL = -1L;
            }
        }

        UUID sourceUUID = Util.NIL_UUID;

        if (source != null && source.equals("$$acme_admin:doom$$")) {
            sourceUUID = DOOM_BAN_SOURCE_UUID;
            source = "Doom Potion";
        }

        return new PunishmentData(
            player.getUUID(),
            player.getIpAddress(),
            player.getDisplayName(),
            player.getGameProfile().getName(),
            sourceUUID,
            Component.literal(Objects.requireNonNullElse(source, "ACME Admin Tools")),
            nowL,
            expirationL,
            Objects.requireNonNullElse(reason, "No reason provided"),
            type
        );
    }
}
