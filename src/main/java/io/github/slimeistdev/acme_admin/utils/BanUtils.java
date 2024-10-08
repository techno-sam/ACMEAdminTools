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
import io.github.slimeistdev.acme_admin.api.v0.causes.IBanCause;
import io.github.slimeistdev.acme_admin.api.v0.causes.IKickCause;
import io.github.slimeistdev.acme_admin.api.v0.events.ACMEBanCallback;
import io.github.slimeistdev.acme_admin.api.v0.events.ACMEKickCallback;
import io.github.slimeistdev.acme_admin.impl.v0.CancellableImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BanUtils {
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public static void banPlayer(@NotNull ServerPlayer player, @Nullable String source, @NotNull IBanCause cause) {
        CancellableImpl cancellation = new CancellableImpl();
        ACMEBanCallback.EVENT.invoker().onBan(player, source, cause, cancellation);
        if (cancellation.isCancelled()) {
            return;
        }

        Date expiration = cause.getExpiration();
        String reason = cause.getReason();

        MinecraftServer server = player.getServer();
        UserBanList banList = server.getPlayerList().getBans();

        GameProfile profile = player.getGameProfile();
        UserBanListEntry entry = new UserBanListEntry(profile, new Date(), source, expiration, reason);
        banList.add(entry);

        MutableComponent message = reason == null
            ? Component.translatable("multiplayer.disconnect.banned")
            : Component.translatable("multiplayer.disconnect.banned.reason", reason);
        if (expiration != null) {
            message.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(expiration)));
        }

        disconnect(player, message);

        ACMEAdminTools.LOGGER.info("Banned player {} with message:\n{}", profile.getName(), message.getString());
    }

    public static void kickPlayer(@NotNull ServerPlayer player, @NotNull IKickCause cause) {
        CancellableImpl cancellation = new CancellableImpl();
        ACMEKickCallback.EVENT.invoker().onKick(player, cause, cancellation);
        if (cancellation.isCancelled()) {
            return;
        }

        String reason = cause.getReason();

        MutableComponent message = reason == null
            ? Component.translatable("multiplayer.disconnect.kicked")
            : Component.literal(reason);

        disconnect(player, message);

        ACMEAdminTools.LOGGER.info("Kicked player {} with message:\n{}", player.getGameProfile().getName(), message.getString());
    }

    private static void disconnect(ServerPlayer player, Component message) {
        player.connection.disconnect(message);

        if (player.getClass().getName().equals("carpet.patches.EntityPlayerMPFake")) {
            try {
                Method killMethod = player.getClass().getDeclaredMethod("kill", Component.class);

                killMethod.invoke(player, message);
            } catch (NoSuchMethodException e) {
                ACMEAdminTools.LOGGER.error("Failed to disconnect Carpet fake player: kill method not found", e);
            } catch (InvocationTargetException e) {
                ACMEAdminTools.LOGGER.error("Failed to disconnect Carpet fake player: kill method threw an exception", e);
            } catch (IllegalAccessException e) {
                ACMEAdminTools.LOGGER.error("Failed to disconnect Carpet fake player: kill method is inaccessible", e);
            }
        }
    }
}
