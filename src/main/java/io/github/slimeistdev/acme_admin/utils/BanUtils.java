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

package io.github.slimeistdev.acme_admin.utils;

import com.mojang.authlib.GameProfile;
import io.github.slimeistdev.acme_admin.ACMEAdminTools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BanUtils {
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public static void banPlayer(@NotNull ServerPlayer player, @Nullable String source, @Nullable String reason, @Nullable Date expires) {
        MinecraftServer server = player.getServer();
        UserBanList banList = server.getPlayerList().getBans();

        GameProfile profile = player.getGameProfile();
        UserBanListEntry entry = new UserBanListEntry(profile, new Date(), source, expires, reason);
        banList.add(entry);

        MutableComponent message = reason == null
            ? Component.translatable("multiplayer.disconnect.banned")
            : Component.translatable("multiplayer.disconnect.banned.reason", reason);
        if (expires != null) {
            message.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(expires)));
        }

        player.connection.disconnect(message);

        ACMEAdminTools.LOGGER.info("Banned player {} with message:\n{}", profile.getName(), message.getString());
    }
}
