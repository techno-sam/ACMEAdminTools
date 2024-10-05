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

package io.github.slimeistdev.acme_admin.networking;

import io.github.slimeistdev.acme_admin.ACMEAdminTools;
import io.github.slimeistdev.acme_admin.content.effects.ModeratorSyncedEffect;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class ACMEServerNetworking {
    public static void register() {

    }

    public static void syncEffect(ServerPlayer player, MobEffectInstance effectInstance, boolean add) {
        if (!(effectInstance.getEffect() instanceof ModeratorSyncedEffect)) {
            ACMEAdminTools.LOGGER.warn("Attempted to sync non-ModeratorSyncedEffect {} to client", effectInstance.getEffect());
            return;
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(add);
        buf.writeVarInt(player.getId());
        buf.writeNbt(effectInstance.save(new CompoundTag()));

        for (ServerPlayer to : player.server.getPlayerList().getPlayers()) {
            if (!AuthUtils.isAuthorized(to)) continue;

            ServerPlayNetworking.send(to, ACMENetworkingConstants.MODERATOR_EFFECT_SYNC_S2C, buf);
        }
    }
}
